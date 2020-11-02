package com.hty.forum.service;

import com.hty.forum.entity.Topic;
import com.hty.forum.entity.TopicFollow;
import com.hty.forum.utils.R;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
public interface TopicService {

    List<Topic> listAllTopic();

    List<Topic> listRelatedTopics(Integer id);

    R saveOrUpdate(TopicFollow topicFollow);

    R unFollowTopic(TopicFollow topicFollow);
}
