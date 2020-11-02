package com.hty.forum.controller.web;

import com.hty.forum.entity.User;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.UserService;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * create by Semineces on 2020-10-10
 */
@Controller
public class SignController {

    @Autowired
    private UserService userService;

    @ResponseBody
    @GetMapping("/sigIned")
    public R isSigned(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) return new R().okOf().addMsg("sigined", 0);
        if (userService.isSigned(user.getId())) return new R().okOf().addMsg("sigined", "1");
        return new R().okOf().addMsg("sigined", 0);
    }

    @ResponseBody
    @GetMapping("/sigIn")
    public R signIn(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        return userService.signIn(user.getId());
    }
}
