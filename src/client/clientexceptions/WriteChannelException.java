package client.clientexceptions;

import java.io.IOException;

/**
 * Created by zfh on 16-3-26.
 */
public class WriteChannelException extends IOException {
    public WriteChannelException() {
    }

    public WriteChannelException(String message) {
        super(message);
    }

    public WriteChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public WriteChannelException(Throwable cause) {
        super(cause);
    }
}
