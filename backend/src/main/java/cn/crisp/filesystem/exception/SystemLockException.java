package cn.crisp.filesystem.exception;

public class SystemLockException extends RuntimeException{
    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public SystemLockException(Integer code, String message){
        super(message);
        this.code = code;
    }

    public SystemLockException(Integer code, String message, Throwable cause){
        super(message, cause);
        this.code = code;
    }
}
