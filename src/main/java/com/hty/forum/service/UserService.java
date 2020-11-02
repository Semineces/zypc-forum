package com.hty.forum.service;

import com.hty.forum.dto.NewUserDto;
import com.hty.forum.entity.User;
import com.hty.forum.utils.R;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
public interface UserService {

    //查询新注册的用户
    List<NewUserDto> findNewUsers(Integer count);

    List<User> getFollowList(User user);

    List<User> getFansList(User user);

    //通过token查询当前登录的用户
    User findUserByToken(String token);

    boolean isSigned(Integer id);

    R signIn(Integer id);
}
