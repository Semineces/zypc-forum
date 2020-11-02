package com.hty.forum.utils;

import com.hty.forum.myenum.QuestionCodeEnum;
import com.hty.forum.myenum.ResultCode;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * create by Semineces on 2020-10-05
 * 统一结果封装Json类
 */
@Data
public class R {

    private String code;

    private String message;

    private Map<String,Object> extend = new HashMap<>();

    public R errorOf(ResultCode resultCode) {
        R r = new R();
        r.setCode(resultCode.getCode());
        r.setMessage(resultCode.getMessage());
        return r;
    }

    public R errorOf(String message) {
        R r = new R();
        r.setCode("-1");
        r.setMessage(message);
        return r;
    }

    public R errorOf(QuestionCodeEnum error) {
        R r = new R();
        r.setCode(error.getCode());
        r.setMessage(error.getMsg());
        return r;
    }

    public R okOf(String code, String message) {
        R r = new R();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public R okOf(){
        R r = new R();
        r.setMessage(ResultCode.OK.getMessage());
        r.setCode(ResultCode.OK.getCode());
        return r;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }

    public R addMsg(String key, Object value){
        this.extend.put(key,value);
        return this;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "R{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", extend=" + extend +
                '}';
    }


}