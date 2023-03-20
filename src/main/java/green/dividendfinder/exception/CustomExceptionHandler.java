package green.dividendfinder.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice  // 서비스에서 발생한 에러를 잡아서 리스폰스로 던져줄 수 있다
public class CustomExceptionHandler {

    @ExceptionHandler(AbstractException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(AbstractException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getStatusCode())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(e.getStatusCode()));
    }

}
