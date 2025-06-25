package cn.linter.learning.user.controller;

import cn.linter.learning.auth.entity.EmailRequest;
import cn.linter.learning.auth.entity.RegisterWrapper;
import cn.linter.learning.common.entity.Page;
import cn.linter.learning.common.entity.Result;
import cn.linter.learning.common.entity.ResultStatus;
import cn.linter.learning.user.client.CourseClient;
import cn.linter.learning.user.entity.Course;
import cn.linter.learning.user.entity.Note;
import cn.linter.learning.user.entity.User;
import cn.linter.learning.user.service.UserService;
import cn.linter.learning.user.service.EmailService;
import com.github.pagehelper.PageInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 用户控制器
 *
 * @author wangxiaoyang
 * @since 2020/11/01
 */
@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;
    private final CourseClient courseClient;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    public UserController(UserService userService, CourseClient courseClient, EmailService emailService,StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.courseClient = courseClient;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("{username}")
    public Result<User> queryUser(@PathVariable String username) {
        User user = userService.queryByUsername(username);
        return Result.of(ResultStatus.SUCCESS, user);
    }

    @GetMapping("{username}/courses")
    public Result<Page<Course>> listCoursesOfUser(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize,
                                                  @PathVariable("username") String studentName) {
        return courseClient.listCoursesByStudentName(pageNum, pageSize, studentName);
    }

    @GetMapping("{username}/notes")
    public Result<Page<Note>> listNotesOfUser(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize,
                                              @PathVariable String username) {
        return courseClient.listNotesByUsername(pageNum, pageSize, username);
    }

    @GetMapping
    public Result<Page<User>> listUser(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<User> pageInfo = userService.list(pageNum, pageSize);
        return Result.of(ResultStatus.SUCCESS, Page.of(pageInfo.getList(), pageInfo.getTotal()));
    }

    @PostMapping
    public Result<?> createUser(@RequestBody RegisterWrapper registerWrapper) {
        String email = registerWrapper.getEmailAddress();
        String code = registerWrapper.getVerificationCode();

        // 从 Redis 中获取真实验证码
        String cacheKey = "email:code:" + email;
        String realCode = redisTemplate.opsForValue().get(cacheKey);

        if (realCode == null || !realCode.equals(code)) {
            return Result.of(ResultStatus.EMAIL_CODE_INVALID, null);
        }

        // 构造 User 对象
        User user = new User();
        user.setEmailAddress(email);
        user.setUsername(registerWrapper.getUsername());
        user.setPassword(registerWrapper.getPassword());
        redisTemplate.opsForValue().set("user:email:" + registerWrapper.getUsername(), email);

        // 创建用户
        User createdUser = userService.create(user);
        return Result.of(ResultStatus.SUCCESS, createdUser);
    }

    //新增发送邮箱验证
    @PostMapping("/sendEmailCode")
    public Result<?> sendEmailCode(@RequestBody EmailRequest request) {
        String email = request.getEmail();
        boolean success = emailService.sendCode(email);
        return Result.of(success ? ResultStatus.SUCCESS : ResultStatus.EMAIL_SEND_FAILED, null);
    }


    @PutMapping
    public Result<User> updateUser(@RequestBody @Validated({User.Update.class}) User user) {
        User updatedUser = userService.update(user);
        return Result.of(ResultStatus.SUCCESS, updatedUser);
    }

    @DeleteMapping("{username}")
    public ResultStatus deleteUser(@PathVariable String username) {
        userService.delete(username);
        return ResultStatus.SUCCESS;
    }

}