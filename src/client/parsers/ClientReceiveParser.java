package client.parsers;

import client.utils.FieldReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-2-28.
 */
public class ClientReceiveParser extends Parser {
    private String pathRootSave;
    private String encoding;
    private String fileName;
    private long packageNumber;
    private long totalPackages;
    private int subFileLength;
    private long totalFileLength;
    private String totalHeader = "";
    private String totalResponse = "";
    private int alreadyReadLength = 0;
    private ReceiveStatus status;

    public ClientReceiveParser(SocketChannel channel, String pathRootSave, boolean needSentRequest) {
        super(channel);
        if (!pathRootSave.endsWith(File.separator)) {
            pathRootSave += File.separator;
        }
        this.pathRootSave = pathRootSave;
        if (needSentRequest) {
            this.status = ReceiveStatus.SEND_REQUEST;
        } else {
            this.status = ReceiveStatus.WAIT_HEADER;
        }
    }

    @Override
    public void parse(byte[] array) {
        if (status == ReceiveStatus.WAIT_RESPONSE) {
            String response = new String(array);
            totalResponse += response;
            FieldReader fieldReader = new FieldReader(totalResponse);
            if (fieldReader.response != null) {
                status = ReceiveStatus.getMatchedStatus(fieldReader.response);
                totalResponse = "";
                if (status != ReceiveStatus.WAIT_HEADER) {
                    return;
                }
            } else {
                System.out.println(totalResponse);
                for (String responseString : ReceiveStatus.listResponse()) {
                    if (("Response:" + responseString + "\r\n").contains(totalResponse)) {
                        return;// 可能没有一次全部接收完整响应
                    }
                }
                throw new RuntimeException("server response error " + totalResponse);// 接收到服务器错误响应
            }
        }
        if (status == ReceiveStatus.WAIT_HEADER) {// 再解析子文件首部
            String header = new String(array);
            int headerEnd = header.indexOf("\r\n\r\n");
            if (headerEnd == -1) {// 子文件首部没有一次发送完成
                totalHeader += header;
                return;
            }
            totalHeader += header.substring(0, headerEnd + 4);
            FieldReader fieldReader = new FieldReader(totalHeader);
            fileName = fieldReader.fileName;
            encoding = fieldReader.encoding;
            String packageInfo = fieldReader.packageInfo;
            String lengthInfo = fieldReader.LengthInfo;
            if (fileName == null || encoding == null || packageInfo == null || lengthInfo == null) {
                return;// 本次没有接收到子文件首部，等待下一次接收
            }
            int solidus1 = packageInfo.indexOf("/");
            this.packageNumber = Long.parseLong(packageInfo.substring(0, solidus1));
            this.totalPackages = Long.parseLong(packageInfo.substring(solidus1 + 1));
            int solidus2 = lengthInfo.indexOf("/");
            this.subFileLength = Integer.parseInt(lengthInfo.substring(0, solidus2));
            this.totalFileLength = Long.parseLong(lengthInfo.substring(solidus2 + 1));
            fileName += ".tmp" + packageNumber;// 子文件附属编号
            int restLength = array.length - (headerEnd + 4);
            byte[] restFileData = new byte[restLength];
            System.arraycopy(array, headerEnd + 4, restFileData, 0, restLength);
            array = restFileData;
            status = ReceiveStatus.RECEIVING;
        }
        if (status == ReceiveStatus.RECEIVING) {// 最后接收文件内容
            try {// 每次写入文件时都要新建输出流
                FileOutputStream fileOut = new FileOutputStream(pathRootSave + fileName, alreadyReadLength > 0);
                fileOut.write(array);
                fileOut.flush();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            alreadyReadLength += array.length;
            if (alreadyReadLength > subFileLength) {
                throw new RuntimeException("alreadyReadLength > subFileLength");
            }
            if (alreadyReadLength == subFileLength) {
                status = ReceiveStatus.RECEIVE_OVER;
            }
        }
    }

    public void changeReadAndWaitResponse(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        status = ReceiveStatus.WAIT_RESPONSE;
    }

    public void changeWriteAndRespondDone(SelectionKey key) {
        String response = "Response:Done\r\n";
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);// 转换为写模式发送响应
        status = ReceiveStatus.RESPOND_DONE;
    }

    public void finishAndChangeRead(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        status = ReceiveStatus.FINISHED;
    }

    public void closeChannelAndCancelKey(SelectionKey key) throws IOException {
        status = null;
        getChannel().close();
        key.cancel();
    }

    public String getPathRootSave() {
        return pathRootSave;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getFileName() {
        return fileName;
    }

    public long getPackageNumber() {
        return packageNumber;
    }

    public long getTotalPackages() {
        return totalPackages;
    }

    public int getSubFileLength() {
        return subFileLength;
    }

    public long getTotalFileLength() {
        return totalFileLength;
    }

    public int getAlreadyReadLength() {
        return alreadyReadLength;
    }

    public ReceiveStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ClientReceiveParser{" +
                "fileName='" + fileName + '\'' +
                ", packageNumber=" + packageNumber +
                "/" + totalPackages +
                ", subFileLength=" + subFileLength +
                "/" + totalFileLength +
                ", alreadyReadLength=" + alreadyReadLength +
                ", status=" + status +
                ", channel=" + getChannel() +
                '}';
    }
}
