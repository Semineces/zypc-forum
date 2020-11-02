package com.hty.forum.controller.web;

import com.github.pagehelper.PageInfo;
import com.hty.forum.dto.TopicQueryDto;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.Topic;
import com.hty.forum.entity.TopicFollow;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.TopicFollowExample;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.QuestionMapper;
import com.hty.forum.mapper.TopicFollowMapper;
import com.hty.forum.mapper.TopicMapper;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.QuestionService;
import com.hty.forum.service.TopicService;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * create by Semineces on 2020-10-09
 * 话题Controller
 */
@Controller
public class TopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private TopicFollowMapper topicFollowMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 页面跳转
     * @param map
     * @return
     */
    @GetMapping("/topic")
    public String topic(Map<String, Object> map) {
        List<Topic> topics = topicService.listAllTopic();
        map.put("topics", topics);
        map.put("navLi", "topic");
        return "topic";
    }

    /**
     * 获取用户关注的状态 通过topic_follow表 status状态 1关注 0未关注
     * 目前没发现有什么用
     * @param request
     * @param id
     * @return
     */
    @ResponseBody
    @GetMapping("/topic/getFollowStatus")
    public R getFollowTopicStatusAjax(HttpServletRequest request, @RequestParam("id") Integer id) {
        User user = (User) request.getSession().getAttribute("user");
        //该用户关注话题的状态
        Integer status = 0;
        TopicFollowExample example = new TopicFollowExample();
        if (user != null) {
            example.createCriteria().andTopicIdEqualTo(id).andUserIdEqualTo(user.getId());
            List<TopicFollow> topicFollows = topicFollowMapper.selectByExample(example);
            if (!topicFollows.isEmpty()) status = topicFollows.get(0).getStatus();
        }
        return new R().okOf().addMsg("followStatus", status);
    }

    /**
     * 话题详情
     * @param id
     * @param map
     * @return
     */
    @GetMapping("/topic/{id}")
    public String findTopic(@PathVariable("id") String id, Map<String, Object> map) {
        int topicId = Integer.parseInt(id);
        Topic topic = topicMapper.selectByPrimaryKey((long) topicId);
        if (topic == null) throw new MyException(ResultCode.NOT_FOUND_TOPIC);
        map.put("navLi", "topic");
        map.put("topic", topic);
        return "topicInfo";
    }

    /**
     * 加载话题的数据
     * @param topicId 话题id
     * @param pageNo 页面起始
     * @param pageSize 页面大小
     * @param sortBy 排序方式
     * @return
     */
    @ResponseBody
    @GetMapping("/loadTopicInfo")
    public R loadTopicInfo(@RequestParam("topicId") String topicId,
                           @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                           @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize,
                           @RequestParam(value = "sortBy", required = false) String sortBy
                           ) {
        //该话题的问题列表
        TopicQueryDto topicQueryDto = new TopicQueryDto();
        topicQueryDto.setId((long) Integer.parseInt(topicId));
        topicQueryDto.setPageSize(pageSize);
        topicQueryDto.setPageNo(pageNo);
        topicQueryDto.setSortBy(sortBy);
        PageInfo<Question> pageInfo = questionService.findQuestionWithUserByTopic(topicQueryDto);
        //关注该话题的用户
        List<User> userList = new ArrayList<>();
        TopicFollowExample example = new TopicFollowExample();
        example.createCriteria().andTopicIdEqualTo(Integer.parseInt(topicId)).andStatusEqualTo(1);
        List<TopicFollow> topicFollows = topicFollowMapper.selectByExample(example);
        if (topicFollows.size() > 0) {
            for (TopicFollow topicFollow : topicFollows) {
                Integer userId = topicFollow.getUserId();
                User user = userMapper.selectByPrimaryKey(userId);
                userList.add(user);
            }
        }
        //相关话题
        List<Topic> topicList = topicService.listRelatedTopics(Integer.parseInt(topicId));
        return new R().okOf().addMsg("page", pageInfo)
                .addMsg("relatedTopics", topicList)
                .addMsg("userList", userList);
    }

    /**
     * 关注话题
     * @param topicId
     * @param request
     * @return 返回结果是获取关注的数量
     */
    @ResponseBody
    @GetMapping("/topic/follow")
    public R followTopic(@RequestParam("id") Integer topicId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return new R().errorOf(ResultCode.USER_NO_LOGIN);
        }
        Topic topic = topicMapper.selectByPrimaryKey((long) topicId);
        if (topic == null) {
            return new R().errorOf(ResultCode.NOT_FOUND_TOPIC);
        }
        //首先判断该用户是否已经关注话题
        TopicFollowExample example = new TopicFollowExample();
        example.createCriteria().andUserIdEqualTo(user.getId()).andTopicIdEqualTo(topicId);
        List<TopicFollow> topicFollows = topicFollowMapper.selectByExample(example);
        if (!topicFollows.isEmpty() && topicFollows.get(0).getStatus() == 1) {
            return new R().errorOf(ResultCode.TOPIC_IS_FOLLOWED);
        }
        //插入或者更新状态
        TopicFollow topicFollow = new TopicFollow();
        topicFollow.setTopicId(topicId);
        topicFollow.setUserId(user.getId());
        topicFollow.setGmtCreate(System.currentTimeMillis());
        topicFollow.setGmtModified(System.currentTimeMillis());
        //如果用户是之前取消关注的
        if (!topicFollows.isEmpty()) {
            TopicFollow dbTopicFollow = topicFollows.get(0);
            //设置id
            topicFollow.setId(dbTopicFollow.getId());
        }
        return topicService.saveOrUpdate(topicFollow);
    }

    @ResponseBody
    @GetMapping("/topic/unFollowTopic")
    public R unFollowTopic(@RequestParam("id") Integer topicId, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return new R().errorOf(ResultCode.USER_NO_LOGIN);
        }
        Topic topic = topicMapper.selectByPrimaryKey((long) topicId);
        if (topic == null) {
            return new R().errorOf(ResultCode.NOT_FOUND_TOPIC);
        }
        TopicFollowExample example = new TopicFollowExample();
        example.createCriteria().andUserIdEqualTo(user.getId())
                .andTopicIdEqualTo(topicId)
                .andStatusEqualTo(1);
        List<TopicFollow> topicFollows = topicFollowMapper.selectByExample(example);
        if (!topicFollows.isEmpty()) {
            return topicService.unFollowTopic(topicFollows.get(0));
        } else return new R().errorOf(ResultCode.NOT_FOUND_TOPIC);
    }
}
