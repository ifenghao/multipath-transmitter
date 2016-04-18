package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-3-10.
 */
enum SendStatus {
    SEND_REQUEST, WAIT_RESPONSE,
    ACCEPT_BAD_REQUEST("Bad Request"),ACCEPT_SERVICE_UNAVAILABLE("Service Unavailable"),
    ACCEPT_OK("OK"), SENDING, WAIT_DONE, ACCEPT_DONE("Done"), FINISHED;
    private String response;

    private SendStatus() {
    }

    private SendStatus(String response) {
        this.response = response;
    }

    public static SendStatus getMatchedStatus(String response){
        for (SendStatus sendStatus:SendStatus.values()){
            if (response.equals(sendStatus.getResponse())){
                return sendStatus;
            }
        }
        throw new RuntimeException("no this status");
    }

    public String getResponse() {
        return response;
    }
}

public class ClientSendParser extends Parser {
    private SendStatus status;

    public ClientSendParser(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void parse(byte[] array) {
        String response = new String(array);
        FieldReader fieldReader=new FieldReader(response);
        if (fieldReader.response != null) {
            status=SendStatus.getMatchedStatus(fieldReader.response);
        }else {// 没有接收到服务器的响应引起异常

        }
    }

    public void attachRequestAndChangeWrite(SelectionKey key, String header) {
        ByteBuffer headerBuffer = ByteBuffer.wrap(header.getBytes());
        key.attach(headerBuffer);
        key.interestOps(SelectionKey.OP_WRITE);
        this.status = SendStatus.SEND_REQUEST;
    }

    public void attachContentAndChangeWrite(SelectionKey key, ContentBuilder cb) {
        ByteBuffer contentBuffer = ByteBuffer.wrap(cb.getContent());
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

    public void setFinished(){
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
