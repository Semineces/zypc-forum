package com.hty.forum.controller.web;

import com.hty.forum.cache.TagsCache;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.exception.MyException;
import com.hty.forum.myenum.QuestionCodeEnum;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.QuestionService;
import com.hty.forum.service.TopicService;
import com.hty.forum.utils.R;
import com.hty.forum.utils.SensitiveWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * create by Semineces on 2020-10-09
 */
@Controller
public class PublishController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TopicService topicService;

    public static final SensitiveWord sw = new SensitiveWord();

    /**
     * 进行发布页面的跳转
     * @param map
     * @param model
     * @return
     */
    @GetMapping("/publish")
    public String publish(Map<String, Object> map, Model model) {
        List<TagsCache> tagsCache = TagsCache.getTagsCache();
        model.addAttribute("tagsCache", tagsCache);
        map.put("topiclist", topicService.listAllTopic());
        map.put("navLi", "publish");
        return "publish";
    }

    @ResponseBody
    @PostMapping("/publish")
    public R doPublish(@RequestParam(value = "title") String title,
                       @RequestParam("description") String description,
                       @RequestParam("tag") String tag,
                       @RequestParam(value = "category", required = false) Integer category,
                       @RequestParam(value = "topic", required = false) Integer topic,
                       HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        //验证用户登入
        if (currentUser == null) {
            return new R().errorOf(QuestionCodeEnum.QUESTION_NEED_LOGIN);
        }
        //验证帖子标题是否合法
        if (StringUtils.isEmpty(title)) {
            return new R().errorOf(QuestionCodeEnum.QUESTION_HEAD_CANT_EMPTY);
        } else if (title.trim().length() < 3) {
            return new R().errorOf(ResultCode.TITLE_IS_TOO_SIMPLE);
        } else {
            //过滤敏感词汇
            title = sw.filterInfo(title);
        }
        //校验分类
        if (category == 0) {
            return new R().errorOf(QuestionCodeEnum.Question_Category_CANT_EMPTY);
        }
        //校验内容
        if (StringUtils.isEmpty(description)) {
            return new R().errorOf(QuestionCodeEnum.QUESTION_DESC_CANT_EMPTY);
        } else {
            description = sw.filterInfo(description);
        }
        //校验标签
        if (StringUtils.isEmpty(tag)) {
            return new R().errorOf(QuestionCodeEnum.QUESTION_TAGS_CANT_EMPTY);
        } else {
            String[] tags = tag.split(",");
            for (String t : tags) {
                sw.filterInfo(t);
                if (sw.sensitiveWordSet.size() > 0) {
                    return new R().errorOf("标签包含敏感词：" + sw.sensitiveWordSet);
                }
            }
        }

        Question question = new Question();
        question.setTitle(title);
        question.setDescription(description);
        question.setTag(tag);
        question.setCreator(currentUser.getId());
        question.setCategory(category);
        question.setTopic(topic); //没加入就是0
        question.setUser(currentUser);
        question.setGmtCreate(System.currentTimeMillis());
        return questionService.saveOrUpdate(question);
    }

    /**
     * 修改帖子
     * @param questionId
     * @param map
     * @param model
     * @return
     */
    @GetMapping("/publish/{id}")
    public String editQuestion(@PathVariable("id") Integer questionId, Map<String, Object> map, Model model) {
        Question question = questionService.getQuestionById(questionId);
        List<TagsCache> tagsCache = TagsCache.getTagsCache();
        model.addAttribute("tagsCache", tagsCache);
        map.put("title", question.getTitle());
        map.put("description",question.getDescription());
        map.put("tag",question.getTag());
        map.put("id",questionId);
        map.put("category",question.getCategory());
        return "publish";
    }

    @PostMapping("/publish/{id}")
    public String doUpdate(@PathVariable("id") Integer questionId,
                           @RequestParam("title") String title,
                           @RequestParam("description") String description,
                           @RequestParam("tag") String tag) {
        Question question = questionService.getQuestionById(questionId);
        question.setTitle(title);
        question.setDescription(description);
        question.setGmtModified(System.currentTimeMillis());
        question.setTag(tag);
        int result = questionService.updateQuestion(question);
        if (result > 0) return "redirect:/";
        else throw new MyException(ResultCode.NOT_OK);
    }
}
