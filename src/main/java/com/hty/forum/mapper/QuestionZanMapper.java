package com.hty.forum.mapper;

import com.hty.forum.entity.QuestionZan;
import com.hty.forum.entity.example.QuestionZanExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionZanMapper {
    int countByExample(QuestionZanExample example);

    int deleteByExample(QuestionZanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(QuestionZan record);

    int insertSelective(QuestionZan record);

    List<QuestionZan> selectByExample(QuestionZanExample example);

    QuestionZan selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") QuestionZan record, @Param("example") QuestionZanExample example);

    int updateByExample(@Param("record") QuestionZan record, @Param("example") QuestionZanExample example);

    int updateByPrimaryKeySelective(QuestionZan record);

    int updateByPrimaryKey(QuestionZan record);

}