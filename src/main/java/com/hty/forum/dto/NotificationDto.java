package com.hty.forum.dto;

import com.hty.forum.entity.User;
import lombok.Data;

/**
 * create by Semineces on 2020-10-08
 */
@Data
public class NotificationDto<T> {

    //通知的id
    private Integer id;

    //通知人的姓名
    private User notifier;

    //通知的类型
    private String commentNotificationType;

    //外键信息
    private T item;

    //状态
    private Integer status;

    //通知时间
    private String showTime;

    //通知内容？
    private String statusMsg;

    //通知标题
    private String msgTitle;

    private String statusClass;

    private long gmtCreate;
}
