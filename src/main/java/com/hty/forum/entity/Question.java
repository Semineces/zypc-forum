package com.hty.forum.entity;

import lombok.Data;

@Data
public class Question {

    private Integer id;

    private String title;

    private Long gmtCreate;

    private Long gmtModified;

    private Integer commentCount;

    private Integer viewCount;

    private Integer likeCount;

    private String tag;

    private Integer creator;

    private Integer category;

    private Integer topic;

    private Integer top;

    private String description;

    //格式化后的时间
    private String showTime;

    private String typeName;

    private User user;
}