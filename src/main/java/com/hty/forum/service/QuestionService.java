package com.hty.forum.service;

import com.github.pagehelper.PageInfo;
import com.hty.forum.dto.CommentDto;
import com.hty.forum.dto.QuestionDto;
import com.hty.forum.dto.QuestionQueryDto;
import com.hty.forum.dto.TopicQueryDto;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.utils.R;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
public interface QuestionService {

    //发布或者更新帖子
    R saveOrUpdate(Question question);

    //根据用户id查询其发表的帖子
    PageInfo<Question> getQuestionByUserId(Integer pageNo, Integer pageSize, Integer userId);

    //根据帖子id查询帖子
    Question getQuestionById(Integer questionId);

    //查询相关帖子
    List<Question> getRelatedQuestion(Question question);

    //增加访问数
    void increaseViewCount(Question question);

    //查询帖子的评论
    List<CommentDto> getQuestionComments(Integer questionId);

    //分页查询搜索的帖子
    PageInfo<Question> getPageBySearch(QuestionQueryDto questionQueryDto);

    //分页查询根据分类的帖子
    PageInfo<Question> findQuestionByCategory(Integer pageNo, Integer pageSize, Integer categoryId);

    //查询某个用户的收藏
    PageInfo<Question> getCollectPage(Integer pageNo, Integer pageSize, Integer userId);

    //获取收藏的用户
    List<User> getCollectUser(Integer id);

    Boolean deleteCollect(Integer questionId, Integer userId);

    //查询最新的帖子
    List<QuestionDto> findNewQuestions(Integer count);

    List<QuestionDto> findRecommendQuestions(Integer pageNo, Integer pageSize);

    int updateQuestion(Question question);

    //根据话题找到帖子
    PageInfo<Question> findQuestionWithUserByTopic(TopicQueryDto topicQueryDto);
}
