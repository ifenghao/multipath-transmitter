package client;

import client.parsers.ClientSendParser;
import client.utils.ClientUtil;
import client.utils.SubContentSlicer;
import gui.ChannelStatus;
import gui.MainFrame;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zfh on 16-3-10.
 */
public class Sender implements Callable<Void> {
    private Selector selector;
    private final int BUFFER_SIZE = 1024;
    private SubContentSlicer slicer;// 发送一个文件只建立一个分割器
    private boolean sendFailed = false;
    private List<ClientSendParser> sendList = Collections.synchronizedList(new ArrayList<ClientSendParser>());// 为每个通道建立解析器
    private MainFrame mainFrame;

    public Sender(String remote, int remotePort, List<InetAddress> localIps, int localPort, String fileName, String pathRootFind,
                  MainFrame mainFrame) {
        this.slicer = new SubContentSlicer(fileName, pathRootFind);
        slicer.determineTotalPackage(localIps.size());// 确定分包总数
        this.mainFrame=mainFrame;
        try {// 建立所有本地地址连接到服务器的SocketChannel
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
                ClientSendParser csp = new ClientSendParser(channel);
                String header = "Method:PUT\r\n" +
                        "IdCode:" + this.hashCode() + "\r\n" +// 默认的hashCode返回的是内存地址，保证了每个Sender对象不同
                        "FileName:" + fileName + "\r\n" +
                        "ChannelInfo:" + i + "/" + localIps.size() + "\r\n\r\n";
                csp.attachRequestAndChangeWrite(key, header);
                sendList.add(csp);
//                System.out.println(channel);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void call() throws IOException {
        long alreadyLength=0;
        long totalLength=1;
        while (!ClientUtil.isAllSent(slicer, sendList) && !sendFailed) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isWritable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();// 只需把附属内容全部发送出去并等待返回的响应
                    if (buffer.hasRemaining()) {
                        try {
                            int bytes=channel.write(buffer);
                            String localIp=channel.getLocalAddress().toString();
                            mainFrame.getChannelMap().get(localIp.substring(0,localIp.lastIndexOf(":"))).addTX(bytes);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "发送中断", "警告", JOptionPane.WARNING_MESSAGE);
                            channel.close();
                            key.cancel();
                            sendFailed = true;
                        }
                    } else {
                        ClientSendParser csp = ClientUtil.getMatchedSendParser(channel, sendList);
                        switch (csp.getStatus()) {
                            case SEND_REQUEST:
                                csp.changeReadAndWaitResponse(key);
                                break;
                            case SENDING:
                                csp.changeReadAndWaitDone(key);
                                break;
                            default:
                                break;
                        }
                    }
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    try {
                        int bytes=channel.read(buffer);
                        String localIp=channel.getLocalAddress().toString();
                        mainFrame.getChannelMap().get(localIp.substring(0, localIp.lastIndexOf(":"))).addRX(bytes);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "接收中断", "警告", JOptionPane.WARNING_MESSAGE);
                        channel.close();
                        key.cancel();
                        sendFailed = true;
                        continue;
                    }
                    buffer.flip();
                    int limit = buffer.limit();
                    if (limit == 0) {
                        sendFailed = true;// 没有读取到任何数据
                    }
                    byte[] array = new byte[limit];
                    buffer.get(array);
                    ClientSendParser csp = ClientUtil.getMatchedSendParser(channel, sendList);
                    switch (csp.getStatus()) {
                        case WAIT_RESPONSE:
                            csp.parse(array);
                            switch (csp.getStatus()) {
                                case ACCEPT_OK:
                                    csp.attachContentAndChangeWrite(key, slicer.next());
                                    break;
                                case ACCEPT_BAD_REQUEST:
                                    break;
                                case ACCEPT_SERVICE_UNAVAILABLE:
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case WAIT_DONE:
                            csp.parse(array);
                            switch (csp.getStatus()) {
                                case ACCEPT_DONE:
                                    alreadyLength+=slicer.getSubFileLength();
                                    totalLength=slicer.getTotalFileLength();
                                    if (slicer.isSplitFinished()) {// 文件分割完成，此通道没有要发送的文件可以关闭
                                        alreadyLength=totalLength;
                                        csp.setFinished();
//                                        System.out.println(" close " + csp);
                                        csp.closeChannelAndCancelKey(key);
                                    } else {// 文件还有剩余，重新分配子文件给此通道
                                        csp.attachContentAndChangeWrite(key, slicer.next());
                                    }
                                    break;
                                default:
                                    break;
                            }
                        default:
                            break;
                    }
                }
            }
            mainFrame.getTaskPane().getTaskMap().get(this).setProgress(alreadyLength,totalLength);
        }
        return null;
    }
}
