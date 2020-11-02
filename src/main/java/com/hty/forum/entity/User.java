package com.hty.forum.entity;

import lombok.Data;

@Data
public class User {

    private Integer id;

    private String accountId;

    private String name;

    private String token;

    private Long gmtCreate;

    private Long gmtModified;

    private String avatarUrl;

    private String bio;

    private String location;

    private String company;

    private Integer userType;

    private String password;

    private Integer rank;

}