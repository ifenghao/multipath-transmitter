package client.utils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by zfh on 16-2-27.
 * 寻找设备可用的网络地址
 */
public class AvailableAddressFinder {
    private List<InetAddress> availableAddresses = new ArrayList<InetAddress>();

    public AvailableAddressFinder() throws SocketException {
        getAvailableAddress();
        if (availableAddresses.size() == 0) {
            throw new SocketException("no available address!!");
        }
    }

    private void getAvailableAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> interfacesAddress = ni.getInetAddresses();
            while (interfacesAddress.hasMoreElements()) {
                InetAddress localIp = interfacesAddress.nextElement();
                if (localIp.isSiteLocalAddress() && isRemoteReachable(localIp)) {// 如果是网站地址就是可用地址
                    availableAddresses.add(localIp);//服务器只用一个地址,客户端使用多个地址
                }
            }
        }
    }

    public boolean isRemoteReachable(InetAddress localIp) {
        SocketAddress localSocket = new InetSocketAddress(localIp, 0);
        SocketAddress remoteSocket = new InetSocketAddress("www.baidu.com", 80);
        Socket socket = new Socket();
        try {
            socket.bind(localSocket);
            socket.connect(remoteSocket, 1000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<InetAddress> getList() {
        return availableAddresses;
    }
}
