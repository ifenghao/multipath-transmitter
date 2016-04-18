package client;

import client.parsers.CheckStatus;
import client.parsers.ClientCheckParser;
import client.utils.ClientUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by zfh on 16-4-3.
 */
public class Checker implements Callable<Void> {
    private Selector selector;
    private final int BUFFER_SIZE = 4096;
    private List<ClientCheckParser> checkList = new ArrayList<ClientCheckParser>();
    private List<SocketChannel> channelList = new ArrayList<SocketChannel>();

    public Checker(String remote, int remotePort, List<InetAddress> localIps, int localPort) {
        try {
            selector = Selector.open();
            for (int i = 0; i < localIps.size(); i++) {
                SocketAddress localNetSocket = new InetSocketAddress(localIps.get(i), localPort);
                SocketAddress remoteSocket = new InetSocketAddress(remote, remotePort);
                SocketChannel channel = SocketChannel.open();
                Socket socket = channel.socket();
                socket.setSoTimeout(5000);
                socket.bind(localNetSocket);
                socket.connect(remoteSocket);
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE);// 注册后就要发送请求
                String header = "Method:CHECK\r\n" +
                        "IdCode:" + this.hashCode() + "\r\n" +// 默认的hashCode返回的是内存地址，保证了每个Receiver对象不同
                        "FileName:" + null + "\r\n" +
                        "ChannelInfo:" + i + "/" + localIps.size() + "\r\n\r\n";
                ByteBuffer headerBuffer = ByteBuffer.wrap(header.getBytes());
                key.attach(headerBuffer);
                ClientCheckParser ccp=new ClientCheckParser(channel);
                checkList.add(ccp);
                channelList.add(channel);
                System.out.println(channel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void call() throws IOException {
        while (!ClientUtil.isAllChecked(checkList)){
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isWritable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer headerBuffer = (ByteBuffer) key.attachment();
                    if (headerBuffer.hasRemaining()) {
                        channel.write(headerBuffer);
                    } else {
                        ClientCheckParser ccp=ClientUtil.getMatchedCheckParser(channel,checkList);
                        ccp.changeReadAndWaitResponse(key);
                    }
                }else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    channel.read(buffer);
                    buffer.flip();
                    int limit = buffer.limit();
                    byte[] array = new byte[limit];
                    buffer.get(array);
                    if (limit==0){
                        throw new RuntimeException("limit==0"+channel);
                    }
                    ClientCheckParser ccp = ClientUtil.getMatchedCheckParser(channel, checkList);
                    ccp.parse(array);
                    if (ccp.getStatus()== CheckStatus.CHECK_OVER){
                        System.out.println(ccp.getFiles());
                        ccp.closeChannelAndCancelKey(key);
                    }
                }
            }
        }
        return null;
    }
}
