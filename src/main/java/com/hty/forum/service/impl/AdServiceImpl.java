package com.hty.forum.service.impl;

import com.hty.forum.entity.Ad;
import com.hty.forum.entity.example.AdExample;
import com.hty.forum.mapper.AdMapper;
import com.hty.forum.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
@Service
public class AdServiceImpl implements AdService {

    @Autowired
    private AdMapper adMapper;

    @Override
    public List<Ad> listAds(String position) {
        AdExample adExample = new AdExample();
        //创建一个criteria以便于后续操作
        AdExample.Criteria criteria = adExample.createCriteria();
        adExample.setOrderByClause("gmt_create desc"); //添加排序条件
        criteria.andStatusEqualTo(1); //添加状态为1，即存在
        criteria.andGmtStartLessThan(System.currentTimeMillis()); //添加开始时间小于当前时间
        criteria.andGmtEndGreaterThan(System.currentTimeMillis()); //添加结束时间大于当前时间
        criteria.andPostionEqualTo(position); //添加广告的位置
        return adMapper.selectByExample(adExample);
    }
}
