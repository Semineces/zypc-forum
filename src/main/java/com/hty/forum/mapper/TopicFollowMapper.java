package com.hty.forum.mapper;

import com.hty.forum.entity.TopicFollow;
import com.hty.forum.entity.example.TopicFollowExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicFollowMapper {

    int countByExample(TopicFollowExample example);

    int deleteByExample(TopicFollowExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TopicFollow record);

    int insertSelective(TopicFollow record);

    List<TopicFollow> selectByExample(TopicFollowExample example);

    TopicFollow selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TopicFollow record, @Param("example") TopicFollowExample example);

    int updateByExample(@Param("record") TopicFollow record, @Param("example") TopicFollowExample example);

    int updateByPrimaryKeySelective(TopicFollow record);

    int updateByPrimaryKey(TopicFollow record);
}