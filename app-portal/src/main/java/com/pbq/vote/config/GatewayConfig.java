package com.pbq.vote.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {

    private boolean sign = false;
    private List<String> signExclues = new ArrayList<>();
    private String signKey = "com.pbq.vote";
    private String signMod = "TEST";
    private List<String> routes = new ArrayList<>();
    private int connTimeOut = 10000;
    private int readTimeOut = 30000;
    private int maxPerRoute = 128;
    private int maxTotal = 1024;


    public boolean checkSign(String path){
        if (!this.sign) {
            return false;
        }
        for (String p : this.signExclues) {
            if (path.matches(p)) {
                return false;
            }
        }
        return true;
    }

    public String getSignKey() {
        return signKey;
    }

    public String getSignMod() {
        return signMod;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }

    public int getConnTimeOut() {
        return connTimeOut;
    }

    public void setConnTimeOut(int connTimeOut) {
        this.connTimeOut = connTimeOut;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }
}
