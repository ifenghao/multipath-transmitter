package server.serverexceptions;

import java.io.IOException;

/**
 * Created by zfh on 16-3-26.
 */
public class ReadChannelException extends IOException {
    public ReadChannelException() {
    }

    public ReadChannelException(String message) {
        super(message);
    }

    public ReadChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadChannelException(Throwable cause) {
        super(cause);
    }
}
