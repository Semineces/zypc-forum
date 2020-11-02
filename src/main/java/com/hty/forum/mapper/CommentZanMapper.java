package com.hty.forum.mapper;

import com.hty.forum.entity.CommentZan;
import com.hty.forum.entity.example.CommentZanExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentZanMapper {
    int countByExample(CommentZanExample example);

    int deleteByExample(CommentZanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CommentZan record);

    int insertSelective(CommentZan record);

    List<CommentZan> selectByExample(CommentZanExample example);

    CommentZan selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CommentZan record, @Param("example") CommentZanExample example);

    int updateByExample(@Param("record") CommentZan record, @Param("example") CommentZanExample example);

    int updateByPrimaryKeySelective(CommentZan record);

    int updateByPrimaryKey(CommentZan record);
}