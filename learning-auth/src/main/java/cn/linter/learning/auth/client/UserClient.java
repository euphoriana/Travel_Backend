package cn.linter.learning.auth.client;

import cn.linter.learning.auth.entity.EmailRequest;
import cn.linter.learning.auth.entity.RegisterWrapper;
import cn.linter.learning.auth.entity.User;
import cn.linter.learning.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务接口
 *
 * @author wangxiaoyang
 * @date 2020/12/20
 */
@FeignClient("user-service")
public interface UserClient {

    /**
     * 查询用户
     *
     * @param username 用户名
     * @return 用户
     */
    @GetMapping("users/{username}")
    Result<User> queryUser(@PathVariable String username);

    /**
     * 新增用户
     *
     * @param registerWrapper 用户实例
     * @return 用户
     */
    @PostMapping("users")
    Result<?> createUser(@RequestBody RegisterWrapper registerWrapper);
    /**
     * 发送邮箱验证码
     */
    @PostMapping("users/sendEmailCode")
    Result<?> sendEmailCode(@RequestBody EmailRequest request);

}
