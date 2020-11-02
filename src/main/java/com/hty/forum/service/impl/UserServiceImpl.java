package com.hty.forum.service.impl;

import com.hty.forum.dto.NewUserDto;
import com.hty.forum.entity.Follow;
import com.hty.forum.entity.Integral;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.FollowExample;
import com.hty.forum.entity.example.IntegralExample;
import com.hty.forum.entity.example.QuestionExample;
import com.hty.forum.entity.example.UserExample;
import com.hty.forum.mapper.FollowMapper;
import com.hty.forum.mapper.IntegralMapper;
import com.hty.forum.mapper.QuestionMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.FollowStatus;
import com.hty.forum.myenum.IntegralType;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.UserService;
import com.hty.forum.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * create by Semineces on 2020-10-07
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IntegralMapper integralMapper;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    //最新注册的
    @Override
    public List<NewUserDto> findNewUsers(Integer count) {
        List<User> userList = userMapper.selectNewUserList(count);
        List<NewUserDto> newUserDtos = new ArrayList<>();
        if (userList != null) {
            for (User user : userList) {
                NewUserDto newUserDto = new NewUserDto();
                newUserDto.setFansCount(userMapper.getFansCount(user.getId()));
                BeanUtils.copyProperties(user, newUserDto);
                QuestionExample example = new QuestionExample();
                QuestionExample.Criteria criteria = example.createCriteria();
                criteria.andCreatorEqualTo(user.getId());
                newUserDto.setQuestionCount(questionMapper.countByExample(example));
            }
        }
        return newUserDtos;
    }

    //获取关注列表
    @Override
    public List<User> getFollowList(User user) {
        List<User> userList = new ArrayList<>();
        FollowExample example = new FollowExample();
        FollowExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(FollowStatus.FOLLOWED.getVal());
        criteria.andUserIdEqualTo(user.getId());
        List<Follow> follows = followMapper.selectByExample(example);
        if (follows.size() > 0) {
            for (Follow follow : follows) {
                Integer followedUserId = follow.getFollowedUser();
                User followedUser = userMapper.selectByPrimaryKey(followedUserId);
                userList.add(followedUser);
            }
        }
        return userList;
    }

    //获取粉丝列表
    @Override
    public List<User> getFansList(User user) {
        if (user != null) {
            List<Integer> fansIds = userMapper.getFansIds(user.getId());
            if (fansIds.size() > 0) {
                UserExample example = new UserExample();
                example.createCriteria().andIdIn(fansIds);
                return userMapper.selectByExample(example);
            }
        }
        return null;
    }

    @Override
    public User findUserByToken(String token) {
        UserExample example = new UserExample();
        example.createCriteria().andTokenEqualTo(token);
        List<User> userList = userMapper.selectByExample(example);
        return userList.size() > 0 ? userList.get(0) : null;
    }

    //签到得积分
    @Override
    public boolean isSigned(Integer userId) {
        String day = simpleDateFormat.format(new Date());
        String key = "signId:" + day;
        return redisTemplate.opsForSet().isMember(key, userId.toString());
    }

    @Override
    public R signIn(Integer userId) {
        if (userId == null) return new R().errorOf(ResultCode.USER_ID_EMPTY);
        String day = simpleDateFormat.format(new Date());
        String key = "signId:" + day;
        //从Redis中找看是否已经签过到
        //该方法就是从一set里面通过key来获取value，看看是否有该记录
        boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (isMember) return new R().errorOf(ResultCode.ALREADY_SIGN_IN);
        redisTemplate.opsForSet().add(key, userId.toString());
        //设置该key的过期时间
        redisTemplate.expire(key, getRefreshTime(), TimeUnit.SECONDS);
        addPointsRecord(userId);
        return new R().okOf();
    }

    //签到得积分
    private void addPointsRecord(Integer userId) {
        IntegralExample example = new IntegralExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<Integral> integrals = integralMapper.selectByExample(example);
        if (integrals.size() > 0) {
            Integral integral = integrals.get(0);
            integral.setIntegral(integral.getIntegral() + IntegralType.SIGN_IN.getVal());
            integralMapper.updateByPrimaryKeySelective(integral);
        } else {
            Integral integral = new Integral();
            integral.setIntegral(IntegralType.SIGN_IN.getVal());
            integral.setUserId(userId);
            integral.setGmtCreate(System.currentTimeMillis());
            integral.setGmtModified(System.currentTimeMillis());
            integralMapper.insertSelective(integral);
        }
    }

    //获取当前时间离明天凌晨还有多久，为了清除redis里的key
    private static long getRefreshTime() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis() / 1000;
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis() / 1000 - now;
    }
}
