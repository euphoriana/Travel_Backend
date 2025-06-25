package cn.linter.learning.trade.service.impl;

import cn.linter.learning.trade.service.EmailService;
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
    public boolean sendPaymentSuccessNotice(String email, String courseName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("1563086482@qq.com");            // 发件人
            message.setTo(email);                            // 收件人
            message.setSubject("【旅游社】报团成功通知 🧳✨");   // 邮件标题
            message.setText(
                    "尊敬的旅客您好：\n\n" +
                            "🎉 恭喜您已成功报名旅行团：《" + courseName + "》。\n\n" +
                            "感谢您选择【旅游社】！我们的工作人员将在 24 小时内与您确认行程细节。\n\n" +
                            "📅 出发日期 / 集合地点 等信息可在 App「我的订单」中随时查看。\n\n" +
                            "🤝 如需修改出行人信息、升级房型或了解签证/保险等事宜，请及时联系客服：\n" +
                            "   · 电话：400-888-8888\n" +
                            "   · 邮箱：service@your-travel.com\n\n" +
                            "祝您旅途愉快，探索精彩世界！🌍\n\n" +
                            "-- 【旅游社】客户服务团队"
            );
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("发送支付成功邮件失败: {}", e.getMessage(), e);
            return false;
        }
    }

}
