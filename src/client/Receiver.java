package client;

import client.parsers.ClientReceiveParser;
import client.utils.ClientFileAssembler;
import client.utils.ClientUtil;
import client.utils.ContentBuilder;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by zfh on 16-3-9.
 */
public class Receiver implements Callable<Void> {
    private String pathRootSaveOriginal;
    private String pathRootSaveChanged;
    private String fileNameChanged;
    private Selector selector;
    private final int BUFFER_SIZE = 4096;
    private List<ClientReceiveParser> receiveList = new ArrayList<ClientReceiveParser>();
    private List<SocketChannel> channelList = new ArrayList<SocketChannel>();

    public Receiver(String remote, int remotePort, List<InetAddress> localIps, int localPort, String fileName, String pathRootSave) {
        if (!pathRootSave.endsWith(File.separator)) {
            pathRootSave += File.separator;
        }
        this.pathRootSaveOriginal = pathRootSave;
        this.fileNameChanged = ContentBuilder.createDir(pathRootSave, fileName);// 创建子文件夹存放接收到的子文件
        this.pathRootSaveChanged = pathRootSave + fileNameChanged + ".dir" + File.separator;
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
                String header = "Method:GET\r\n" +
                        "IdCode:" + this.hashCode() + "\r\n" +// 默认的hashCode返回的是内存地址，保证了每个Receiver对象不同
                        "FileName:" + fileName + "\r\n" +
                        "ChannelInfo:" + i + "/" + localIps.size() + "\r\n\r\n";
                ByteBuffer headerBuffer = ByteBuffer.wrap(header.getBytes());
                key.attach(headerBuffer);
                ClientReceiveParser crp = new ClientReceiveParser(channel, pathRootSaveChanged, true);
                receiveList.add(crp);
                channelList.add(channel);
                System.out.println(channel);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void call() throws IOException {
        while (!ClientUtil.isAllReceived(receiveList)) {
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
                        ClientReceiveParser crp = ClientUtil.getMatchedReceiveParser(channel, receiveList);
                        switch (crp.getStatus()){
                            case SEND_REQUEST:// 首先发送请求
                                crp.changeReadAndWaitResponse(key);
                                break;
                            case RESPOND_DONE:// 接收完成返回响应
                                crp.finishAndChangeRead(key);
                                if (ClientUtil.hasNextSubFile(receiveList)) {// 总包数不够还要继续接收
                                    ClientReceiveParser cpNew = new ClientReceiveParser(channel, pathRootSaveChanged, false);
                                    receiveList.add(cpNew);
                                } else {// 最后一个文件不知道服务器已经发送到哪个通道，所以给每个通道建立解析器都尝试去读
                                    for (SocketChannel socketChannel : channelList) {
                                        ClientReceiveParser cpNew = new ClientReceiveParser(socketChannel, pathRootSaveChanged, false);
                                        receiveList.add(cpNew);
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ClientReceiveParser crp = ClientUtil.getMatchedReceiveParser(channel, receiveList);
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    channel.read(buffer);
                    buffer.flip();
                    int limit = buffer.limit();
                    byte[] array = new byte[limit];
                    buffer.get(array);
                    if (limit == 0) {// 读取为空说明本通道没有子文件接收，要关闭
                        System.out.println(" close " + crp);
                        crp.closeChannelAndCancelKey(key);
                        ClientUtil.cleanList(channelList, receiveList);
                    } else {
                        crp.parse(array);// 处理一次接收的数据
                        switch (crp.getStatus()){
                            case RECEIVE_OVER:// 接收一个子文件完成
                                crp.changeWriteAndRespondDone(key);
                                break;
                            case ACCEPT_NOT_FOUND:
                                System.out.println(" not found " + crp);
                                crp.closeChannelAndCancelKey(key);
                                receiveList.remove(crp);// 删除失败请求
                                break;
                            case ACCEPT_BAD_REQUEST:
                                System.out.println(" bad request " + crp);
                                crp.closeChannelAndCancelKey(key);
                                receiveList.remove(crp);// 删除失败请求
                                break;
                            case ACCEPT_SERVICE_UNAVAILABLE:
                                System.out.println(" service unavailable " + crp);
                                crp.closeChannelAndCancelKey(key);
                                receiveList.remove(crp);// 删除失败请求
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        if (receiveList.isEmpty()) {// 没有接收文件，说明请求失败
            System.out.println("request failed");
            new File(pathRootSaveChanged).delete();
        } else {// 文件全部接收成功开始启动新线程组装文件
            System.out.println("all received");
            new Thread(new ClientFileAssembler(receiveList, fileNameChanged, pathRootSaveOriginal)).start();
        }
        return null;
    }
}
