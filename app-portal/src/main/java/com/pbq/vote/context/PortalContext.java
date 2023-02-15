package com.pbq.vote.context;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class PortalContext {

    public static final String CLIENT_KEY = "x-client-id";
    public static final String SESSION_KEY = "x-session-id";
    public static final String CLIENT_ATTR_KEY = "x-client";
    public static final String SESSION_ATTR_KEY = "x-session";
    public static final String PORTAL_CTX_ATTR_KEY = "x-portal-ctx";
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    protected Map<String, String> headerMap = new HashMap<>();
    protected Map<String, String> cookieMap = new HashMap<>();

    public PortalContext(HttpServletRequest httpRequest, HttpServletResponse httpResponse){
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        for (Enumeration<String> he = this.httpRequest.getHeaderNames(); he.hasMoreElements();) {
            String key = he.nextElement();
            if (!key.toLowerCase().startsWith("sec-")) {
                this.headerMap.put(key.toLowerCase(), this.httpRequest.getHeader(key));
            }
        }
        if (this.httpRequest.getCookies() != null) {
            for (Cookie ck : this.httpRequest.getCookies()) {
                this.cookieMap.put(ck.getName(), ck.getValue());
            }
        }
    }
    public String getCookieValue(String name){
        return this.cookieMap.get(name);
    }

    public String getSignValue(String key){
        String val = getHeader(key);
        if (StrUtil.isEmptyOrUndefined(val)) {
            val = httpRequest.getParameter(key);
        }
        return val;
    }

    public String getHeader(String key){
        return this.headerMap.get(key.toLowerCase());
    }
    public String getClientId(){
        String clientId = headerMap.get(CLIENT_KEY);
        if (StrUtil.isNotBlank(clientId)) {
            return clientId;
        }
        return null;
    }

    public String getUserAgent(){
        return getHeader("user-agent");
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }
    public void setNewClientId(String clientId){
        this.httpResponse.setHeader(CLIENT_KEY, clientId);
    }

    public String getPath(){
        return this.httpRequest.getRequestURI();
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    public String getSessionId(){
        String sessionId = headerMap.get(SESSION_KEY);
        if (StrUtil.isNotBlank(sessionId)) {
            return sessionId;
        }
        sessionId = this.httpRequest.getParameter(SESSION_KEY);
        if (StrUtil.isNotBlank(sessionId)) {
            return sessionId;
        }
        return null;
    }
}
