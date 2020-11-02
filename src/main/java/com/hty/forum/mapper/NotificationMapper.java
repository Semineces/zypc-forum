package com.hty.forum.mapper;

import com.hty.forum.entity.Notification;
import com.hty.forum.entity.example.NotificationExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationMapper {
    int countByExample(NotificationExample example);

    int deleteByExample(NotificationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Notification record);

    int insertSelective(Notification record);

    List<Notification> selectByExample(NotificationExample example);

    Notification selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Notification record, @Param("example") NotificationExample example);

    int updateByExample(@Param("record") Notification record, @Param("example") NotificationExample example);

    int updateByPrimaryKeySelective(Notification record);

    int updateByPrimaryKey(Notification record);
}