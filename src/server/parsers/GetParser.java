package server.parsers;

import server.utils.FieldReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-3-14.
 */
public class GetParser extends Parser {
    private GetStatus status;
    private boolean ResponseError = false;
    private String totalResponse = "";

    public GetParser(SocketChannel channel) {
        super(channel);
        this.status = GetStatus.PUTTING;
    }

    @Override
    public void parse(byte[] array) {
        String response = new String(array);
        totalResponse += response;// 可能一次没有全部接收完整响应
        FieldReader fieldReader = new FieldReader(totalResponse);
        if ((fieldReader.response != null) && fieldReader.response.equals("Done")) {
            status = GetStatus.FINISHED;
            totalResponse = "";
        } else {
            if (("Response:Done\r\n").contains(totalResponse)) {
                return;// 可能没有一次全部接收完整响应
            }
            ResponseError = true;// 对于客户端出现异常的处理
        }
    }

    public void attachAndChangeWrite(SelectionKey key, byte[] content) {
        ByteBuffer buffer = ByteBuffer.wrap(content);
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

    public boolean isResponseError() {
        return ResponseError;
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
