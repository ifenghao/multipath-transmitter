package server.parsers;

import server.*;
import server.utils.FieldReader;
import server.utils.FileSearcher;
import server.utils.ServerUtil;
import server.utils.SubContentSlicer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zfh on 16-3-14.
 */
public class RequestParser extends Parser {
    private int clientIdCode;
    private String method;
    private int channelNumber;
    private int totalChannels;
    private String fileName;// 文件原始名称
    private RequestStatus status;
    private String pathRootFind;
    private String pathRootSave;
    private String totalRequest = "";

    public RequestParser(SocketChannel channel, String pathRootFind, String pathRootSave) {
        super(channel);
        this.pathRootFind = pathRootFind;
        this.pathRootSave = pathRootSave;
        this.status = RequestStatus.WAIT_REQUEST;// 只有第一次才可读取请求
    }

    @Override
    public void parse(byte[] array) {// 解析请求中的方法和文件名
        String request = new String(array);
        int headerEnd = request.indexOf("\r\n\r\n");
        if (headerEnd == -1) {// 请求没有一次发送完成
            totalRequest += request;
            return;
        }
        totalRequest += request.substring(0, headerEnd + 4);
        FieldReader fieldReader = new FieldReader(totalRequest);
        if (fieldReader.method != null) {
            this.method = fieldReader.method;
        } else {
            this.status = RequestStatus.RESPOND_BAD_REQUEST;
            return;
        }
        if (fieldReader.clientIdCode != null) {
            this.clientIdCode = Integer.parseInt(fieldReader.clientIdCode);
        } else {
            this.status = RequestStatus.RESPOND_BAD_REQUEST;
            return;
        }
        if (fieldReader.fileName != null) {
            this.fileName = fieldReader.fileName;
        } else {
            this.status = RequestStatus.RESPOND_BAD_REQUEST;
            return;
        }
        if (fieldReader.channelInfo != null) {
            String channelInfo = fieldReader.channelInfo;
            int solidus = channelInfo.indexOf("/");
            this.channelNumber = Integer.parseInt(channelInfo.substring(0, solidus));
            this.totalChannels = Integer.parseInt(channelInfo.substring(solidus + 1));
        } else {
            this.status = RequestStatus.RESPOND_BAD_REQUEST;
            return;
        }
        if (method.equals("GET")) {
            if (FileSearcher.isExist(pathRootFind, fileName)) {
                status = RequestStatus.RESPOND_OK;
            } else {
                status = RequestStatus.RESPOND_NOT_FOUND;
            }
        } else if (method.equals("PUT")) {
            status = RequestStatus.RESPOND_OK;
        } else if (method.equals("CHECK")) {
            status = RequestStatus.RESPOND_OK;
        }
    }

    /**
     * 部署GET方法，第一次创建文件的键值
     *
     * @param getMap
     */
    public void deployGET(Map<GetMapKey, List<GetParser>> getMap) {
        if (!ServerUtil.hasMatchedGetMapKey(this, getMap)) {// 如果已经存在键值则不做处理
            GetMapKey getMapKey = new GetMapKey(fileName, pathRootFind, clientIdCode);
            SubContentSlicer slicer = getMapKey.getSlicer();
            slicer.determineTotalPackage(totalChannels);// 确定分包总数
            List<GetParser> getList = new ArrayList<GetParser>(totalChannels);// 列表长度为通道数
            getMap.put(getMapKey, getList);
        }
    }

    /**
     * 开始GET方法发送数据
     *
     * @param key
     * @param getMap
     */
    public void startGET(SelectionKey key, Map<GetMapKey, List<GetParser>> getMap) {
        GetMapKey getMapKey = ServerUtil.getMatchedGetMapKey(this, getMap);
        getMapKey.addChannel(getChannel());
        SubContentSlicer slicer = getMapKey.getSlicer();
        GetParser gp = new GetParser(getChannel());
        gp.attachAndChangeWrite(key, slicer.next());// 给每个准备好的通道分配子文件
        getMap.get(getMapKey).add(gp);// 记录该通道的解析器
        status = RequestStatus.REQUEST_END;// 请求结束，下一次发送
    }

    /**
     * 部署PUT方法，第一次创建文件的键值
     *
     * @param putMap
     */
    public void deployPUT(Map<PutMapKey, List<PutParser>> putMap) {
        if (!ServerUtil.hasMatchedPutMapKey(this, putMap)) {
            PutMapKey putMapKey = new PutMapKey(fileName, pathRootSave, clientIdCode);
            List<PutParser> putList = new ArrayList<PutParser>();// 列表长度为分包数
            putMap.put(putMapKey, putList);
        }
    }

    /**
     * 开始PUT方法接收数据
     *
     * @param key
     * @param putMap
     */
    public void startPUT(SelectionKey key, Map<PutMapKey, List<PutParser>> putMap) {
        PutMapKey putMapKey = ServerUtil.getMatchedPutMapKey(this, putMap);
        putMapKey.addChannel(getChannel());
        PutParser pp = new PutParser(getChannel(), putMapKey.getPathRootSaveChanged());// 所有同名文件都有单独的文件夹
        pp.changeReadAndGet(key);// 准备读取子文件
        putMap.get(putMapKey).add(pp);// 记录该子文件的解析器
        status = RequestStatus.REQUEST_END;// 请求结束，下一次发送
    }

    /**
     * 开始CHECK返回文件列表
     *
     * @param key
     */
    public void startCHECK(SelectionKey key) {
        String[] fileList = FileSearcher.getFiles(pathRootFind);
        StringBuilder filesBuilder = new StringBuilder();
        for (int i = 0; i < fileList.length; i++) {
            String concatFile = fileList[i];
            if (i < fileList.length - 1) {
                concatFile += ", ";
            }
            filesBuilder.append(concatFile);
        }
        String header = "FilesLength:" + filesBuilder.length() + "\r\n\r\n";
        filesBuilder.insert(0, header);
        ByteBuffer buffer = ByteBuffer.wrap(filesBuilder.toString().getBytes());
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);
        status = RequestStatus.REQUEST_END;// 请求结束，下一次发送
    }

    public void attachAndChangeWrite(SelectionKey key, RequestStatus requestStatus) {
        String response = requestStatus.getResponse();
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);// 转换为写模式发送响应
    }

    public void closeChannelAndCancelKey(SelectionKey key) throws IOException {
        status = null;
        getChannel().close();
        key.cancel();
    }

    public int getClientIdCode() {
        return clientIdCode;
    }

    public String getMethod() {
        return method;
    }

    public String getFileName() {
        return fileName;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public int getTotalChannels() {
        return totalChannels;
    }

    public RequestStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestParser that = (RequestParser) o;

        if (clientIdCode != that.clientIdCode) return false;
        if (channelNumber != that.channelNumber) return false;
        if (totalChannels != that.totalChannels) return false;
        if (!method.equals(that.method)) return false;
        return fileName.equals(that.fileName);

    }

    @Override
    public int hashCode() {
        int result = clientIdCode;
        result = 31 * result + method.hashCode();
        result = 31 * result + channelNumber;
        result = 31 * result + totalChannels;
        result = 31 * result + fileName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RequestParser{" +
                "clientIdCode=" + clientIdCode +
                ", method='" + method + '\'' +
                ", fileName='" + fileName + '\'' +
                ", channelNumber=" + channelNumber +
                ", totalChannels=" + totalChannels +
                ", channel=" + getChannel() +
                '}';
    }
}
