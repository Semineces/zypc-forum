package com.hty.forum.dto;

import lombok.Data;

/**
 * create by Semineces on 2020-10-09
 */
@Data
public class TopicQueryDto {

    private Long id;

    private String title;

    private Long talkCount;

    private Integer followCount;

    private String image;

    private String simpleDesc;

    private Long gmtCreate;

    private Long gmtModified;

    private Integer status;

    private Integer pageNo;

    private Integer pageSize;

    private String sortBy;

}
