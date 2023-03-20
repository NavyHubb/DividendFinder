package green.dividendfinder.exception;

public abstract class AbstractException extends RuntimeException {
    // 이 예외 클래스를 활용하여 custom exception을 구현. 구현체는 impl 패키지 내부에 위치

    abstract public int getStatusCode();
    abstract public String getMessage();

}
