package server;

import server.utils.SubContentSlicer;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zfh on 16-3-17.
 */
public class GetMapKey {
    private SubContentSlicer slicer;
    private String fileName;
    private int clientIdCode;
    private List<SocketChannel> channelList=new ArrayList<SocketChannel>();

    public GetMapKey(String fileName, String pathRootFind, int clientIdCode) {
        this.fileName = fileName;
        this.slicer = new SubContentSlicer(fileName,pathRootFind);
        this.clientIdCode = clientIdCode;
    }

    public void addChannel(SocketChannel channel){
        channelList.add(channel);
    }

    public String getFileName() {
        return fileName;
    }

    public SubContentSlicer getSlicer() {
        return slicer;
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

        GetMapKey getMapKey = (GetMapKey) o;

        if (clientIdCode != getMapKey.clientIdCode) return false;
        if (!fileName.equals(getMapKey.fileName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + clientIdCode;
        return result;
    }

    @Override
    public String toString() {
        return "GetMapKey{" +
                "fileName='" + fileName + '\'' +
                ", clientIdCode=" + clientIdCode +
                '}';
    }
}
