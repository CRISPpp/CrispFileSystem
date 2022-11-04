package cn.crisp.filesystem.common;


import cn.crisp.filesystem.exception.SystemLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {
    @ExceptionHandler(SystemLockException.class)
    public R<String> doSystemException(SystemLockException exception){
        exception.printStackTrace();
        //记录日志
        log.info("SystemLockError");

        return R.errorWithCode(exception.getCode(), exception.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public R<String> OtherException(Exception exception){
        exception.printStackTrace();
        //记录日志
        log.info("gg");

        return R.errorWithCode(CODE.UnknownError, "系统寄了");
    }
}
