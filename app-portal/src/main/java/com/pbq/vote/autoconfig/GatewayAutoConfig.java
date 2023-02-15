package com.pbq.vote.autoconfig;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.propagation.B3Propagation;
import brave.propagation.ExtraFieldPropagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.pbq.vote.config.GatewayConfig;
import com.pbq.vote.servlet.GatewayProxyServlet;
import com.pbq.vote.utils.HttpClientUtils;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;

@Configuration
public class GatewayAutoConfig {
    @Bean
    public GatewayProxyServlet proxyServlet(){
        return new GatewayProxyServlet();
    }

    @Bean
    public ServletRegistrationBean<GatewayProxyServlet> proxyServletRegistration(GatewayProxyServlet proxyServlet, GatewayConfig gatewayConfig){
        ServletRegistrationBean<GatewayProxyServlet> reg = new ServletRegistrationBean<>(proxyServlet);
        for (String route : gatewayConfig.getRoutes()) {
            reg.addUrlMappings(GatewayProxyServlet.PROXY_PREFIX + route + "/*");
        }
        return reg;
    }

    @Bean
    Tracing tracing(@Value("${spring.application.name}") String serviceName) {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "mianshigee-cma"))
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().build()
                ).build();
    }
    @Bean
    HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }

    /*@Bean
    public HttpTracing tracing(HttpTracing httpTracing){
        return HttpTracing.create(Tracing.newBuilder().build());
    }*/

    @Bean
    public HttpClient httpClient(HttpTracing httpTracing){
        return HttpClientUtils.httpClient(500, 60000, 256, 1024, httpTracing);
    }
}
