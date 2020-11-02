package com.hty.forum.controller.web;

import com.hty.forum.dto.CommentDto;
import com.hty.forum.entity.*;
import com.hty.forum.entity.example.CommentZanExample;
import com.hty.forum.mapper.CommentMapper;
import com.hty.forum.mapper.CommentZanMapper;
import com.hty.forum.mapper.NotificationMapper;
import com.hty.forum.mapper.QuestionMapper;
import com.hty.forum.myenum.CommentType;
import com.hty.forum.myenum.NotificationStatus;
import com.hty.forum.myenum.NotificationType;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.CommentService;
import com.hty.forum.service.NotificationService;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * create by Semineces on 2020-10-07
 */

@Controller
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentZanMapper commentZanMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 点赞评论以及通知
     * @param commentId 评论的id
     * @param request
     * @return
     */
    @GetMapping("/likeComment")
    @ResponseBody
    public R likeComment(@RequestParam("id") Integer commentId, HttpServletRequest request) {
        //判断用户有无登录
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        //判断评论是否存在
        Comment comment = commentMapper.selectByPrimaryKey(commentId);
        if (comment == null) return new R().errorOf(ResultCode.COMMENT_NOT_FOUNT);
        //判断该用户是否已经点赞，没有的话则进行点赞操作
        CommentZanExample example = new CommentZanExample();
        CommentZanExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo((long) currentUser.getId());
        criteria.andCommentIdEqualTo(commentId);
        List<CommentZan> commentZans = commentZanMapper.selectByExample(example);
        if (commentZans.size() > 0) {
            return new R().errorOf(ResultCode.COMMENT_LIKE_TWICE);
        } else {
            CommentZan commentZan = new CommentZan();
            commentZan.setCommentId(commentId);
            commentZan.setUserId((long) currentUser.getId());
            commentZan.setGmtCreate(System.currentTimeMillis());
            commentZan.setGmtModified(System.currentTimeMillis());
            int result = commentZanMapper.insertSelective(commentZan);
            if (result < 1) return new R().errorOf(ResultCode.NOT_OK);
            commentMapper.increaseLikeCount(commentId);
        }
        //点赞完后进行通知
        //自己给自己点赞不通知
        if (!comment.getCommentor().equals(currentUser.getId())) {
            //通知
            Notification notification = new Notification();
            notification.setNotifier((long) currentUser.getId());
            notification.setReceiver((long) comment.getCommentor());
            notification.setType(NotificationType.COMMENT_LIKE.getCode());
            notification.setOutterId(commentId);
            notification.setStatus(NotificationStatus.UN_READ.getCode());
            notification.setGmtCreate(System.currentTimeMillis());
            int result = notificationMapper.insertSelective(notification);
            if (result < 1) return new R().errorOf(ResultCode.NOT_OK);
        }
        //TODO：应该把加的逻辑写数据库里面
        return new R().okOf().addMsg("likecount", comment.getLikeCount() + 1);
    }

    /**
     * 评论和通知
     * @param commentDto
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/comment")
    public R postComment(CommentDto commentDto, HttpServletRequest request) {
        //验证用户是否登录
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) {
            return new R().errorOf(ResultCode.USER_NO_LOGIN);
        }
        //验证评论信息是否为空
        if (StringUtils.isEmpty(commentDto.getContent())) {
            return new R().errorOf(ResultCode.COMMENT_CANT_EMPTY);
        }
        //验证评论的长度不能超过50个字
        if (commentDto.getContent().length() > 50) {
            return new R().errorOf(ResultCode.COMMENT_CONTENT_TO_MANY);
        }
        //对一级评论和二级评论分别进行处理
        if (commentDto.getType() == CommentType.COMMENT_ONE.getVal()) {
            Question question = questionMapper.selectByPrimaryKey(commentDto.getParentId());
            if (question == null) return new R().errorOf(ResultCode.QUESTION_NOT_FOUND);
            //创建评论
            Comment comment = commentService.createComment(commentDto, currentUser);
            //评论问题通知
            Notification notification = notificationService.createNotification(
                    question.getCreator(),
                    comment,
                    NotificationType.COMMENT_QUESTION.getCode());
            //添加通知
            if (!notification.getNotifier().equals(notification.getReceiver())) {
                notificationMapper.insertSelective(notification);
            }
        }
        if (commentDto.getType() == CommentType.COMMENT_TWO.getVal()) {
            Comment oneComment = commentMapper.selectByPrimaryKey(commentDto.getParentId());
            if (oneComment == null) return new R().errorOf(ResultCode.COMMENT_NOT_FOUNT);
            //创建评论
            Comment towComment = commentService.createComment(commentDto, currentUser);
            //评论问题通知
            Notification notification = notificationService.createNotification(
                    oneComment.getCommentor(),
                    towComment,
                    NotificationType.COMMENT_QUESTION.getCode());
            if (!notification.getNotifier().equals(notification.getReceiver())) {
                notificationMapper.insertSelective(notification);
            }
        }
        return new R().okOf();
    }

    /**
     * 根据一级评论的id获得其下所有的二级评论
     * @param idStr
     * @return
     */
    @ResponseBody
    @GetMapping("/comment/{id}")
    public R comments(@PathVariable("id") String idStr) {
        if (StringUtils.isEmpty(idStr)) return new R().errorOf(ResultCode.COMMENT_NOT_FOUNT);
        Integer id = Integer.parseInt(idStr);
        List<CommentDto> twoCommentDtos = commentService.getSecondComment(id);
        return new R().okOf().addMsg("comment2s", twoCommentDtos);
    }

}
