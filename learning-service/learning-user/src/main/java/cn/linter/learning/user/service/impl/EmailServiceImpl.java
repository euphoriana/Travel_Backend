package cn.linter.learning.user.service.impl;

import cn.linter.learning.user.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean sendCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("1563086482@qq.com");
            message.setTo(email);
            message.setSubject("【旅游社】您的专属验证码已送达 ✈️");
            message.setText("亲爱的用户：\n\n"
                    + "您好！欢迎使用【旅游社】服务。😊\n\n"
                    + "您的验证码是：" + code + "\n\n"
                    + "请在 5分钟 内完成验证，以保障账户安全。\n\n"
                    + "如非本人操作🙅‍♂️，请忽略此邮件。\n\n"
                    + "祝您旅途愉快，探索精彩世界！🌍\n\n"
                    + "-- 旅游社客户服务团队");
            mailSender.send(message);

            redisTemplate.opsForValue().set("email:code:" + email, code, 5, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
