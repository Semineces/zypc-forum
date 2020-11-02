package com.hty.forum.mapper;

import com.hty.forum.entity.Integral;
import com.hty.forum.entity.example.IntegralExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntegralMapper {
    int countByExample(IntegralExample example);

    int deleteByExample(IntegralExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Integral record);

    int insertSelective(Integral record);

    List<Integral> selectByExample(IntegralExample example);

    Integral selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Integral record, @Param("example") IntegralExample example);

    int updateByExample(@Param("record") Integral record, @Param("example") IntegralExample example);

    int updateByPrimaryKeySelective(Integral record);

    int updateByPrimaryKey(Integral record);
}