package com.ultrakid.ultratools.exception;

/**
 * 自定义业务异常
 *
 * @author ultrakid
 * @version 1.0
 * @date 2020/08/21 10:21
 */
public class UltraRuntimeException extends RuntimeException {

    public UltraRuntimeException() {
        super();
    }

    /**
     * 构造方法
     *
     * @param message 错误信息
     */
    public UltraRuntimeException(String message) {
        super(message);
    }

    /**
     * 构造方法
     *
     * @param cause 错误原因
     */
    public UltraRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造方法
     *
     * @param message 错误信息
     * @param cause   错误原因
     */
    public UltraRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
