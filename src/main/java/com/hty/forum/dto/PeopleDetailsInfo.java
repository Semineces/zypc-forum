package com.hty.forum.dto;

import lombok.Data;

/**
 * create by Semineces on 2020-10-08
 */
@Data
public class PeopleDetailsInfo {

    //粉丝数
    private Integer fanCount;

    //帖子数
    private Integer questionCount;

    //关注的人数
    private Integer followCount;

    //积分
    private Long integral;

    //收藏数量
    private Integer collectCount;


}
