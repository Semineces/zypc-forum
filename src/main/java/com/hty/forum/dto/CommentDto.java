package com.hty.forum.dto;

import com.hty.forum.entity.User;
import lombok.Data;

/**
 * create by Semineces on 2020-10-05
 * 用于封装前端评论数据的
 */
@Data
public class CommentDto {

    private Integer id;

    private Integer parentId; //该评论的父id

    private String content; //评论内容

    private int type; //

    private User user; //评论人

    private Integer likeCount; //点赞数

    private Integer commentCount; //评论数

    private String showTime; //评论时间

    private Long gmtCreate;

    private Long gmtModified;

}
