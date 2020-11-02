package com.hty.forum.entity;

import java.io.Serializable;

public class Notification implements Serializable {
    private Integer id;

    private Long notifier;

    private Long receiver;

    private Integer type;

    private Integer outterId;

    private Long gmtCreate;

    private Integer status;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getNotifier() {
        return notifier;
    }

    public void setNotifier(Long notifier) {
        this.notifier = notifier;
    }

    public Long getReceiver() {
        return receiver;
    }

    public void setReceiver(Long receiver) {
        this.receiver = receiver;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getOutterId() {
        return outterId;
    }

    public void setOutterId(Integer outterId) {
        this.outterId = outterId;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", notifier=").append(notifier);
        sb.append(", receiver=").append(receiver);
        sb.append(", type=").append(type);
        sb.append(", outterId=").append(outterId);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}