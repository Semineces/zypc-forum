package com.hty.forum.controller.web;

import com.hty.forum.entity.Follow;
import com.hty.forum.entity.Notification;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.FollowExample;
import com.hty.forum.mapper.FollowMapper;
import com.hty.forum.mapper.NotificationMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.FollowStatus;
import com.hty.forum.myenum.NotificationStatus;
import com.hty.forum.myenum.NotificationType;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * create by Semineces on 2020-10-07
 */
@Controller
public class FollowController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 验证是否已经关注
     * @param followUserId 关注人的id
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/isFollowed")
    public R isFollowed(@RequestParam("id") Integer followUserId, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.FOLLOW_NEED_LOGIN);
        FollowExample example = new FollowExample();
        FollowExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(currentUser.getId());
        criteria.andFollowedUserEqualTo(followUserId);
        List<Follow> follows = followMapper.selectByExample(example);
        if (follows.size() > 0) {
            return new R().okOf().addMsg("status", follows.get(0).getStatus());
        }
        return new R().okOf();
    }

    /**
     * 执行关注操作
     * @param followUserId 关注人的id
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/follow")
    public R follow(@RequestParam("id") Integer followUserId, HttpServletRequest request) {
        //查看当前用户是否已经登录
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.FOLLOW_NEED_LOGIN);
        //查看要关注的用户是否存在
        User followUser = userMapper.selectByPrimaryKey(followUserId);
        if (followUser == null) return new R().errorOf(ResultCode.USER_NOT_FOUND);
        //进行关注逻辑
        FollowExample example = new FollowExample();
        FollowExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(currentUser.getId());
        criteria.andFollowedUserEqualTo(followUserId);
        List<Follow> follows = followMapper.selectByExample(example);
        //在关注表中已经有该记录
        if (follows.size() > 0) {
            Follow follow = follows.get(0);
            //再执行一次判断看是否是取消关注（数据库中Status字段就是是否关注0否 1是，如果是0表明是之前取消关注的）
            if (follow.getStatus().equals(FollowStatus.UN_FOLLOWED.getVal())) {
                follow.setStatus(FollowStatus.FOLLOWED.getVal());
                followMapper.updateByPrimaryKeySelective(follow);
                return new R().okOf();
            }
        } else {
            //插入新纪录
            Follow follow = new Follow();
            follow.setUserId(currentUser.getId());
            follow.setFollowedUser(followUser.getId());
            follow.setGmtCreate(System.currentTimeMillis());
            follow.setStatus(FollowStatus.FOLLOWED.getVal());
            followMapper.insertSelective(follow);
            //发送通知
            Notification notification = new Notification();
            notification.setNotifier((long) currentUser.getId());
            notification.setReceiver((long) followUser.getId());
            notification.setStatus(NotificationStatus.UN_READ.getCode());
            notification.setType(NotificationType.FOLLOWING.getCode());
            notification.setGmtCreate(System.currentTimeMillis());
            notification.setOutterId(currentUser.getId());
            notificationMapper.insertSelective(notification);
        }
        return new R().okOf();
    }

    /**
     * 取消关注
     * @param followUserId 关注人的id
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/deleteFollow")
    public R deleteFollow(@RequestParam("id") Integer followUserId, HttpServletRequest request) {
        //查看当前用户是否已经登录
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.FOLLOW_NEED_LOGIN);
        //查看要关注的用户是否存在
        User followUser = userMapper.selectByPrimaryKey(followUserId);
        if (followUser == null) return new R().errorOf(ResultCode.USER_NOT_FOUND);
        //执行取消关注逻辑，即把数据库中的表中status字段设置为0
        FollowExample example = new FollowExample();
        FollowExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(currentUser.getId());
        criteria.andStatusEqualTo(FollowStatus.FOLLOWED.getVal());
        criteria.andFollowedUserEqualTo(followUser.getId());
        List<Follow> follows = followMapper.selectByExample(example);
        if (follows.size() > 0) {
            Follow follow = follows.get(0);
            follow.setStatus(FollowStatus.UN_FOLLOWED.getVal());
            followMapper.updateByPrimaryKeySelective(follow);
        } else return new R().errorOf(ResultCode.NOT_OK);
        return new R().okOf();
    }

    /**
     * 查询关注的人
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/findMyFollow")
    public R findMyFollow(HttpServletRequest request) {
        //查看当前用户是否已经登录
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.FOLLOW_NEED_LOGIN);
        List<User> followUserList = new ArrayList<>();
        FollowExample example = new FollowExample();
        FollowExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(FollowStatus.FOLLOWED.getVal());
        criteria.andUserIdEqualTo(currentUser.getId());
        List<Follow> follows = followMapper.selectByExample(example);
        if (follows != null && follows.size() > 0) {
            for (Follow follow : follows) {
                Integer followedUser = follow.getFollowedUser();
                User user = userMapper.selectByPrimaryKey(followedUser);
                followUserList.add(user);
            }
        }
        return new R().okOf().addMsg("follows", followUserList);
    }

}
