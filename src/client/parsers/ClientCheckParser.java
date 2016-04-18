package client.parsers;

import client.utils.FieldReader;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-4-3.
 */
public class ClientCheckParser extends Parser {
    private CheckStatus status;
    private StringBuilder filesBuilder = new StringBuilder();
    private int filesLength;
    private int readLength = 0;

    public ClientCheckParser(SocketChannel channel) {
        super(channel);
        this.status = CheckStatus.SEND_REQUEST;
    }

    @Override
    public void parse(byte[] array) {
        if (status == CheckStatus.WAIT_RESPONSE) {
            String header = new String(array);
            FieldReader fieldReader = new FieldReader(header);
            if (fieldReader.response != null) {
                status = CheckStatus.getMatchedStatus(fieldReader.response);
                if (status != CheckStatus.WAIT_HEADER) {
                    return;
                }
            } else {// 没有接收到服务器的响应引起异常
                throw new NullPointerException("null response");
            }
        }
        if (status == CheckStatus.WAIT_HEADER) {
            String header = new String(array);
            int headerEnd = header.indexOf("\r\n\r\n");
            header = header.substring(0, headerEnd + 4);
            FieldReader fieldReader = new FieldReader(header);
            if (fieldReader.filesLength == null) {
                return;// 本次没有接收到首部，等待下一次接收
            }
            this.filesLength = Integer.parseInt(fieldReader.filesLength);
            int restLength = array.length - (headerEnd + 4);
            byte[] restFiles = new byte[restLength];
            System.arraycopy(array, headerEnd + 4, restFiles, 0, restLength);
            array = restFiles;
            status = CheckStatus.CHECKING;
        }
        if (status == CheckStatus.CHECKING) {
            String files = new String(array);
            filesBuilder.append(files);
            readLength += array.length;
            if (readLength == filesLength) {
                status = CheckStatus.CHECK_OVER;
            }
        }
    }

    public void changeReadAndWaitResponse(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        status = CheckStatus.WAIT_RESPONSE;
    }

    public void closeChannelAndCancelKey(SelectionKey key) throws IOException {
        getChannel().close();
        key.cancel();
        status = CheckStatus.FINISHED;
    }

    public String getFiles() {
        return filesBuilder.toString();
    }

    public CheckStatus getStatus() {
        return status;
    }
}
