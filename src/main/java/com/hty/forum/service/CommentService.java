package com.hty.forum.service;

import com.hty.forum.dto.CommentDto;
import com.hty.forum.entity.Comment;
import com.hty.forum.entity.User;

import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
public interface CommentService {

    //增加评论的数量
    void doComment(Comment comment);

    //创建评论
    Comment createComment(CommentDto commentDto, User currentUser);

    //获取二级评论
    List<CommentDto> getSecondComment(Integer id);
}
