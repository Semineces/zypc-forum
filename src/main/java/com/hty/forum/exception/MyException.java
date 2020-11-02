package com.hty.forum.exception;

import com.hty.forum.myenum.ResultCode;

/**
 * create by Semineces on 2020-10-05
 */
public class MyException extends RuntimeException {

    @Override
    public String getMessage() {
        return super.getMessage();
    }
    public MyException(ResultCode code) {
        super(code.getMessage());
    }
}
