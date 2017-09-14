package tech.rsqn.reflectionhelpers;


public class ReflectionException extends RuntimeException {
    public ReflectionException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ReflectionException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ReflectionException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    //protected ReflectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      //  super(message, cause, enableSuppression, writableStackTrace);    //To change body of overridden methods use File | Settings | File Templates.
    //}
}
