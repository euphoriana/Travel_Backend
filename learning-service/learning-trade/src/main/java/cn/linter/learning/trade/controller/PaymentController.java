package cn.linter.learning.trade.controller;

import cn.linter.learning.common.entity.Result;
import cn.linter.learning.common.entity.ResultStatus;
import cn.linter.learning.common.exception.BusinessException;
import cn.linter.learning.trade.client.CourseClient;
import cn.linter.learning.trade.entity.Course;
import cn.linter.learning.trade.entity.Order;
import cn.linter.learning.trade.service.OrderService;
import cn.linter.learning.trade.service.EmailService;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 支付控制器
 *
 * @author wangxiaoyang
 * @since 2021/3/19
 */
@RestController
@RequestMapping("payments")
public class PaymentController {

    @Value("${gateway.protocol}")
    private String gatewayProtocol;
    @Value("${gateway.host}")
    private String gatewayHost;

    private final OrderService orderService;
    private final CourseClient courseClient;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    public PaymentController(OrderService orderService, CourseClient courseClient,EmailService emailService,StringRedisTemplate redisTemplate) {
        this.orderService = orderService;
        this.courseClient = courseClient;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    @RequestMapping
    public Result<String> pay(@RequestParam Long orderId) {
        Order order = orderService.queryById(orderId);
        AlipayTradePagePayResponse pagePayResponse;
        try {
            pagePayResponse = Factory.Payment.Page().pay(
                    order.getProductName(), order.getTradeNo(),
                    order.getPrice().toString(),
                    gatewayProtocol + "://" + gatewayHost + "/api/payments/success"
            );
        } catch (Exception e) {
            throw new BusinessException(ResultStatus.PAYMENT_CREATE_FAILURE);
        }
        return Result.of(ResultStatus.SUCCESS, pagePayResponse.getBody());
    }

    @RequestMapping("success")
    public void paymentSuccess(@RequestParam("out_trade_no") String tradeNo, HttpServletResponse response) {
        Order order = orderService.queryByTradeNo(tradeNo);
        order.setStatus(1);
        orderService.update(order);
        Course course = new Course();
        course.setId(order.getProductId());
        course.setRegistered(true);
        courseClient.updateCourse(course.getId(), order.getUsername());

        //发送成功预订的邮件
        String username = order.getUsername();
        String emailKey = "user:email:" + username;
        String email = redisTemplate.opsForValue().get(emailKey);
        // 判断 Redis 中是否拿到 email，拿不到就不发邮件，或可降级处理
        if (email != null && !email.isEmpty()) {
            emailService.sendPaymentSuccessNotice(email, order.getProductName());
        } else {
            // 可选：记录日志或降级查询用户服务
            System.out.println("警告：Redis 中未找到用户名 " + username + " 对应的邮箱，邮件未发送。");
        }

        response.setContentType("text/html; charset=utf8");
        try (PrintWriter out = response.getWriter()) {
            out.print("支付成功，3秒之后跳转回首页" +
                    "<script>setTimeout(()=>window.location.href=\"http://localhost:3000/courses/" +
                    order.getProductId() + "\",3000)</script>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}