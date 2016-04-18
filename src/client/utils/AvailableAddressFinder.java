package client.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by zfh on 16-2-27.
 * 寻找设备可用的网络地址
 */
public class AvailableAddressFinder {
    private List<InetAddress> availableAddresses = new ArrayList<InetAddress>();

    public AvailableAddressFinder() {
        try {
            getAvailableAddress();
            if (getSize() == 0) {
                throw new SocketException("no available address!");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void getAvailableAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> interfacesAddress = ni.getInetAddresses();
                while (interfacesAddress.hasMoreElements()) {
                    InetAddress localIp = interfacesAddress.nextElement();
                    if (localIp.isSiteLocalAddress()) {// 如果是网站地址就是可用地址
                        availableAddresses.add(localIp);//服务器只用一个地址,客户端使用多个地址
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public List<InetAddress> getList() {
        return availableAddresses;
    }

    public int getSize() {
        return availableAddresses.size();
    }
}
