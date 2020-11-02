package com.hty.forum.interceptor;

import com.hty.forum.entity.Notification;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.NotificationExample;
import com.hty.forum.mapper.NotificationMapper;
import com.hty.forum.myenum.AdType;
import com.hty.forum.myenum.NotificationStatus;
import com.hty.forum.service.AdService;
import com.hty.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * create by Semineces on 2020-10-09
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private AdService adService;

    //TODO：该interceptor完全可以再写controller那么CRUD
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //加入广告
        for (AdType adType : AdType.values()) {
            request.getSession().setAttribute(adType.name(), adService.listAds(adType.name()));
        }
        //用户登录的cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    User user = userService.findUserByToken(token);
                    if (user != null) {
                        request.getSession().setAttribute("user", user);
                        break;
                    }
                }
            }
        }
        //获取未读消息数量
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            //获取未读信息
            NotificationExample example = new NotificationExample();
            example.createCriteria()
                    .andReceiverEqualTo((long) user.getId())
                    .andStatusEqualTo(NotificationStatus.UN_READ.getCode());
            Integer count = notificationMapper.countByExample(example);
            request.getSession().setAttribute("unreadcount", count);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
