package com.hty.forum.mapper;

import com.hty.forum.entity.Topic;
import com.hty.forum.entity.TopicFollow;
import com.hty.forum.entity.example.TopicExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicMapper {

    int countByExample(TopicExample example);

    int deleteByExample(TopicExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Topic record);

    int insertSelective(Topic record);

    List<Topic> selectByExample(TopicExample example);

    Topic selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Topic record, @Param("example") TopicExample example);

    int updateByExample(@Param("record") Topic record, @Param("example") TopicExample example);

    int updateByPrimaryKeySelective(Topic record);

    int updateByPrimaryKey(Topic record);

    //增加话题数量
    void increaseTalkCount(@Param("id") Integer topic);

    //减少关注数量
    void decreaseTopicFollowCount(@Param("id") Integer topicId);

    //获取话题关注数量
    Integer getTopicFollowCountById(TopicFollow topicFollow);
}