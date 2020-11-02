package com.hty.forum.dto;

import lombok.Data;

/**
 * create by Semineces on 2020-10-07
 */
@Data
public class NewUserDto {

    //帖子数
    private Integer questionCount;

    //点赞人数
    private Integer likeCount;

    //粉丝数
    private Integer fansCount;

    //注册时间？
    private Integer time;
}
