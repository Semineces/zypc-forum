package com.hty.forum.myenum;

/**
 * create by Semineces on 2020-10-05
 */
public enum NotificationStatus {

    READ(0, "已读"),
    UN_READ(1, "未读")
    ;

    private String status;
    private Integer code;

    NotificationStatus(Integer codes, String status) {
        this.status = status;
        this.code = codes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer codes) {
        this.code = codes;
    }
}
