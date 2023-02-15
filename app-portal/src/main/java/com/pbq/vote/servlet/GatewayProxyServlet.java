package com.pbq.vote.servlet;

import brave.http.HttpTracing;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.pbq.vote.component.ServiceCenter;
import com.pbq.vote.config.GatewayConfig;
import com.pbq.vote.context.PortalContext;
import com.pbq.vote.po.ClientInfo;
import com.pbq.vote.utils.HttpClientUtils;
import com.pbq.vote.utils.WebUtil;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GatewayProxyServlet extends GenericServlet {
    private static final Logger logger = LoggerFactory.getLogger(GatewayProxyServlet.class);
    public static String PROXY_PREFIX = "/api/";
    public static int PROXY_PREFIX_LENGTH = PROXY_PREFIX.length();
    @Autowired
    ServiceCenter serviceCenter;
    @Autowired
    private HttpTracing httpTracing;
    CloseableHttpClient httpClient;
    @Autowired
    private GatewayConfig gateWayConfig;
    @Autowired
    private Environment env;

    @Override
    public void init() throws ServletException {
        this.httpClient = HttpClientUtils.httpClient(gateWayConfig.getConnTimeOut(), gateWayConfig.getReadTimeOut(), gateWayConfig.getMaxPerRoute(),
                gateWayConfig.getMaxTotal(), httpTracing);
    }

    @Override
    public void destroy() {
        if (this.httpClient != null) {
            try{
                this.httpClient.close();
            }catch (Exception e){
            }
        }
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        PortalContext portalContext = (PortalContext) req.getAttribute(PortalContext.PORTAL_CTX_ATTR_KEY);
        if (portalContext == null) {
            portalContext = new PortalContext(request, response);
        }
        String path = request.getRequestURI();
        if (path.startsWith(PROXY_PREFIX)) {
            path = path.substring(PROXY_PREFIX_LENGTH);
        }
        int pos = path.indexOf("/");
        String serviceId = path;
        String queryPath = "";
        if (pos > 0) {
            serviceId = path.substring(0, pos);
            queryPath = path.substring(pos);
        }
        doRute(req, request, response, path, serviceId, queryPath, portalContext);
    }

    private void doRute(ServletRequest req, HttpServletRequest request, HttpServletResponse response, String path, String serviceId, String queryPath, PortalContext portalContext) throws IOException {
        String root = serviceCenter.findOneService(serviceId);
        String url = root + queryPath;
        ClientInfo client = (ClientInfo) req.getAttribute(PortalContext.CLIENT_ATTR_KEY);
        AuditLog audit = new AuditLog();

//        audit.traceId = httpTracing.tracing().currentTraceContext().get().traceIdString();
        audit.traceId = UUID.randomUUID().toString();
        MDC.put("traceId", audit.traceId);
        audit.time = DateTime.now().toString("HH:mm:ss.SSS");
        audit.path = portalContext.getPath();
        Sign sign = new Sign();
        sign.path = portalContext.getPath();
        sign.timestamp = portalContext.getSignValue("timestamp");
        sign.sign = portalContext.getSignValue("sign");
        String qs = request.getQueryString();
        HttpUriRequest endReq = createRequest(url, request, portalContext, client, sign, audit);
        if (this.gateWayConfig.checkSign(portalContext.getPath())) {
            String calcSign = sign.sign(this.gateWayConfig.getSignKey());
            if (!calcSign.equals(sign.sign)) {
                return;
            }
        }
        long startTime = System.currentTimeMillis();
        CloseableHttpResponse endRes = this.httpClient.execute(endReq);
        int endStatusCode = endRes.getStatusLine().getStatusCode();
        if (endStatusCode != HttpStatus.OK.value() && endStatusCode != HttpStatus.FOUND.value()) {
            response.sendError(endRes.getStatusLine().getStatusCode(), endRes.getStatusLine().getReasonPhrase());
            return;
        }
        response.setStatus(endStatusCode);
        for (Header header : endRes.getAllHeaders()) {
            if (header.getName().equalsIgnoreCase("Transfer-Encoding")
                || header.getName().equalsIgnoreCase("Keep-Alive")
                || header.getName().equalsIgnoreCase("Connection")) {
                continue;
            }
            response.setHeader(header.getName(), header.getValue());
        }
        long endTime = System.currentTimeMillis();
        logger.info("req traceId: {}, path: {}, qs: {}, timestamp: {}, costTime: {}ms", audit.traceId, audit.path, qs, audit.time, (endTime - startTime));

        WebUtil.writeOutput(env, request, response, endRes.getEntity().getContent(), endRes.getEntity().getContentLength());
    }

    private HttpUriRequest createRequest(String url, HttpServletRequest httpReq, PortalContext portalContext, ClientInfo client, Sign sign, AuditLog audit) throws IOException {
        RequestBuilder builder = RequestBuilder.create(httpReq.getMethod());
        String qs = httpReq.getQueryString();
        if (!StrUtil.isEmpty(qs)) {
            url = url + "?" +qs;
        }
        String ctStr = httpReq.getContentType();
        if (!StrUtil.isEmpty(ctStr)) {
            builder.addHeader("Content-type", ctStr);
        }
        if (client != null) {
            String jsonClient = JSONUtil.toJsonStr(client);
            builder.addHeader(PortalContext.CLIENT_ATTR_KEY, jsonClient);
        }
        builder.addHeader("X-Referer", portalContext.getHeader("Referer"));
        builder.addHeader("X-Forwarded-Path", portalContext.getPath());
        builder.addHeader("X-Forwarded-Host", portalContext.getHeader("host"));
        builder.addHeader("User-Agent", portalContext.getHeader("user-Agent"));
        builder.addHeader("traceId", audit.traceId);
        builder.addHeader(HttpHeaders.IF_NONE_MATCH, portalContext.getHeader(HttpHeaders.IF_NONE_MATCH));
        builder.setUri(url);
        if (HttpPost.METHOD_NAME.equals(httpReq.getMethod())
                || HttpPut.METHOD_NAME.equals(httpReq.getMethod())
                || HttpPatch.METHOD_NAME.equals(httpReq.getMethod())) {
            long cl = httpReq.getContentLength();
            if (ctStr.contains("json")) {
                String reqJson = StreamUtils.copyToString(httpReq.getInputStream(), Charset.forName("UTF-8"));
                sign.data = reqJson;
                builder.setEntity(new StringEntity(reqJson, Charset.forName("UTF-8")));
            }else{
                builder.setEntity(new InputStreamEntity(httpReq.getInputStream(), cl));
            }
        }
        return builder.build();
    }

    static class Sign{
        String path;
        String qs;
        String data;
        String timestamp;
        String sign;
        String calcSign;
        String signTxt;
        String signTxt(String key, String bodyText){
            StringBuilder sb = new StringBuilder(path);
            if (StrUtil.isNotEmpty(qs)) {
                int tmpPos = qs.indexOf(timestamp);
                if (tmpPos > 1) {
                    qs = qs.substring(0, tmpPos);
                }
                tmpPos = qs.indexOf("sign");
                if (tmpPos > 1) {
                    qs = qs.substring(0, tmpPos);
                }
                sb.append(qs);
            }
            if (StrUtil.isNotEmpty(bodyText)) {
                sb.append(DigestUtil.md5Hex(bodyText));
            }
            sb.append(timestamp);
            sb.append(key);
            this.signTxt = sb.toString();
            String calcSign = DigestUtil.md5Hex(this.signTxt);
            return calcSign;
        }
        String sign(String key){
            return signTxt(key, data);
        }
    }

    static class AuditLog{
        String traceId;
        String time;
        String clientIp;
        String path;
        String userId = "";
        String ct = "";
        String qs = "";
        String req = "";
        String resp = "";
    }

}
