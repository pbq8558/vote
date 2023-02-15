package com.pbq.vote.utils;

import brave.http.HttpTracing;
import brave.httpclient.TracingHttpClientBuilder;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpClientUtils {
    public static CloseableHttpClient httpClient(int connTimeOut, int readTimeOut, HttpTracing httpTracing){
        return httpClient(connTimeOut, readTimeOut, 128, 1024, httpTracing);
    }

    public static CloseableHttpClient httpClient(int connTimeOut, int readTimeOut, int maxPerRoute, int maxTotal, HttpTracing httpTracing){
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(readTimeOut)
                .setSocketTimeout(readTimeOut).setConnectTimeout(3000)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setRedirectsEnabled(false)
                .build();
        HttpClientBuilder builder = null;
        if (httpTracing != null) {
            builder = TracingHttpClientBuilder.create(httpTracing);
        }else{
            builder = HttpClientBuilder.create();
        }
        builder.setDefaultRequestConfig(requestConfig);
        builder.setRetryHandler(new DefaultHttpRequestRetryHandler());
        try{
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] trustManagers = new TrustManager[1];
            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            };
            trustManagers[0] = tm;
            sslContext.init(null, trustManagers, null);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
            })).build();
        }catch (Exception e){

        }
        return builder.build();
    }
}
