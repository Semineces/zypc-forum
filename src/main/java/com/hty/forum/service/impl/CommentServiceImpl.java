package com.hty.forum.service.impl;

import com.hty.forum.dto.CommentDto;
import com.hty.forum.entity.Comment;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.CommentExample;
import com.hty.forum.mapper.CommentMapper;
import com.hty.forum.mapper.QuestionMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.CommentType;
import com.hty.forum.service.CommentService;
import com.hty.forum.utils.DateFormateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * create by Semineces on 2020-10-05
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 向数据库中插入评论，并增加评论数量
     * @param comment
     */
    @Transactional
    @Override
    public void doComment(Comment comment) {
        int result = commentMapper.insertSelective(comment);
        if (result > 0) {
            //如果是一级评论，则给当前帖子的评论加一；否则就是给评论的回复数量加一
            if (comment.getType().equals(CommentType.COMMENT_ONE.getVal())) {
                questionMapper.increaseCommentCount(comment.getParentId());
            } else {
                commentMapper.increaseCommentCount(comment.getParentId());
            }
        }
    }

    /**
     * 创建评论
     * @param commentDto
     * @param currentUser
     * @return
     */
    @Transactional
    @Override
    public Comment createComment(CommentDto commentDto, User currentUser) {
        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setParentId(commentDto.getParentId());
        comment.setGmtCreate(System.currentTimeMillis());
        comment.setGmtModified(System.currentTimeMillis());
        comment.setType(commentDto.getType());
        comment.setCommentor(currentUser.getId());
        doComment(comment);
        return comment;
    }

    @Override
    public List<CommentDto> getSecondComment(Integer parentId) {
        //将所有的parentId二级评论按照时间顺序查出来
        CommentExample commentExample = new CommentExample();
        commentExample.setOrderByClause("gmt_create desc");
        CommentExample.Criteria criteria = commentExample.createCriteria();
        criteria.andParentIdEqualTo(parentId)
                .andTypeEqualTo(CommentType.COMMENT_TWO.getVal());
        List<Comment> twoComments = commentMapper.selectByExample(commentExample);
        //将二级评论封装成dto发送给前端
        List<CommentDto> twoCommentDtos = new ArrayList<>();
        if (twoComments.size() > 0) {
            for (Comment twoComment : twoComments) {
                CommentDto twoCommentDto = new CommentDto();
                //将查出来的部分数据直接拷贝
                BeanUtils.copyProperties(twoComment, twoCommentDto);
                //设置用户
                twoCommentDto.setUser(userMapper.selectByPrimaryKey(twoComment.getCommentor()));
                //设置时间
                String dateString = simpleDateFormat.format(new Date(twoComment.getGmtCreate()));
                String time = DateFormateUtil.getTime(dateString);
                twoCommentDto.setShowTime(time);
                twoCommentDtos.add(twoCommentDto);
            }
        }
        return twoCommentDtos;
    }
}
