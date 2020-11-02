package com.hty.forum.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hty.forum.dto.CommentDto;
import com.hty.forum.dto.QuestionDto;
import com.hty.forum.dto.QuestionQueryDto;
import com.hty.forum.dto.TopicQueryDto;
import com.hty.forum.entity.Collect;
import com.hty.forum.entity.Comment;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.CollectExample;
import com.hty.forum.entity.example.CommentExample;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.*;
import com.hty.forum.myenum.QuestionCategory;
import com.hty.forum.myenum.QuestionCodeEnum;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.QuestionService;
import com.hty.forum.utils.DateFormateUtil;
import com.hty.forum.utils.DateUtil;
import com.hty.forum.utils.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * create by Semineces on 2020-10-05
 * TODO：规范化Controller
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Transactional
    @Override
    public R saveOrUpdate(Question question) {
        question.setGmtCreate(System.currentTimeMillis());
        question.setGmtModified(System.currentTimeMillis());
        question.setViewCount(0);
        question.setCommentCount(0);
        question.setLikeCount(0);
        //不知道什么意思的字段
        question.setTop(0);
        //用户是否加入或者创建了话题
        if (question.getTopic() != 0) {
            //加入话题讨论数增加
            topicMapper.increaseTalkCount(question.getTopic());
        }
        int insert = questionMapper.insert(question);
        if (insert > 0) {
            return new R().okOf().addMsg("result", QuestionCodeEnum.QUESTION_PUBLISH_SUCCESS.getMsg());
        } else return new R().errorOf(ResultCode.NOT_OK);

    }

    //通过id分页查询该人的问题
    @Override
    public PageInfo<Question> getQuestionByUserId(Integer pageNo, Integer pageSize, Integer userId) {
        //通过拦截SQL的方式设置分页数据，第几和一页有多少个
        PageHelper.startPage(pageNo, pageSize);
        List<Question> list = questionMapper.listQuestionWithUserByUserId(userId);
        PageInfo<Question> questionPageInfo = new PageInfo<>(list);
        //导航页码数，所谓导航页码数，就是在页面进行展示的那些1.2.3.4...
        //比如一共有分为两页数据的话，则将此值设置为2
        //一共分五页数据？
        questionPageInfo.setNavigatePages(5);
        return questionPageInfo;
    }

    @Override
    public Question getQuestionById(Integer questionId) {
        Question question = questionMapper.selectQuestionWithUserById(questionId);
        if (question == null) throw new MyException(ResultCode.QUESTION_NOT_FOUND);
        return question;
    }

    @Override
    public List<Question> getRelatedQuestion(Question question) {
        List<Question> relatedList = new ArrayList<>();
        String tags = question.getTag();
        if (!StringUtils.isEmpty(tags)) {
            //将tag转为sql语句正则表达式，其中|的意思是在该范围内进行或匹配
            //例如tag REGEXP 'java|spring' 将查询出所有的tag中含java或者spring
            String sqlRegexp = tags.replaceAll(",", "|");
            //mapper中的sql语句限定18个，也可以写在程序中用list.subList
            relatedList = questionMapper.selectRelated(sqlRegexp, question.getId());
        }
        return relatedList;
    }

    @Transactional
    @Override
    public void increaseViewCount(Question question) {
        questionMapper.increaseViewCount(question.getId());
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<CommentDto> getQuestionComments(Integer questionId) {
        CommentExample example = new CommentExample();
        CommentExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(questionId);
        example.setOrderByClause("gmt_create desc");
        //查询出来当前帖子下的所有评论，其中parent_id是帖子id
        List<Comment> commentList = commentMapper.selectByExample(example);
        List<CommentDto> commentDtoList = new ArrayList<>();
        if (commentList.size() > 0) {
            for (Comment comment : commentList) {
                //将查询出来的评论信息拷贝到dto里面
                CommentDto commentDto = new CommentDto();
                BeanUtils.copyProperties(comment, commentDto);
                //查询出来评论的用户信息并添加到dto里面
                User user = userMapper.selectByPrimaryKey(comment.getCommentor());
                commentDto.setUser(user);
                //获取创建时间并转换
                String dateString = simpleDateFormat.format(new Date(comment.getGmtCreate()));
                String time = DateFormateUtil.getTime(dateString);
                commentDto.setShowTime(time);
                //只给结果集中添加用户不为空的评论
                if (user != null) commentDtoList.add(commentDto);
            }
        }
        return commentDtoList;
    }

    //获取搜索的分页查询
    @Override
    public PageInfo<Question> getPageBySearch(QuestionQueryDto questionQueryDto) {
        List<Question> list = new ArrayList<>();
        //设置初始页以及每页的大小
        PageHelper.startPage(questionQueryDto.getPageNo(), questionQueryDto.getPageSize());
        //获取排序状态
        String sortType = questionQueryDto.getSort();
        switch (sortType) {
            case "ALL" :
                list = questionMapper.listQuestionWithUserBySearch(questionQueryDto);
                break;
            case "WEEK_HOT" :
                long startWeekTime = Objects.requireNonNull(DateUtil.getBeginDayOfWeek()).getTime();
                long endWeekTime = DateUtil.getEndDayOfWeek().getTime();
                questionQueryDto.setBeginTime(startWeekTime);
                questionQueryDto.setEndTime(endWeekTime);
                list = questionMapper.listQuestionWithUserBySearch(questionQueryDto);
                break;
            case "MONTH_HOT" :
                long startMonthTime = Objects.requireNonNull(DateUtil.getBeginDayOfMonth()).getTime();
                long endMonthTime = DateUtil.getEndDayOfWeek().getTime();
                questionQueryDto.setBeginTime(startMonthTime);
                questionQueryDto.setEndTime(endMonthTime);
                list = questionMapper.listQuestionWithUserBySearch(questionQueryDto);
                break;
            case "WAIT_COMMENT" :
                list = questionMapper.listQuestionZeroHot(questionQueryDto.getTag(),
                        questionQueryDto.getCategory());
                break;
            case "LIKE_HOT" :
                list = questionMapper.listQuestionMostLike(questionQueryDto);
                break;
            case "COMMENT_HOT" :
                list = questionMapper.listQuestionMostComment(questionQueryDto);
                break;
            case "VIEW_HOT" :
                list = questionMapper.listQuestionViewHot(questionQueryDto);
                break;
        }
        //格式化时间
        buildQuestionTime(list);
        PageInfo<Question> questionPageInfo = new PageInfo<>(list);
        questionPageInfo.setNavigatePages(5);
        return questionPageInfo;
    }

    @Override
    public PageInfo<Question> findQuestionByCategory(Integer pageNo, Integer pageSize, Integer categoryId) {
        PageHelper.startPage(pageNo, pageSize);
        List<Question> questions = questionMapper.listQuestionWithUserByCategory(categoryId);
        buildQuestionTime(questions);
        return new PageInfo<>(questions);
    }

    //分页获得收藏
    //TODO：通过收藏关系表，实际上查了两次数据库，可以优化为只查一次直接查出所有的收藏问题
    @Override
    public PageInfo<Question> getCollectPage(Integer pageNo, Integer pageSize, Integer userId) {
        PageHelper.startPage(pageNo, pageSize);
        List<Integer> questionIds = collectMapper.findQuestionByUserId(userId);
        if (questionIds.size() == 0) return null;
        List<Question> questions = questionMapper.listQuestionCollectedWithUser(questionIds);
        if (questions.size() > 0) {
            buildQuestionTime(questions);
        }
        return new PageInfo<>(questions);
    }

    @Override
    public List<User> getCollectUser(Integer questionId) {
        //查询出来某个帖子下的所有收藏者
        CollectExample example = new CollectExample();
        example.createCriteria().andQuestionIdEqualTo(questionId);
        List<Collect> collects = collectMapper.selectByExample(example);
        if (collects.size() > 0) {
            List<User> users = new ArrayList<>();
            for (Collect collect : collects) {
                User user = userMapper.selectByPrimaryKey(collect.getUserId());
                if (user != null) users.add(user);
            }
            return users;
        }
        return null;
    }

    @Transactional
    @Override
    public Boolean deleteCollect(Integer questionId, Integer userId) {
        //判断是否存在该question
        if (StringUtils.isEmpty(questionId) || StringUtils.isEmpty(userId)) {
            return false;
        }
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (question == null) return false;
        //执行删除逻辑
        CollectExample example = new CollectExample();
        CollectExample.Criteria criteria = example.createCriteria();
        criteria.andQuestionIdEqualTo(questionId);
        criteria.andUserIdEqualTo(userId);
        int result = collectMapper.deleteByExample(example);
        return result > 0;
    }

    @Override
    public List<QuestionDto> findNewQuestions(Integer count) {
        return questionMapper.selectNewQuestions(count);
    }

    @Override
    public List<QuestionDto> findRecommendQuestions(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return questionMapper.selectRecommendQuestions();
    }

    @Override
    public int updateQuestion(Question question) {
        return questionMapper.updateByPrimaryKeySelective(question);
    }

    //查询话题下的所有问题带分页
    @Override
    public PageInfo<Question> findQuestionWithUserByTopic(TopicQueryDto topicQueryDto) {
        PageHelper.startPage(topicQueryDto.getPageNo(), topicQueryDto.getPageSize());
        List<Question> questions = new ArrayList<>();
        switch (topicQueryDto.getSortBy()){
            case "all": questions= questionMapper.findQuestionsWithUserByTopicAll(topicQueryDto);
                break;
            case "jh": questions= questionMapper.findQuestionsWithUserByTopicJH(topicQueryDto);
                break;
            case "tj": questions= questionMapper.findQuestionsWithUserByTopicTJ(topicQueryDto);
                break;
            case "wt": questions= questionMapper.findQuestionsWithUserByTopicWT(topicQueryDto);
                break;
        }
        if (questions != null && questions.size() > 0) {
            //格式化时间
            buildQuestionTime(questions);
            return new PageInfo<>(questions);
        }
        return null;
    }

    //获取问题的时间，并转化
    //TODO：完全可以写dto来传值，将其设置在dto里面
    private void buildQuestionTime(List<Question> questionList) {
        for (Question question : questionList) {
            //获取帖子的时间并进行转化
            Date date = new Date(question.getGmtCreate());
            String dateString = simpleDateFormat.format(date);
            //设置时间
            if (!StringUtils.isEmpty(dateString)) {
                String time = DateFormateUtil.getTime(dateString);
                question.setShowTime(time);
            } else {
                //如果为空则设置当前时间
                String nowTime = DateFormateUtil.getTime(simpleDateFormat.format(new Date(System.currentTimeMillis())));
                question.setShowTime(nowTime);
            }
            Integer category = question.getCategory();
            String typename = QuestionCategory.getNameByVal(category);
            question.setTypeName(typename);
        }
    }
}
