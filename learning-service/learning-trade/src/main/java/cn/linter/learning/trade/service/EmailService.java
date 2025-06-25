package cn.linter.learning.trade.service;

public interface EmailService {
    boolean sendPaymentSuccessNotice(String email, String courseName);
}