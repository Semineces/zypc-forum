package com.hty.forum.controller.web;

import com.github.pagehelper.PageInfo;
import com.hty.forum.cache.HotTagCache;
import com.hty.forum.dto.NewUserDto;
import com.hty.forum.dto.QuestionDto;
import com.hty.forum.dto.QuestionQueryDto;
import com.hty.forum.entity.Question;
import com.hty.forum.entity.User;
import com.hty.forum.entity.example.UserExample;
import com.hty.forum.exception.MyException;
import com.hty.forum.mapper.UserMapper;
import com.hty.forum.myenum.ResultCode;
import com.hty.forum.myenum.UserType;
import com.hty.forum.service.QuestionService;
import com.hty.forum.service.UserService;
import com.hty.forum.utils.MD5Util;
import com.hty.forum.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * create by Semineces on 2020-10-07
 */
@Controller
public class IndexController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private HotTagCache hotTagCache;

    /**
     * 主页
     * @param tag 标签
     * @param search 搜索
     * @param category 分类
     * @param map
     * @return
     */
    @RequestMapping("/")
    public String index(@RequestParam(name = "tag", required = false) String tag,
                        @RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "category", defaultValue = "0") String category,
                        Map<String, Object> map) {
        map.put("tag", tag);
        map.put("search", search);
        map.put("category", category);
        map.put("navLi","find");
        return "index";
    }

    /**
     * 加载帖子数据
     * @param sort 排序方式
     * @param search 搜索内容
     * @param tag 标签
     * @param pageSize 每页大小
     * @param pageNo 第几页
     * @param categoryStrVal 分类信息
     * @param request
     * @return
     */
    @ResponseBody
    @GetMapping("/loadQuestionList")
    public R listQuestion(@RequestParam(name = "sortby", defaultValue = "ALL") String sort,
                          @RequestParam(name = "search", required = false) String search,
                          @RequestParam(name = "tag", required = false) String tag,
                          @RequestParam(name = "pageSize", defaultValue = "30") Integer pageSize,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "category", defaultValue = "0") String categoryStrVal,
                          HttpServletRequest request) {
        Integer category = Integer.parseInt(categoryStrVal);
        //获取首页数据（分页）
        QuestionQueryDto questionQueryDto = new QuestionQueryDto();
        questionQueryDto.setSearch(search);
        questionQueryDto.setSort(sort);
        questionQueryDto.setTag(tag);
        questionQueryDto.setCategory(category);
        questionQueryDto.setPageNo(pageNo);
        questionQueryDto.setPageSize(pageSize);
        //根据设置的条件dto去查询分页数据
        PageInfo<Question> questionPageInfo = questionService.getPageBySearch(questionQueryDto);
        request.getSession().setAttribute("category", category); //全部
        return new R().okOf().addMsg("page", questionPageInfo);
    }

    /**
     * 加载右边数据
     * @return
     */
    @ResponseBody
    @GetMapping("/loadRightList")
    public R loadRightList() {
        //最新用户，限定为6个，可以通过管理平台修改
        List<NewUserDto> userList = userService.findNewUsers(6);
        //热门标签
        List<String> hot = hotTagCache.updateTags();
        //List<String> hotRedis = redisTemplate.opsForList().findNewQuestion(7);
        //最新帖子
        List<QuestionDto> newQuestions = questionService.findNewQuestions(6);
        //帖子推荐
        Integer pageNo = 1, pageSize = 0;
        List<QuestionDto> recommendQuestions = questionService.findRecommendQuestions(pageNo, pageSize);
        return new R().okOf()
                .addMsg("userList", userList)
                .addMsg("newQuestions", newQuestions)
                .addMsg("tag", hot)
                .addMsg("recommend", recommendQuestions);
    }

    /**
     * 校验用户名是否已经被使用过
     * @param username 用户名
     * @return
     */
    @ResponseBody
    @GetMapping("/ajaxNameUsed")
    public R ajaxNameUsed(@RequestParam("username") String username) {
        if (StringUtils.isEmpty(username)) return new R().errorOf(ResultCode.NOT_OK);
        UserExample example = new UserExample();
        example.createCriteria().andNameEqualTo(username.trim());
        List<User> users = userMapper.selectByExample(example);
        return users.size() > 0 ? new R().errorOf(ResultCode.USERNAME_IS_USED) : new R().okOf();
    }

    /**
     * 注册
     * @param name 用户名
     * @param phone 手机号
     * @param password1 密码
     * @param password2 再次输入密码
     * @param code 验证码
     * @param request
     * @param response
     * @return
     */
    @ResponseBody
    @PostMapping("/register")
    public R register(@RequestParam("name") String name,
                      @RequestParam("phone") String phone,
                      @RequestParam("password1") String password1,
                      @RequestParam("password2") String password2,
                      @RequestParam("code") String code,
                      HttpServletRequest request,
                      HttpServletResponse response) {
        //获取并校验验证码
        String regCode = (String) request.getSession().getAttribute("regCode");
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(regCode) || !regCode.equalsIgnoreCase(code)) {
            return new R().errorOf(ResultCode.CODE_NOT_SUCCESS);
        }
        //校验用户名
        if (StringUtils.isEmpty(name) || name.trim().equals("")) {
            return new R().errorOf(ResultCode.USER_NAME_NOT_EMPTY);
        }
        //校验密码
        if (StringUtils.isEmpty(password1) ||
                StringUtils.isEmpty(password2) ||
                "".equals(password1.trim()) ||
                "".equals(password2.trim())) {
            return new R().errorOf(ResultCode.NOT_OK);
        }
        if (!password1.equals(password2)) {
            return new R().errorOf(ResultCode.PASSWORD_TWICE_EQ);
        }
        //校验电话号码
        //TODO：应该在后端也加上一个正则校验一下是否为合法电话号码
        if (StringUtils.isEmpty(phone) || "".equals(phone.trim())) {
            return new R().errorOf(ResultCode.PHONE_NOT_EMPTY);
        }
        //验证用户名是否可用
        //TODO：将该方法写在service里面
        UserExample example = new UserExample();
        example.createCriteria().andNameEqualTo(name);
        List<User> users = userMapper.selectByExample(example);
        if (users.size() > 0) {
            return new R().errorOf(ResultCode.USERNAME_IS_USED);
        }
        example.clear();
        //验证电话号码是否已经被注册
        //TODO:将该方法写在service里面
        example.createCriteria().andAccountIdEqualTo(phone);
        List<User> users1 = userMapper.selectByExample(example);
        if (users1.size() > 0) {
            return new R().errorOf(ResultCode.THIS_PHONE_USED);
        }
        //执行注册
        User user = new User();
        user.setUserType(UserType.PHONE.getCode());
        user.setName(name);
        user.setAccountId(phone);
        user.setAvatarUrl("https://semineces-online.oss-cn-shanghai.aliyuncs.com/2020/08/11/41420c5d8ddc443e90a47465a029e590file.png");
        user.setGmtCreate(System.currentTimeMillis());
        user.setGmtModified(System.currentTimeMillis());
        //将密码MD5加密后进行存入
        try {
            String password = MD5Util.encodeByMd5(password1);
            user.setPassword(password);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new MyException(ResultCode.NOT_OK);
        }
        //获取用户的token并存入
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        //插入用户
        userMapper.insertSelective(user);
        //将该token保存到本地并设置cookie的最大存活时间为一天，即记住密码操作
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(3600 * 24);
        response.addCookie(cookie);
        //TODO：聊天
        return new R().okOf();
    }

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @PostMapping("/login")
    @ResponseBody
    public R login(@RequestParam("username") String username,
                   @RequestParam("password") String password,
                   @RequestParam("code") String code,
                   HttpServletRequest request) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //获取验证码并校验
        String loginCode = (String) request.getSession().getAttribute("loginCode");
        if (StringUtils.isEmpty(code) || !code.equalsIgnoreCase(loginCode)) {
            return new R().errorOf(ResultCode.CODE_NOT_SUCCESS);
        }
        //校验用户名密码合法性
        if (StringUtils.isEmpty(username) ||
                StringUtils.isEmpty(password) ||
                "".equals(username.trim()) ||
                "".equals(password.trim())) {
            return new R().errorOf(ResultCode.NAME_AND_PASSWORD_CANT_EMPTY);
        }
        //执行登录逻辑
        String md5Password = MD5Util.encodeByMd5(password);
        UserExample example = new UserExample();
        example.createCriteria().andNameEqualTo(username).andPasswordEqualTo(md5Password);
        List<User> users = userMapper.selectByExample(example);
        if (users.size() > 0) {
            User user = users.get(0);
            //登录成功，将用户信息存入session
            long time = System.currentTimeMillis();
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("xlm_uid", user.getId());
            session.setAttribute("xlm_name", user.getName());
            session.setAttribute("xlm_avatar", user.getAvatarUrl());
            session.setAttribute("xlm_time", time);
            return new R().okOf();
        } else return new R().errorOf(ResultCode.LOGIN_FAIL);
    }

    /**
     * 帖子分类
     * @param action 分类的总集合
     * @param pageSize 每页的大小
     * @param pageNo 第几页
     * @param request
     * @param map
     * @return
     */
    @ResponseBody
    @GetMapping("/explore/{action}")
    public String explore(@PathVariable("action") String action,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "15") Integer pageSize,
                          HttpServletRequest request,
                          Map<String, Object> map) {
        //前端是这个样子的category-0 所以我们要分隔开取后面的
        String[] split = action.split("-");
        Integer categoryVal = Integer.parseInt(split[1]);
        if (categoryVal == 0) {
            request.getSession().setAttribute("category", categoryVal);
            return "forward:/";
        }
        PageInfo<Question> questionPageInfo = questionService
                .findQuestionByCategory(pageNo, pageSize, categoryVal);
        //获取最新用户
        List<NewUserDto> newUserDtos = userService.findNewUsers(8);
        //获取最新帖子
        List<QuestionDto> newQuestions = questionService.findNewQuestions(6);

        //渲染页面
        request.getSession().setAttribute("category", categoryVal);
        map.put("sort", "all"); //全部
        map.put("newQuestions", newQuestions);
        map.put("users", newUserDtos);
        map.put("page", questionPageInfo);
        map.put("navLi", "find");
        return "redirect:/index.html";
    }


    /**
     * 退出登录
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.invalidate();
        //清除cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) cookie.setMaxAge(0);
            }
        }
        response.addCookie(new Cookie("token", null));
        return "redirect:/";
    }
}
