package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-3-14.
 */
enum GetStatus {
    PUTTING, WAIT_DONE, FINISHED
}

public class GetParser extends Parser {
    private GetStatus status;

    public GetParser(SocketChannel channel) {
        super(channel);
        this.status = GetStatus.PUTTING;
    }

    @Override
    public void parse(byte[] array) {
        String response = new String(array);
        FieldReader fieldReader = new FieldReader(response);
        if ((fieldReader.response != null) && fieldReader.response.equals("Done")) {
            status = GetStatus.FINISHED;
        } else {// 对于客户端出现异常的处理
            throw new NullPointerException("null response");
        }
    }

    public void attachAndChangeWrite(SelectionKey key, ContentBuilder cb) {
        ByteBuffer buffer = ByteBuffer.wrap(cb.getContent());
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);// 转换为写模式发送子文件的头部和数据
        status = GetStatus.PUTTING;
    }

    public void changeReadAndWaitDone(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        status = GetStatus.WAIT_DONE;
    }

    public void closeChannelAndCancelKey(SelectionKey key) throws IOException {
        getChannel().close();
        key.cancel();
    }

    public GetStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "GetParser{" +
                "status=" + status +
                ", channel=" + getChannel() +
                '}';
    }
}
