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
    private final int remotePort;
    private String pathRootFind;
    private String pathRootSave;
    private static AvailableAddressFinder clientFinder;

    public FileNioClient(int remotePort) {
        this.remotePort = remotePort;
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
        String remote = "10.13.88.15";
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 3; i++) {
            Receiver receiver = new Receiver(remote, remotePort, localIps, 1210 + i, "serverMusic.flac", pathRootSave);
            pool.submit(receiver);
            Sender sender = new Sender(remote, remotePort, localIps, 1220 + i, "clientMusic3.flac", pathRootFind);
            pool.submit(sender);
            Checker checker=new Checker(remote, remotePort, localIps, 1230 + i);
            pool.submit(checker);
        }
        pool.shutdown();
    }

    public static void main(String[] args) throws IOException {
        new FileNioClient(8080).start();
    }
}
