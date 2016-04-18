package server;

import server.utils.ContentBuilder;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zfh on 16-3-17.
 */
public class PutMapKey {
    private String fileNameOriginal;// 面向RequestParser
    private String fileNameChanged;// 面向接收到的子文件
    private int clientIdCode;
    private String pathRootSaveChanged;
    private List<SocketChannel> channelList = new ArrayList<SocketChannel>();

    public PutMapKey(String fileName, String pathRootSave, int clientIdCode) {
        if (!pathRootSave.endsWith("/")) {
            pathRootSave += "/";
        }
        this.fileNameOriginal = fileName;
        this.fileNameChanged = ContentBuilder.createDir(pathRootSave, fileName);
        this.pathRootSaveChanged = pathRootSave + fileNameChanged + ".dir/";
        this.clientIdCode = clientIdCode;
    }

    public void addChannel(SocketChannel channel) {
        channelList.add(channel);
    }

    public String getFileNameOriginal() {
        return fileNameOriginal;
    }

    public String getFileNameChanged() {
        return fileNameChanged;
    }

    public String getPathRootSaveChanged() {
        return pathRootSaveChanged;
    }

    public int getClientIdCode() {
        return clientIdCode;
    }

    public List<SocketChannel> getChannelList() {
        return channelList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PutMapKey putMapKey = (PutMapKey) o;

        if (clientIdCode != putMapKey.clientIdCode) return false;
        return fileNameChanged.equals(putMapKey.fileNameChanged);

    }

    @Override
    public int hashCode() {
        int result = fileNameChanged.hashCode();
        result = 31 * result + clientIdCode;
        return result;
    }

    @Override
    public String toString() {
        return "PutMapKey{" +
                "fileNameChanged='" + fileNameChanged + '\'' +
                ", clientIdCode=" + clientIdCode +
                '}';
    }
}
