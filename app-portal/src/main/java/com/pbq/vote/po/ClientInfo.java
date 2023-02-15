package com.pbq.vote.po;

public class ClientInfo {
    private String id;
    private String agentId;
    private String realAgentId;
    private String clientId;
    private String platCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getRealAgentId() {
        return realAgentId;
    }

    public void setRealAgentId(String realAgentId) {
        this.realAgentId = realAgentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPlatCode() {
        return platCode;
    }

    public void setPlatCode(String platCode) {
        this.platCode = platCode;
    }
    public ClientInfo(String id, String agentId, String realAgentId) {
        this.id = id;
        this.agentId = agentId;
        this.realAgentId = realAgentId;
    }
}
