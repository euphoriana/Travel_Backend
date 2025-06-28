package cn.linter.learning.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 响应状态枚举类
 *
 * @author wangxiaoyang
 * @date 2020/12/19
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResultStatus {

    /**
     * 响应状态
     */
    SUCCESS("0000", "操作成功"),
    UNAUTHORIZED("1000", "未授权"),
    FORBIDDEN("1001", "权限不足"),
    TOKEN_IS_INVALID("1002", "Token无效"),
    ARGUMENT_NOT_VALID("2000", "参数无效"),
    USER_NOT_FOUND("3000", "用户不存在"),
    USERNAME_EXISTS("3001", "用户名已存在"),
    COURSE_NOT_BUY("4001", "课程未购买"),
    PAYMENT_CREATE_FAILURE("5001", "支付创建失败"),
    EMAIL_SEND_FAILED("3002", "验证码发送失败"),
    EMAIL_CODE_INVALID("3003", "验证码错误"),
    DATABASE_ERROR("3004", "数据库错误");
    /**
     * 状态码
     */
    private final String code;
    /**
     * 状态信息
     */
    private final String message;

    ResultStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}