package com.hty.forum.service.impl;

import com.hty.forum.entity.Question;
import com.hty.forum.entity.Topic;
import com.hty.forum.entity.TopicFollow;
import com.hty.forum.entity.example.TopicExample;
import com.hty.forum.mapper.QuestionMapper;
import com.hty.forum.mapper.TopicFollowMapper;
import com.hty.forum.mapper.TopicMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.service.QuestionService;
import com.hty.forum.service.TopicService;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * create by Semineces on 2020-10-09
 */
@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TopicFollowMapper topicFollowMapper;

    @Override
    public List<Topic> listAllTopic() {
        return topicMapper.selectByExample(null);
    }

    //查询相关话题
    @Override
    public List<Topic> listRelatedTopics(Integer id) {
        List<Question> questionList = questionMapper.listQuestionByTopic(id);
        if (questionList.size() == 0) return new ArrayList<>();
        List<Integer> topicIds = new ArrayList<>();
        for (Question question : questionList) {
            List<Question> relatedQuestions = questionService.getRelatedQuestion(question);
            if (relatedQuestions.size() > 0) {
                for (Question relatedQuestion : relatedQuestions) {
                    if (relatedQuestion != null && relatedQuestion.getTopic() != null) {
                        topicIds.add(relatedQuestion.getTopic());
                    }
                }
            }
        }
        if (topicIds.size() != 0) {
            TopicExample example = new TopicExample();
            example.createCriteria().andIdIn(topicIds).andIdNotEqualTo(id);
            return topicMapper.selectByExample(example);
        }
        return null;
    }

    @Override
    public R saveOrUpdate(TopicFollow topicFollow) {
        if (topicFollow.getId() == null) {
            topicFollowMapper.insertSelective(topicFollow);
        } else {
            topicFollow.setStatus(1);
            topicFollowMapper.updateByPrimaryKeySelective(topicFollow);
        }
        return null;
    }

    @Override
    public R unFollowTopic(TopicFollow topicFollow) {
        //应该加一个判断逻辑，看是否存在，不加其实也可以
        topicMapper.decreaseTopicFollowCount(topicFollow.getTopicId());
        topicFollow.setStatus(0);
        topicFollowMapper.updateByPrimaryKeySelective(topicFollow);
        return new R().okOf().addMsg("followCount", topicMapper.getTopicFollowCountById(topicFollow));
    }
}
