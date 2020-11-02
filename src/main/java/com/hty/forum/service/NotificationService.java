package com.hty.forum.service;

import com.hty.forum.dto.NotificationDto;
import com.hty.forum.entity.Comment;
import com.hty.forum.entity.Notification;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
public interface NotificationService {

    //创建通知
    Notification createNotification(long receiver, Comment comment, Integer type);

    //TODO
    List<NotificationDto> list(Integer pageNo, Integer pageSize, Integer id);
}
