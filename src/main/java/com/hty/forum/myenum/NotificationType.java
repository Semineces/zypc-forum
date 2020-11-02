package com.hty.forum.myenum;

/**
 * create by Semineces on 2020-10-05
 * 通知的具体情况
 */
public enum NotificationType {

    COMMENT_QUESTION(1,"评论了你的问题"),
    COMMENT_LIKE(3,"点赞了你的评论"),
    COMMENT_REPLY(2,"回复了你的评论"),
    LIKE_QUESTION(4,"点赞了问题"),
    FOLLOWING(5,"关注了"),
    FOLLOWING_YOU(5,"你")
    ;

    private Integer code;
    private String name;

    NotificationType(Integer code, String name) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
