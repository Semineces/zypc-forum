package com.hty.forum.controller.web;

import com.github.pagehelper.PageInfo;
import com.hty.forum.dto.NotificationDto;
import com.hty.forum.dto.PeopleDetailsInfo;
import com.hty.forum.entity.Comment;
import com.hty.forum.entity.Notification;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.NotificationExample;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.CommentMapper;
import com.hty.forum.mapper.NotificationMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.NotificationStatus;
import com.hty.forum.myenum.NotificationType;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.NotificationService;
import com.hty.forum.service.QuestionService;
import com.hty.forum.service.UserService;
import com.hty.forum.utils.R;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * create by Semineces on 2020-10-08
 */
@Controller
public class ProfileController {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * 我的主页
     * @param action 看具体是哪个操作
     * @param map
     * @param request
     * @return
     */
    @GetMapping("/profile")
    public String toProfile(@RequestParam(value = "action", required = false) String action,
                            Map<String, Object> map,
                            HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return "redirect:/";
        }
        if ("relies".equals(action)) {
            map.put("action", action);
        }
        //获取个人数据信息
        PeopleDetailsInfo peopleDetailsInfo = new PeopleDetailsInfo();
        peopleDetailsInfo.setFanCount(userMapper.getFansCount(currentUser.getId()));
        peopleDetailsInfo.setFollowCount(userMapper.getFollowCount(currentUser.getId()));
        peopleDetailsInfo.setIntegral(userMapper.getIntegral(currentUser.getId()));
        peopleDetailsInfo.setQuestionCount(userMapper.getQuestionCount(currentUser.getId()));
        peopleDetailsInfo.setCollectCount(userMapper.getCollectCount(currentUser.getId()));
        map.put("peopleDetails", peopleDetailsInfo);
        map.put("people", currentUser);
        return "profile";
    }

    /**
     * 加载我的通知
     * @param pageSize
     * @param pageNo
     * @param request
     * TODO：其实这些加载的都可以写成一个方法，前端传来一个action作为标识，然后后端返回数据即可。接口就是/userID/load/{action}
     * @return
     */
    @GetMapping("/loadMyReplies")
    @ResponseBody
    public R loadMyReplies(@RequestParam(value = "pageSize", defaultValue = "15") Integer pageSize,
                           @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                           HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        List<NotificationDto> notificationDtos = notificationService.list(pageNo, pageSize, currentUser.getId());
        return new R().okOf().addMsg("notification", notificationDtos);
    }

    /**
     * 加载我的关注
     * @param request
     * @return
     */
    @GetMapping("/loadMyFollows")
    @ResponseBody
    public R loadMyFollows(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        List<User> followList = userService.getFollowList(currentUser);
        return new R().okOf().addMsg("userList", followList);
    }


    /**
     * 加载我的粉丝
     * @param request
     * @return
     */
    @GetMapping("/loadPeopleFans")
    @ResponseBody
    public R loadPeopleFans(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        List<User> fans = userService.getFansList(currentUser);
        return new R().okOf().addMsg("fans", fans);
    }

    /**
     * 查看我的帖子或者收藏
     * @param action 帖子还是收藏
     * @param pageSize
     * @param pageNo
     * @param map
     * @param request
     * @return
     */
    @GetMapping("/loadPeopleData/{action}")
    @ResponseBody
    public R loadPeopleData(@PathVariable("action") String action,
                            @RequestParam(value = "pageSize", defaultValue = "15") Integer pageSize,
                            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                            Map<String, Object> map,
                            HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        if ("questions".equals(action)) {
            PageInfo<Question> questionPage =
                    questionService.getQuestionByUserId(pageNo, pageSize, currentUser.getId());
            return new R().okOf().addMsg("page", questionPage);
        }
        if ("collects".equals(action)) {
            PageInfo<Question> collectPage =
                    questionService.getCollectPage(pageNo, pageSize, currentUser.getId());
            return new R().okOf().addMsg("page", collectPage);
        }
        return new R().errorOf(ResultCode.PAGE_NOT_FOUNT);
    }

    /**
     * 查看最新通知
     * @param notificationId 通知id
     * @param isRead 是否已读
     * @param request
     * @return
     */
    @GetMapping("/read")
    public String getNotificationDetail(@RequestParam("id") Integer notificationId,
                                        @RequestParam("status") Integer isRead,
                                        HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) throw new MyException(ResultCode.USER_NO_LOGIN);
        Notification notification = notificationMapper.selectByPrimaryKey(notificationId);
        if (!isRead.equals(NotificationStatus.READ.getCode())) {
            //修改通知为已读
            notification.setStatus(NotificationStatus.READ.getCode());
            notificationMapper.updateByPrimaryKeySelective(notification);
            //获取未读信息
            NotificationExample notificationExample = new NotificationExample();
            NotificationExample.Criteria criteria = notificationExample.createCriteria();
            criteria.andReceiverEqualTo((long) currentUser.getId());
            criteria.andStatusEqualTo(NotificationStatus.UN_READ.getCode());
            Integer count = notificationMapper.countByExample(notificationExample);
            request.getSession().setAttribute("unreadcount", count);
        }
        //找到关联的页面 帖子还是关注
        Integer type = notification.getType();
        //帖子的页面
        if (type.equals(NotificationType.COMMENT_QUESTION.getCode()) ||
                type.equals(NotificationType.LIKE_QUESTION.getCode())) {
            if (notification.getOutterId() == null) {
                throw new MyException(ResultCode.QUESTION_NOT_FOUND);
            }
            return "redirect:/question/" + notification.getOutterId();
        } else if (type.equals(NotificationType.COMMENT_REPLY.getCode()) ||
                type.equals(NotificationType.COMMENT_LIKE.getCode())) {
            Comment comment = commentMapper.selectByPrimaryKey(notification.getOutterId());
            if (comment == null) {
                throw new MyException(ResultCode.COMMENT_NOT_FOUNT);
            }
            Integer questionId = comment.getParentId();
            return "redirect:/question/" + questionId;
        } else if (type.equals(NotificationType.FOLLOWING_YOU.getCode())) {
            return "redirect:/people?id=" + notification.getOutterId();
        }
        return "error";
    }

    /**
     * 删除通知
     * @param id
     * @return
     */
    @ResponseBody
    @GetMapping("/deleteNotification")
    public R deleteNotification(@RequestParam("id") Integer id) {
        int result = notificationMapper.deleteByPrimaryKey(id);
        return result > 0 ? new R().okOf() : new R().errorOf(ResultCode.DELETE_NOTIFICATION_ERROR);
    }

    /**
     * 删除已读通知
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping
    public R ajaxDeleteRead(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) throw new MyException(ResultCode.USER_NO_LOGIN);
        NotificationExample notificationExample = new NotificationExample();
        NotificationExample.Criteria criteria = notificationExample.createCriteria();
        criteria.andReceiverEqualTo((long) currentUser.getId());
        criteria.andStatusEqualTo(NotificationStatus.READ.getCode());
        notificationMapper.deleteByExample(notificationExample);
        return new R().okOf();
    }

    /**
     * 读取全部通知
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/readAll")
    public R readAll(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) throw new MyException(ResultCode.USER_NO_LOGIN);
        userMapper.readAllNotification(currentUser.getId());
        return new R().okOf();
    }

}
