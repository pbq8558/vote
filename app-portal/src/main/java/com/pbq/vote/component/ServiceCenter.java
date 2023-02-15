package com.pbq.vote.component;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ServiceCenter {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired
    NacosServiceManager nacosServiceManager;

    @Autowired
    Environment env;

    public String findOneService(String serviceId){
        return findOneService(serviceId, nacosDiscoveryProperties.getGroup());
    }

    public String findOneService(String serviceId, String group){
        return findOneService(serviceId, group, "http");
    }

    public String findOneService(String serviceId, String group, String schema){
        String proxy = env.getProperty("proxy." + serviceId + "." + group);
        if (StrUtil.isEmpty(proxy)) {
            proxy = env.getProperty("proxy." + serviceId);
        }
        if (StrUtil.isNotEmpty(proxy)) {
            String[] proxyIns = proxy.split(",|;");
            if (proxyIns.length == 1) {
                return proxy;
            }else{
                Chooser<String, String> chooser = new Chooser<>("proxy." + serviceId + "." + group);
                List<Pair<String>> hostWithWeight = new ArrayList<>();
                for (String ins : proxyIns) {
                    hostWithWeight.add(new Pair<>(ins, 1));
                }
                chooser.refresh(hostWithWeight);
                return chooser.random();
            }
        }else{
            Instance ins = findOneIns(serviceId, group);
            if (StrUtil.isNotBlank(schema)) {
                return schema + "://" + ins.getIp() + ":" + ins.getPort();
            }else{
                return ins.getIp() + ":" + ins.getPort();
            }
        }
    }

    private Instance findOneIns(String serviceId, String group){
        List<Instance> hosts = findInsList(serviceId, group);
        if (hosts.isEmpty()) {
            throw new RuntimeException("Not Found Instance with : " + serviceId + "@@" + group);
        }
        List<Pair<Instance>> hostWithWeight = new ArrayList<>(hosts.size());
        for (Instance host : hosts) {
            if (host.isHealthy()) {
                hostWithWeight.add(new Pair<>(host, host.getWeight()));
            }
        }
        Chooser<String, Instance> vipChooser = new Chooser<>("www.taobao.com");
        vipChooser.refresh(hostWithWeight);
        return vipChooser.randomWithWeight();
    }

    private List<Instance> findInsList(String serviceId, String group){
        List<Instance> list = new ArrayList<>();
        try{
            list = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties()).selectInstances(
                    serviceId, group, Arrays.asList(nacosDiscoveryProperties.getClusterName()), true, false
            );
            if (list.isEmpty()) {
                logger.info("founc empty list instance {}@@{} with cluster <{}>", serviceId, group, nacosDiscoveryProperties.getClusterName());
                list = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties()).selectInstances(serviceId, group, new ArrayList<>(), true, false);
            }
            return list;
        }catch (NacosException e){
            throw new RuntimeException(e);
        }
    }

}
