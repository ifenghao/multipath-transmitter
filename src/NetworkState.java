import client.AvailableAddressFinder;

import java.io.IOException;
import java.net.*;
import java.util.List;

/**
 * Created by zhufenghao on 2016/4/7 0007.
 */
public class NetworkState {
    private AvailableAddressFinder finder=new AvailableAddressFinder();
    public void isRemoteReachable(String remote) {
        List<InetAddress> addressList=finder.getList();
        System.out.println(addressList.size());
        for (InetAddress localIp:addressList){
            SocketAddress localSocket=new InetSocketAddress(localIp,0);
            SocketAddress remoteSocket = new InetSocketAddress(remote, 80);
            Socket socket=new Socket();
            try {
                socket.bind(localSocket);
                socket.connect(remoteSocket,1000);
                System.out.println("success "+localIp);
            } catch (IOException e) {
                System.out.println("failure "+localIp);
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        NetworkState networkState=new NetworkState();
        networkState.isRemoteReachable("www.baidu.com");
    }
}
