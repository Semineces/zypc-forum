package com.hty.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.hty.forum.dto.NotificationDto;
import com.hty.forum.entity.Comment;
import com.hty.forum.entity.Notification;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.NotificationExample;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.CommentMapper;
import com.hty.forum.mapper.NotificationMapper;
import com.hty.forum.mapper.QuestionMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.NotificationStatus;
import com.hty.forum.myenum.NotificationType;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.NotificationService;
import com.hty.forum.utils.DateFormateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * create by Semineces on 2020-10-07
 * 只有神知道写的是什么
 */

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Notification createNotification(long receiver, Comment comment, Integer type) {
        if (StringUtils.isEmpty(receiver) || comment == null) throw new MyException(ResultCode.NOT_OK);
        Notification notification = new Notification();
        notification.setType(type);
        notification.setStatus(NotificationStatus.UN_READ.getCode());
        notification.setOutterId(comment.getParentId());
        notification.setNotifier((long) comment.getCommentor());
        notification.setReceiver(receiver);
        notification.setGmtCreate(System.currentTimeMillis());
        return notification;
    }

    //获取通知列表
    @Override
    public List<NotificationDto> list(Integer pageNo, Integer pageSize, Integer userId) {
        NotificationExample example = new NotificationExample();
        example.createCriteria()
                .andReceiverEqualTo((long) userId)
                //通知者不能是自己，即不能自己通知自己
                .andNotifierNotEqualTo((long) userId);
        example.setOrderByClause("gmt_create desc");
        PageHelper.startPage(pageNo, pageSize);
        List<Notification> notificationList = notificationMapper.selectByExample(example);
        if (notificationList.size() > 0) {
            List<NotificationDto> notificationDtoList = new ArrayList<>();
            for (Notification notification : notificationList) {
                //如果是评论问题的通知
                if (notification.getType().equals(NotificationType.COMMENT_QUESTION.getCode())) {
                    NotificationDto<Question> notificationDto = new NotificationDto<>();
                    Long notifier = notification.getNotifier();
                    int notifierId = notifier.intValue();
                    User user = userMapper.selectByPrimaryKey(notifierId);
                    createNotificationDtoCommentQuestion(notificationDtoList, notification, notificationDto, user);
                } else if (notification.getType().equals(NotificationType.COMMENT_REPLY.getCode())) {
                    NotificationDto<Comment> notificationDto = new NotificationDto<>();
                    Long notifier = notification.getNotifier();
                    int notifierId = notifier.intValue();
                    User user = userMapper.selectByPrimaryKey(notifierId);
                    createNotificationDtoCommentReply(notificationDtoList, notification, notificationDto, user);
                } else if (notification.getType().equals(NotificationType.COMMENT_LIKE.getCode())) {
                    NotificationDto<Comment> notificationDto = new NotificationDto<>();
                    Long notifier = notification.getNotifier();
                    int notifierId = notifier.intValue();
                    User user = userMapper.selectByPrimaryKey(notifierId);
                    createNotificationDtoCommentLike(notificationDtoList, notification, notificationDto, user);
                } else if (notification.getType().equals(NotificationType.LIKE_QUESTION.getCode())) {
                    NotificationDto<Question> notificationDto = new NotificationDto<>();
                    Long notifier = notification.getNotifier();
                    int notifierId = notifier.intValue();
                    User user = userMapper.selectByPrimaryKey(notifierId);
                    createNotificationDtoQuestionLike(notificationDtoList, notification, notificationDto, user);
                } else if (notification.getType().equals(NotificationType.FOLLOWING.getCode())) {
                    NotificationDto<User> notificationDto = new NotificationDto<>();
                    Long notifier = notification.getNotifier();
                    int notifierId = notifier.intValue();
                    User user = userMapper.selectByPrimaryKey(notifierId);
                    createNotificationDtoFollowing(notificationDtoList, notification, notificationDto, user);
                }
            }
            BuildNotificationTime(notificationDtoList);
            return notificationDtoList;
        }
        return null;
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //格式化时间，以及通知是否已读
    private void BuildNotificationTime(List<NotificationDto> notificationDTOList) {
        for (NotificationDto notificationDTO : notificationDTOList) {
            Date date = new Date(notificationDTO.getGmtCreate());
            String dateString = simpleDateFormat.format(date);
            String time = DateFormateUtil.getTime(dateString);
            notificationDTO.setShowTime(time);
            if (notificationDTO.getStatus() == 0) {
                notificationDTO.setStatusMsg("已读");
                notificationDTO.setStatusClass("label label-success");
            } else {
                notificationDTO.setStatusMsg("未读");
                notificationDTO.setStatusClass("label label-danger");
            }
        }
    }

    private void createNotificationDtoCommentReply(List<NotificationDto> notificationDtoList, Notification notification, NotificationDto<Comment> notificationDto, User user) {
        notificationDto.setId(notification.getId());
        notificationDto.setNotifier(user);
        notificationDto.setStatus(notification.getStatus());
        notificationDto.setGmtCreate(notification.getGmtCreate());
        notificationDto.setCommentNotificationType(NotificationType.COMMENT_REPLY.getName());
        notificationDto.setItem(commentMapper.selectByPrimaryKey(notification.getOutterId()));
        notificationDto.setMsgTitle(commentMapper.selectByPrimaryKey(notification.getOutterId()).getContent());
        notificationDtoList.add(notificationDto);
    }

    /**
     * 封装关注的通知
     */
    private void createNotificationDtoFollowing(List<NotificationDto> notificationDtoList, Notification notification, NotificationDto<User> notificationDto, User user) {
        notificationDto.setId(notification.getId());
        notificationDto.setNotifier(user);
        notificationDto.setStatus(notification.getStatus());
        notificationDto.setGmtCreate(notification.getGmtCreate());
        notificationDto.setCommentNotificationType(NotificationType.FOLLOWING.getName());
        notificationDto.setItem(userMapper.selectByPrimaryKey(notification.getOutterId()));
        notificationDto.setMsgTitle(NotificationType.FOLLOWING_YOU.getName());
        //notificationDTO.setItem(commentMapper.selectByPrimaryKey(notification.getOutterId()));
        notificationDtoList.add(notificationDto);
    }

    /**
     * 封装评论点赞的通知
     */
    private void createNotificationDtoCommentLike(List<NotificationDto> notificationDtoList, Notification notification, NotificationDto<Comment> notificationDto, User user) {
        notificationDto.setId(notification.getId());
        notificationDto.setNotifier(user);
        notificationDto.setGmtCreate(notification.getGmtCreate());
        notificationDto.setStatus(notification.getStatus());
        notificationDto.setCommentNotificationType(NotificationType.COMMENT_LIKE.getName());
        notificationDto.setItem(commentMapper.selectByPrimaryKey(notification.getOutterId()));
        notificationDto.setMsgTitle(commentMapper.selectByPrimaryKey(notification.getOutterId()).getContent());

        notificationDtoList.add(notificationDto);
    }

    /**
     * 封装回复的通知
     */
    private void createNotificationDtoCommentQuestion(List<NotificationDto> notificationDtoList, Notification notification, NotificationDto<Question> notificationDto, User user) {
        notificationDto.setId(notification.getId());
        notificationDto.setNotifier(user);
        notificationDto.setGmtCreate(notification.getGmtCreate());
        notificationDto.setStatus(notification.getStatus());
        notificationDto.setCommentNotificationType(NotificationType.COMMENT_QUESTION.getName());
        Question question = questionMapper.selectByPrimaryKey(notification.getOutterId());
        notificationDto.setItem(question);
        notificationDto.setMsgTitle(question.getTitle());

        notificationDtoList.add(notificationDto);
    }

    /**
     * 封装回复的通知
     */
    private void createNotificationDtoQuestionLike(List<NotificationDto> notificationDtoList, Notification notification, NotificationDto<Question> notificationDto, User user) {
        notificationDto.setId(notification.getId());
        notificationDto.setNotifier(user);
        notificationDto.setGmtCreate(notification.getGmtCreate());
        notificationDto.setStatus(notification.getStatus());
        notificationDto.setCommentNotificationType(NotificationType.LIKE_QUESTION.getName());
        Question question = questionMapper.selectByPrimaryKey(notification.getOutterId());
        notificationDto.setItem(question);
        notificationDto.setMsgTitle(question.getTitle());
        notificationDtoList.add(notificationDto);
    }

}
