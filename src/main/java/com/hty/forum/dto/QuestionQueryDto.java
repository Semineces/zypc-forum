package com.hty.forum.dto;

import lombok.Data;

/**
 * create by Semineces on 2020-10-05
 */
@Data
public class QuestionQueryDto {

    private String tag;
    private String search; //关键字
    private long beginTime;
    private long endTime;
    private Integer category;
    private Integer pageNo;
    private Integer pageSize;
    private String sort; //排序
}
