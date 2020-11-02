package com.hty.forum.service;

import com.hty.forum.entity.Ad;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
public interface AdService {

    //根据位置来获取广告集合
    List<Ad> listAds(String position);
}
