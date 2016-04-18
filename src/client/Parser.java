package client;

import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-3-14.
 */
public abstract class Parser {
    private final SocketChannel channel;

    public Parser(SocketChannel channel) {
        this.channel = channel;
    }

    public abstract void parse(byte[] array);

    public SocketChannel getChannel() {
        return channel;
    }
}
