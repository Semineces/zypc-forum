package com.hty.forum.controller.web;

import com.hty.forum.dto.CommentDto;
import com.hty.forum.entity.*;
import com.hty.forum.entity.example.CollectExample;
import com.hty.forum.entity.example.IntegralExample;
import com.hty.forum.entity.example.NotificationExample;
import com.hty.forum.entity.example.QuestionZanExample;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.*;
import com.hty.forum.myenum.*;
import com.hty.forum.service.QuestionService;
import com.hty.forum.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * create by Semineces on 2020-10-05
 */
@Controller
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private QuestionZanMapper questionZanMapper;

    @Autowired
    private IntegralMapper integralMapper;

    @Autowired
    private CollectMapper collectMapper;

    /**
     * 查询帖子详情并返回页面
     * @param id 帖子id
     * @param map 返回的数据结果封装
     * @param request 用于获取用户登录信息
     * @return 页面
     */
    @RequestMapping("/question/{id}")
    public String question(@PathVariable("id") String id, Map<String, Object> map, HttpServletRequest request) {
        //获取当前登录的用户以及帖子id
        User currentUser = (User) request.getSession().getAttribute("user");
        Integer questionId = Integer.parseInt(id);
        //进行id合法校验
        if (StringUtils.isEmpty(id) || questionId <= 0) throw new MyException(ResultCode.QUESTION_NOT_FOUND);
        //查询帖子
        Question question = questionService.getQuestionById(questionId);
        //查询相关帖子
        List<Question> relatedQuestions = questionService.getRelatedQuestion(question);
        //让该帖子的浏览数加一
        if (currentUser != null) {
            logger.info(currentUser.getName() + "查看帖子" + question.getId() + new Date());
        }
        questionService.increaseViewCount(question);
        //获取该帖子的评论信息，至于是question.getId()的原因是，这个question是我们已经查询出来的。
        List<CommentDto> commentDtos = questionService.getQuestionComments(question.getId());
        //获取收藏该帖子的用户
        List<User> collectUsers = questionService.getCollectUser(question.getId());
        //获取发帖用户的积分
        Long integral = userMapper.getIntegral(question.getCreator());
        //将前端需要的数据封装到一map并进行返回
        if (StringUtils.isEmpty(integral)) {
            map.put("integral", 0);
        } else map.put("integral", integral);
        map.put("question", question);
        map.put("comments", commentDtos);
        map.put("collect_users", collectUsers);
        map.put("relatedQuestions", relatedQuestions);
        return "question";
    }

    /**
     * 删除帖子
     * @param questionId 帖子id
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/deleteQuestion")
    public R deleteQuestion(@RequestParam("id") Integer questionId, HttpServletRequest request) {
        //获取当前登录的用户
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        //检验要删除的帖子是否是本人的帖子
        Question dbQuestion = questionMapper.selectByPrimaryKey(questionId);
        if (!dbQuestion.getCreator().equals(currentUser.getId())) {
            //可以抛出异常，也可以直接return
            throw new MyException(ResultCode.QUESTION_NOT_IS_YOURS);
        }
        //删除帖子
        //TODO：应该优化为逻辑删除，而该代码是物理删除
        questionMapper.deleteByPrimaryKey(questionId);
        //删除通知中有关该帖子的东西
        NotificationExample notificationExample = new NotificationExample();
        NotificationExample.Criteria criteria = notificationExample.createCriteria();
        criteria.andOutterIdEqualTo(dbQuestion.getId());
        notificationMapper.deleteByExample(notificationExample);
        //TODO：当删除某个帖子时，其关联的数据都应该删除，评论什么的
        return new R().okOf().addMsg("result", QuestionCodeEnum.QUESTION_DELETE_SUCCESS.getMsg());
    }

    /**
     * 点赞帖子
     * @param questionId 帖子的id
     * @param request
     * @return
     */
    @Transactional
    @ResponseBody
    @GetMapping("/likeQuestion")
    public R likeQuestion(@RequestParam("id") Integer questionId, HttpServletRequest request) {
        //获取当前的用户并判断是否登录
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        //构造条件查询该用户是否已经点过赞了
        QuestionZanExample zanExample = new QuestionZanExample();
        QuestionZanExample.Criteria zanExampleCriteria = zanExample.createCriteria();
        zanExampleCriteria.andQuestionIdEqualTo((long) questionId);
        zanExampleCriteria.andUserIdEqualTo(currentUser.getId());
        List<QuestionZan> questionZans = questionZanMapper.selectByExample(zanExample);
        //TODO：对于点赞，如果点赞第二次就是取消点赞
        if (questionZans.size() > 0) return new R().errorOf(ResultCode.CANT_LIKE_QUESTION_TWICE);
        //查询出当前问题
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (question == null) return new R().errorOf(ResultCode.QUESTION_NOT_FOUND);
        //进行点赞操作，实质上就是给用户点赞关系表增加一条数据
        QuestionZan questionZan = new QuestionZan();
        questionZan.setUserId(currentUser.getId());
        questionZan.setQuestionId((long) questionId);
        questionZan.setGmtCreate(System.currentTimeMillis());
        questionZan.setGmtModified(System.currentTimeMillis());
        questionZanMapper.insert(questionZan);
        questionMapper.increaseLikeCount(questionId);

        //进行通知以及点赞操作
        //条件是当前用户操作的不是自己的帖子
        if (!currentUser.getId().equals(question.getCreator())) {
            //给通知表中添加一条数据
            Notification notification = new Notification();
            //通知人和接收人分别是当前用户和帖子的作者，即当前用户点赞了xxx的帖子
            notification.setNotifier((long) currentUser.getId());
            notification.setReceiver((long) question.getCreator());
            notification.setType(NotificationType.LIKE_QUESTION.getCode());
            notification.setOutterId(questionId);
            notification.setStatus(NotificationStatus.UN_READ.getCode());
            notification.setGmtCreate(System.currentTimeMillis());
            notificationMapper.insertSelective(notification);

            //被点赞的人可以获得两积分
            IntegralExample integralExample = new IntegralExample();
            IntegralExample.Criteria integralExampleCriteria = integralExample.createCriteria();
            integralExampleCriteria.andUserIdEqualTo(question.getCreator());
            //这个搜索的SQL实质是，将用户积分关系表中的userId拿来和问题的创建者id进行对比，并查出大于等于0的值
            List<Integral> integrals = integralMapper.selectByExample(integralExample);
            //如果此人之前已经获得过积分，我们查询其实只能查出一条数据来
            if (integrals.size() > 0) {
                Integral integral = integrals.get(0);
                //TODO：多线程下可能会发生问题
                integral.setIntegral(integral.getIntegral() + IntegralType.LIKE.getVal());
                integralMapper.updateByPrimaryKeySelective(integral);
            } else {
                Integral integral = new Integral();
                integral.setIntegral(IntegralType.LIKE.getVal());
                integral.setUserId(question.getCreator());
                integral.setGmtCreate(System.currentTimeMillis());
                integral.setGmtModified(System.currentTimeMillis());
                integralMapper.insert(integral);
            }
        }
        //TODO:多线程下可能会发生问题
        return new R().okOf().addMsg("likequestioncount", question.getLikeCount() + 1);
    }

    /**
     * 收藏帖子
     * @param questionId 帖子id
     * @param request
     * @return
     */
    @GetMapping("/doCollect")
    @ResponseBody
    public R doCollect(@RequestParam("id") Integer questionId, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        //判断该问题是否存在
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (question == null) return new R().errorOf(ResultCode.QUESTION_NOT_FOUND);
        //判断当前用户是否已经收藏，实质上就是在收藏关系里面找
        CollectExample collectExample = new CollectExample();
        CollectExample.Criteria criteria = collectExample.createCriteria();
        criteria.andUserIdEqualTo(currentUser.getId());
        criteria.andQuestionIdEqualTo(questionId);
        int count = collectMapper.countByExample(collectExample);
        //TODO：如果已经收藏，再点应该是取消收藏。
        if (count > 0) {
            return new R().errorOf(ResultCode.YOU_COLLECTED_QUESTION);
        } else {
            //进行收藏
            Collect collect = new Collect();
            collect.setQuestionId(questionId);
            collect.setUserId(currentUser.getId());
            collect.setGmtCreate(System.currentTimeMillis());
            collect.setGmtModified(System.currentTimeMillis());
            collectMapper.insertSelective(collect);
            //对被收藏的人添加积分
            IntegralExample integralExample = new IntegralExample();
            IntegralExample.Criteria integralCriteria = integralExample.createCriteria();
            integralCriteria.andUserIdEqualTo(question.getCreator());
            List<Integral> integralList = integralMapper.selectByExample(integralExample);
            //积分表中有人，则进行添加操作，否则进行新增
            if (integralList.size() > 0) {
                Integral integral = integralList.get(0);
                //TODO：多线程下的安全问题
                integral.setIntegral(integral.getIntegral() + IntegralType.COMMENT.getVal());
                integral.setGmtModified(System.currentTimeMillis());
                integralMapper.updateByPrimaryKeySelective(integral);
            } else {
                Integral integral = new Integral();
                integral.setUserId(question.getCreator());
                integral.setIntegral(IntegralType.COMMENT.getVal());
                integral.setGmtCreate(System.currentTimeMillis());
                integral.setGmtModified(System.currentTimeMillis());
                integralMapper.insertSelective(integral);
            }
        }
        return new R().okOf();

    }

    /**
     * 删除收藏
     * @param questionId 帖子的id
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/deleteCollect")
    public R deleteCollect(@RequestParam("id") Integer questionId, HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null) return new R().errorOf(ResultCode.USER_NO_LOGIN);
        Boolean result = questionService.deleteCollect(questionId, currentUser.getId());
        R r = new R();
        return result ? r.okOf() : r.errorOf(ResultCode.NOT_OK);
    }
}

