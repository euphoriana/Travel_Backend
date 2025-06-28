package cn.linter.learning.user.service.impl;

import cn.linter.learning.common.entity.ResultStatus;
import cn.linter.learning.common.exception.BusinessException;
import cn.linter.learning.user.dao.UserDao;
import cn.linter.learning.user.entity.Role;
import cn.linter.learning.user.entity.User;
import cn.linter.learning.user.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author wangxiaoyang
 * @since 2020/11/01
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User queryByUsername(String username) {
        return userDao.selectByUsername(username);
    }

    @Override
    public PageInfo<User> list(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(userDao.list());
    }

    @Override
    public User create(User user) {
        if (userDao.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultStatus.USERNAME_EXISTS);
        }
        String rawPassword = user.getPassword();
        if (rawPassword != null) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        if (user.getRole() == null || user.getRole().getId() == null) {
            Role role = new Role();
            role.setId(1);
            user.setRole(role);
        }
        if (user.getProfilePicture() == null) {
            user.setProfilePicture("http://localhost:9000//profile-picture/default.jpg");
        }
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userDao.insert(user);
        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = userDao.selectByUsername(user.getUsername());
        if (oldUser != null && !oldUser.getUsername().equals(user.getUsername())) {
            throw new BusinessException(ResultStatus.USERNAME_EXISTS);
        }
        String rawPassword = user.getPassword();
        if (rawPassword != null) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        user.setUpdateTime(LocalDateTime.now());
        userDao.update(user);
        return queryByUsername(user.getUsername());
    }

    @Override
    public boolean delete(String username) {
        try {
            System.out.println("Attempting to delete user with username: " + username);
            int rowsAffected = userDao.delete(username);
            if (rowsAffected > 0) {
                return true; // 删除成功
            } else {
                // 如果没有删除记录，可以抛出异常或返回失败状态
                throw new BusinessException(ResultStatus.USER_NOT_FOUND); // 用户不存在或未找到
            }
        } catch (Exception e) {
            // 捕获异常并返回失败状态
            throw new BusinessException(ResultStatus.DATABASE_ERROR); // 数据库错误
        }
    }
}