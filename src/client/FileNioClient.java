package client;

import client.utils.AvailableAddressFinder;
import client.utils.ContentBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zfh on 16-3-9.
 */
public class FileNioClient {
    private String pathRootFind;
    private String pathRootSave;
    private AvailableAddressFinder clientFinder;

    public FileNioClient() {
        this.pathRootFind = "/home/zfh/find/";
        ContentBuilder.createDir(pathRootFind);
        this.pathRootSave = "/home/zfh/save/";
        ContentBuilder.createDir(pathRootSave);
        try {
            clientFinder = new AvailableAddressFinder();
        } catch (SocketException e) {
            System.out.println("no available address");
        }
    }

    public void start() throws IOException {
        List<InetAddress> localIps = clientFinder.getList();
        String remoteAddress="10.13.88.15";
        int remotePort=8080;
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 1; i++) {
//            Receiver receiver = new Receiver(remoteAddress, remotePort, localIps, 1210 + i, "serverMovie.mkv", pathRootSave);
//            pool.submit(receiver);
//            Sender sender = new Sender(remoteAddress, remotePort, localIps, 1240 + i, "clientMovie.mkv", pathRootFind);
//            pool.submit(sender);
//            Checker checker=new Checker(remoteAddress, remotePort, localIps, 1250 + i);
//            pool.submit(checker);
        }
        pool.shutdown();
    }

    public AvailableAddressFinder getClientFinder() {
        return clientFinder;
    }

    public static void main(String[] args) throws IOException {
        new FileNioClient().start();
    }
}
