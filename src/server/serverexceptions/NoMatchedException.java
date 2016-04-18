package server.serverexceptions;

import java.util.NoSuchElementException;

/**
 * Created by zfh on 16-3-26.
 */
public class NoMatchedException extends NoSuchElementException {
    public NoMatchedException() {
        super();
    }

    public NoMatchedException(String s) {
        super(s);
    }
}
