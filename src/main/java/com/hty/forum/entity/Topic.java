package com.hty.forum.entity;

import lombok.Data;

@Data
public class Topic {

    private Long id;

    private String title;

    private Long talkCount;

    private Integer followCount;

    private String image;

    private String simpleDesc;

    private Long gmtCreate;

    private Long gmtModified;

    private Integer status;

}