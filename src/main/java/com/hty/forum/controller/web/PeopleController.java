package com.hty.forum.controller.web;

import com.github.pagehelper.PageInfo;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.service.QuestionService;
import com.hty.forum.service.UserService;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * create by Semineces on 2020-10-08
 */
@Controller
public class PeopleController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @GetMapping("/people")
    public String people(@RequestParam("id") String id,
                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                         @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                         Map<String, Object> map,
                         HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute("user");
        if (id == null) throw new MyException(ResultCode.PEOPLE_DOT_HAVE);
        Integer peopleId = Integer.parseInt(id);
        //如果他人是自己，直接跳转到自己的页面
        if (currentUser != null && currentUser.getId().equals(peopleId)) {
            return "redirect:/profile";
        }
        //他的问题
        PageInfo<Question> questionPageInfo;
        User user = userMapper.selectByPrimaryKey(peopleId);
        if (user != null) {
            questionPageInfo = questionService.getQuestionByUserId(pageNo, pageSize, user.getId());
        } else throw new MyException(ResultCode.PEOPLE_DOT_HAVE);
        //他关注的人
        List<User> followList = userService.getFollowList(user);
        //他的积分
        Long integral = userMapper.getIntegral(peopleId);
        if (integral == null) integral = (long) 0;
        //他的粉丝
        List<User> fanList = userService.getFansList(user);
        map.put("userList", followList);
        map.put("fansList", fanList);
        map.put("id", peopleId);
        map.put("integral", integral);
        map.put("people", user);
        map.put("questionCount", questionPageInfo.getTotal());
        map.put("page", questionPageInfo);
        return "people";
    }
}
