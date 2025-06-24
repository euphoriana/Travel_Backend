package cn.linter.learning.user.service;

public interface EmailService {
    boolean sendCode(String email);
}