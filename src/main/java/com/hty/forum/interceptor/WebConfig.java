package com.hty.forum.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * create by Semineces on 2020-10-10
 * 配置登录和发布的拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SessionInterceptor sessionInterceptor;

    @Autowired
    private PublishInterceptor publishInterceptor;

    @Autowired
    private AdminLoginInterceptor adminLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**").order(2); //order可能是设置排序方式

        registry.addInterceptor(publishInterceptor)
                .addPathPatterns("/publish/**").order(3);

        //后台登录的拦截器，设置拦截所有的admin，但是放行login和doLogin
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/doLogin").order(1);
    }


}
