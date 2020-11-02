package com.hty.forum.mapper;

import com.hty.forum.dto.QuestionDto;
import com.hty.forum.dto.QuestionQueryDto;
import com.hty.forum.dto.TopicQueryDto;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.example.QuestionExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionMapper {

    int countByExample(QuestionExample example);

    int deleteByExample(QuestionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Question record);

    int insertSelective(Question record);

    List<Question> selectByExample(QuestionExample example);

    Question selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Question record, @Param("example") QuestionExample example);

    int updateByExample(@Param("record") Question record, @Param("example") QuestionExample example);

    int updateByPrimaryKeySelective(Question record);

    int updateByPrimaryKey(Question record);

    //增加评论的数量
    void increaseCommentCount(@Param("id") Integer parentId);

    //增加点赞的数量
    void increaseLikeCount(@Param("id") Integer questionId);

    //增加查看数量
    void increaseViewCount(@Param("id") Integer questionId);

    //查询出帖子以及发帖子的用户信息，将其查询为一条SQL语句
    Question selectQuestionWithUserById(@Param("id") Integer questionId);

    //查询相关的帖子，最多显示18个
    List<Question> selectRelated(@Param("sqlRegexp") String sqlRegexp, @Param("id") Integer questionId);

    //根据查询条件去查询问题
    List<Question> listQuestionWithUserBySearch(QuestionQueryDto questionQueryDto);

    //TODO：以下其实都是一个模子，完全可以传值然后去xml中用if标签
    //查询0评论
    List<Question> listQuestionZeroHot(String tag, Integer category);

    //查询赞最多的
    List<Question> listQuestionMostLike(QuestionQueryDto questionQueryDto);

    //查询评论最多的
    List<Question> listQuestionMostComment(QuestionQueryDto questionQueryDto);

    //查询查看最多的
    List<Question> listQuestionViewHot(QuestionQueryDto questionQueryDto);

    //根据分类查询问题
    List<Question> listQuestionWithUserByCategory(Integer categoryId);

    //查询最新的问题
    List<QuestionDto> selectNewQuestions(@Param("count") Integer count);

    //查询推荐问题
    List<QuestionDto> selectRecommendQuestions();

    //通过用户id查询帖子
    List<Question> listQuestionWithUserByUserId(Integer userId);

    //根据ids去查询所有的问题
    List<Question> listQuestionCollectedWithUser(List<Integer> groups);

    //通过话题查找问题
    List<Question> listQuestionByTopic(Integer id);

    //创建时间
    List<Question> findQuestionsWithUserByTopicAll(TopicQueryDto topicQueryDto);

    //查看数量
    List<Question> findQuestionsWithUserByTopicJH(TopicQueryDto topicQueryDto);

    //点赞数量
    List<Question> findQuestionsWithUserByTopicTJ(TopicQueryDto topicQueryDto);

    //在提问里面找
    List<Question> findQuestionsWithUserByTopicWT(TopicQueryDto topicQueryDto);
}