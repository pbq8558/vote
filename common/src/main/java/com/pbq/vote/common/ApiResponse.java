package com.pbq.vote.common;

import java.util.Map;

public class ApiResponse {
    private Integer rtnCode;
    private boolean succ;
    private String errCode;
    private String tipCode;
    private String errMsg;
    private String tipMsg;
    private Object data;

    public static ApiResponse succ(Object obj){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSucc(true);
        apiResponse.setRtnCode(200);
        apiResponse.setTipCode("T000001");
        apiResponse.setData(obj);
        return apiResponse;
    }

    public static ApiResponse succ(String tipCode, Object obj){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSucc(true);
        apiResponse.setRtnCode(200);
        apiResponse.setTipCode(tipCode);
        apiResponse.setData(obj);
        return apiResponse;
    }

    public static ApiResponse error(Integer rtnCode, String errCode) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSucc(false);
        apiResponse.setRtnCode(rtnCode);
        apiResponse.setErrCode(errCode);
        return apiResponse;
    }

    public static ApiResponse error(String errCode) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSucc(false);
        apiResponse.setErrCode(errCode);
        return apiResponse;
    }

    public Integer getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(Integer rtnCode) {
        this.rtnCode = rtnCode;
    }

    public boolean isSucc() {
        return succ;
    }

    public void setSucc(boolean succ) {
        this.succ = succ;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTipCode() {
        return tipCode;
    }

    public void setTipCode(String tipCode) {
        this.tipCode = tipCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getTipMsg() {
        return tipMsg;
    }

    public void setTipMsg(String tipMsg) {
        this.tipMsg = tipMsg;
    }
}
