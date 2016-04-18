package client.clientexceptions;

/**
 * Created by zfh on 16-3-26.
 */
public class IllegalParserStatusException extends IllegalStateException{
    public IllegalParserStatusException() {
    }

    public IllegalParserStatusException(String s) {
        super(s);
    }

    public IllegalParserStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalParserStatusException(Throwable cause) {
        super(cause);
    }
}
