package com.hty.forum.entity;

import java.io.Serializable;

public class Ad implements Serializable {
    private Integer id;

    private String title;

    private String url;

    private String image;

    private Long gmtCreate;

    private Long gmtModified;

    private Long gmtStart;

    private Long gmtEnd;

    private Integer status;

    private String postion;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Long getGmtStart() {
        return gmtStart;
    }

    public void setGmtStart(Long gmtStart) {
        this.gmtStart = gmtStart;
    }

    public Long getGmtEnd() {
        return gmtEnd;
    }

    public void setGmtEnd(Long gmtEnd) {
        this.gmtEnd = gmtEnd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPostion() {
        return postion;
    }

    public void setPostion(String postion) {
        this.postion = postion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", url=").append(url);
        sb.append(", image=").append(image);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModified=").append(gmtModified);
        sb.append(", gmtStart=").append(gmtStart);
        sb.append(", gmtEnd=").append(gmtEnd);
        sb.append(", status=").append(status);
        sb.append(", postion=").append(postion);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}