package com.hty.forum.mapper;

import com.hty.forum.entity.User;
import com.hty.forum.entity.example.UserExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {

    int countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") User record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //获取发帖用户的积分
    //@Select("select integral from user_integral where user_id = #{id}")
    Long getIntegral(@Param("id") Integer id);

    //获取当前用户的粉丝数量
    Integer getFansCount(@Param("id") Integer id);

    //获取关注数
    Integer getFollowCount(@Param("id") Integer id);

    //获取帖子数
    Integer getQuestionCount(@Param("id") Integer id);

    //获取收藏数
    Integer getCollectCount(@Param("id") Integer id);

    //标记全部已读
    void readAllNotification(@Param("id") Integer id);

    //获取所有的新用户
    List<User> selectNewUserList(@Param("count") Integer count);

    //获得粉丝的id
    List<Integer> getFansIds(@Param("id")Integer id);
}