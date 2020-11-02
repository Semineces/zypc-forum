package com.hty.forum.dto;

import com.hty.forum.entity.User;
import lombok.Data;

/**
 * create by Semineces on 2020-10-05
 * 给前端展示的帖子详情
 */
@Data
public class QuestionDto {

    private Integer id;
    private String title;
    private String description;
    private String tag;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private Integer creator;
    private Integer category;
    private User user;
    private long gmtCreate;
    private long gmtModified;

}
