package com.hty.forum.myenum;

/**
 * create by Semineces on 2020-10-05
 * 获取积分的状态
 */
public enum IntegralType {

    SIGN_IN(1,"签到"),
    COMMENT(2,"被评论"),
    LIKE(1,"被点赞" );

    private long val;
    private String message;

    IntegralType(int val, String message) {
        this.val = val;
        this.message = message;
    }

    public long getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
