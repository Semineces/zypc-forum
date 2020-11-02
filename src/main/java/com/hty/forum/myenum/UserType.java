package com.hty.forum.myenum;

/**
 * create by Semineces on 2020-10-05
 * 登录方式的枚举
 */
public enum UserType {

    GitHub(1),
    QQ(2),
    BaiDu(3),
    PHONE(4);

    private int code;

    UserType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
