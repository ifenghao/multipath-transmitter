package client.parsers;

import client.utils.ContentBuilder;
import client.utils.FieldReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-3-10.
 */
public class ClientSendParser extends Parser {
    private SendStatus status;
    private String totalResponse = "";

    public ClientSendParser(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void parse(byte[] array) {
        String response = new String(array);
        totalResponse += response;// 可能一次没有全部接收完整响应
        FieldReader fieldReader = new FieldReader(totalResponse);
        if (fieldReader.response != null) {
            status = SendStatus.getMatchedStatus(fieldReader.response);
            totalResponse = "";
        } else {
            for (String responseString : SendStatus.listResponse()) {
                if (("Response:" + responseString + "\r\n").contains(totalResponse)) {
                    return;// 可能没有一次全部接收完整响应
                }
            }
            throw new RuntimeException("server response error " + totalResponse);// 接收到服务器错误响应
        }
    }

    public void attachRequestAndChangeWrite(SelectionKey key, String header) {
        ByteBuffer headerBuffer = ByteBuffer.wrap(header.getBytes());
        key.attach(headerBuffer);
        key.interestOps(SelectionKey.OP_WRITE);
        this.status = SendStatus.SEND_REQUEST;
    }

    public void attachContentAndChangeWrite(SelectionKey key, byte[] content) {
        ByteBuffer contentBuffer = ByteBuffer.wrap(content);
        key.attach(contentBuffer);
        key.interestOps(SelectionKey.OP_WRITE);
        this.status = SendStatus.SENDING;
    }

    public void changeReadAndWaitResponse(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        this.status = SendStatus.WAIT_RESPONSE;
    }

    public void changeReadAndWaitDone(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        this.status = SendStatus.WAIT_DONE;
    }

    public void closeChannelAndCancelKey(SelectionKey key) throws IOException {
        getChannel().close();
        key.cancel();
    }

    public void setFinished() {
        this.status = SendStatus.FINISHED;
    }

    public SendStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ClientSendParser{" +
                "status=" + status +
                ", channel=" + getChannel() +
                '}';
    }
}
