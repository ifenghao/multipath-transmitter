package server;

import server.parsers.*;
import server.serverexceptions.IllegalParserStatusException;
import server.utils.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by zfh on 16-2-26.
 * 基于NIO设计服务器
 */
public class FileServer {
    private final int port;
    private final String pathRootFind;
    private final String pathRootSave;
    private final int BUFFER_SIZE = 4096;// 读取客户端消息分配的缓冲区大小
    private final static Logger logger = new LoggerGenerator("serverLogger").generate();
    private static AvailableAddressFinder serverFinder;
    private Selector selector;

    private Map<PutMapKey, List<PutParser>> putMap = Collections.synchronizedMap(new HashMap<PutMapKey, List<PutParser>>());// key=要接收的文件名，list=接收到子文件列表
    private Map<GetMapKey, List<GetParser>> getMap = Collections.synchronizedMap(new HashMap<GetMapKey, List<GetParser>>());// key=文件分割器，list=发送的通道解析器
    private List<RequestParser> requestList = Collections.synchronizedList(new ArrayList<RequestParser>());

    public FileServer(int port) {
        this.port = port;
        this.pathRootFind = "/home/zfh2/find/";
        ContentBuilder.createDir(pathRootFind);
        this.pathRootSave = "/home/zfh2/save/";
        ContentBuilder.createDir(pathRootSave);
        try {
            serverFinder = new AvailableAddressFinder();
            List<InetAddress> availableAddresses = serverFinder.getList();
            InetAddress localIp = availableAddresses.get(0);
            InetSocketAddress localSocket = new InetSocketAddress(localIp, port);
            System.out.println(localSocket);
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverChannel.socket();
            serverSocket.bind(localSocket, 50);
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            logger.severe("channel closed " + e.getMessage());
        } catch (SocketException e) {
            logger.severe("no available " + e.getMessage());
        } catch (IOException e) {
            logger.severe("open failed " + e.getMessage());
        }
    }

    public void start() throws IOException, RuntimeException {
        while (true) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel channel = server.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    RequestParser rp = new RequestParser(channel, pathRootFind, pathRootSave);
                    requestList.add(rp);
                    logger.info(" accept connection " + channel);
                } else if (key.isWritable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    RequestParser rp = ServerUtil.getMatchedRequestParser(channel, requestList);
                    if (rp.getStatus() != RequestStatus.REQUEST_END) {// 对请求返回响应
                        if (buffer.hasRemaining()) {
                            try {
                                channel.write(buffer);// 写响应
                            } catch (IOException e) {
                                logger.severe(" write channel broken " + channel);
                                channel.close();
                                key.cancel();
                            }
                        } else {
                            switch (rp.getStatus()) {
                                case RESPOND_OK:// 返回OK响应
                                    if (rp.getMethod().equals("GET")) {
                                        rp.startGET(key, getMap);
                                    } else if (rp.getMethod().equals("PUT")) {
                                        rp.startPUT(key, putMap);
                                    } else if (rp.getMethod().equals("CHECK")) {
                                        rp.startCHECK(key);
                                    }
                                    break;
                                case RESPOND_NOT_FOUND:
                                    rp.closeChannelAndCancelKey(key);
                                    requestList.remove(rp);
                                    break;
                                case RESPOND_BAD_REQUEST:
                                    rp.closeChannelAndCancelKey(key);
                                    requestList.remove(rp);
                                    break;
                                case RESPOND_SERVICE_UNAVAILABLE:
                                    rp.closeChannelAndCancelKey(key);
                                    requestList.remove(rp);
                                    break;
                                default:
                                    rp.closeChannelAndCancelKey(key);
                                    throw new IllegalParserStatusException("unknown response " + rp.getStatus());
                            }
                        }
                    } else {
                        if (buffer.hasRemaining()) {
                            try {
                                channel.write(buffer);
                            } catch (IOException e) {
                                logger.severe(" write channel broken " + channel);
                                channel.close();
                                key.cancel();
                            }
                        } else {
                            if (rp.getMethod().equals("GET")) {
                                GetParser gp = ServerUtil.getMatchedGetParser(rp, getMap);
                                if (gp.getStatus() == GetStatus.PUTTING) {
                                    gp.changeReadAndWaitDone(key);
                                } else {
                                    gp.closeChannelAndCancelKey(key);
                                    throw new IllegalParserStatusException(gp.getStatus() + " expect PUTTING");
                                }
                            } else if (rp.getMethod().equals("PUT")) {
                                PutParser pp = ServerUtil.getMatchedPutParser(rp, putMap);
                                if (pp.getStatus() == PutStatus.RESPOND_DONE) {
                                    pp.finishAndChangeRead(key);// 子文件接收完成就要启动下一次接收
                                    PutMapKey putMapKey = ServerUtil.getMatchedPutMapKey(rp, putMap);
                                    if (ServerUtil.hasNextSubFile(rp, putMap)) {// 还有下一个子文件
                                        PutParser ppNew = new PutParser(channel, putMapKey.getPathRootSaveChanged());
                                        putMap.get(putMapKey).add(ppNew);
                                    } else {// 最后一个文件不知道客户端使用哪个通道发送，所以给每个通道建立解析器都尝试去读
                                        for (SocketChannel socketChannel : putMapKey.getChannelList()) {
                                            PutParser ppNew = new PutParser(socketChannel, putMapKey.getPathRootSaveChanged());
                                            putMap.get(putMapKey).add(ppNew);
                                        }// 在这里并不关闭通道，对于没有接收的解析器可以在读取为空时删除并关闭通道
                                    }
                                } else {
                                    pp.closeChannelAndCancelKey(key);
                                    throw new IllegalParserStatusException(pp.getStatus() + " expect RESPOND_DONE");
                                }
                            } else if (rp.getMethod().equals("CHECK")) {
                                logger.info(" CHECK close " + channel);
                                rp.closeChannelAndCancelKey(key);
                                requestList.remove(rp);
                            }
                        }
                    }
                } else if (key.isReadable()) {// 服务器接受客户端访问的时候都是读取
                    SocketChannel channel = (SocketChannel) key.channel();
                    RequestParser rp = ServerUtil.getMatchedRequestParser(channel, requestList);
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    try {
                        channel.read(buffer);
                    } catch (IOException e) {
                        logger.severe(" read channel broken " + channel);
                        channel.close();
                        key.cancel();
                        continue;
                    }
                    buffer.flip();
                    int limit = buffer.limit();
                    if (limit == 0 && !rp.getMethod().equals("PUT")) {
                        rp.closeChannelAndCancelKey(key);
                        throw new RuntimeException(rp.getMethod() + " read null " + channel);// 没有读取到任何数据
                    }
                    byte[] array = new byte[limit];
                    buffer.get(array);
                    switch (rp.getStatus()) {
                        case WAIT_REQUEST:// 第一次读取请求
                            rp.parse(array);
                            switch (rp.getStatus()) {
                                case RESPOND_OK:
                                    rp.attachAndChangeWrite(key, RequestStatus.RESPOND_OK);
                                    if (rp.getMethod().equals("GET")) {
                                        rp.deployGET(getMap);
                                        logger.info(" request GET " + rp);
                                    } else if (rp.getMethod().equals("PUT")) {
                                        rp.deployPUT(putMap);
                                        logger.info(" request PUT " + rp);
                                    } else if (rp.getMethod().equals("CHECK")) {
                                        logger.info(" request CHECK " + rp);
                                    }
                                    break;
                                case RESPOND_NOT_FOUND:
                                    rp.attachAndChangeWrite(key, RequestStatus.RESPOND_NOT_FOUND);
                                    break;
                                case RESPOND_BAD_REQUEST:
                                    rp.attachAndChangeWrite(key, RequestStatus.RESPOND_BAD_REQUEST);
                                    break;
                                case RESPOND_SERVICE_UNAVAILABLE:
                                    rp.attachAndChangeWrite(key, RequestStatus.RESPOND_SERVICE_UNAVAILABLE);
                                    break;
                                default:
                                    rp.closeChannelAndCancelKey(key);
                                    throw new IllegalParserStatusException("unknown response " + rp.getStatus());
                            }
                            break;
                        case REQUEST_END:
                            if (rp.getMethod().equals("GET")) {// 等待客户端返回的完成响应
                                GetParser gp = ServerUtil.getMatchedGetParser(rp, getMap);
                                if (gp.getStatus() == GetStatus.WAIT_DONE) {
                                    gp.parse(array);
                                    if (gp.getStatus() == GetStatus.FINISHED) {// 子文件发送完成
                                        GetMapKey getMapKey = ServerUtil.getMatchedGetMapKey(rp, getMap);
                                        SubContentSlicer slicer = getMapKey.getSlicer();
                                        if (slicer.isSplitFinished()) {// 总文件分割没有剩余，关闭通道
                                            logger.info(" GET close " + channel);
                                            gp.closeChannelAndCancelKey(key);
                                            requestList.remove(rp);
                                            if (ServerUtil.isAllGetFinished(rp, getMap)) {// 子文件全部发送完毕
                                                getMap.remove(ServerUtil.getMatchedGetMapKey(rp, getMap));
                                                logger.info(" finish GET " + rp);
                                            }
                                        } else {// 继续发送下一个子文件
                                            gp.attachAndChangeWrite(key, slicer.next());
                                        }
                                    } else if (gp.isResponseError()) {
                                        gp.closeChannelAndCancelKey(key);
                                        throw new IllegalParserStatusException("should finish, but " + gp.getStatus());
                                    }
                                } else {
                                    gp.closeChannelAndCancelKey(key);
                                    throw new IllegalParserStatusException("should wait done, but " + gp.getStatus());
                                }
                            } else if (rp.getMethod().equals("PUT")) {// PUT方法接收到的数据
                                PutParser pp = ServerUtil.getMatchedPutParser(rp, putMap);
                                if (limit == 0) {// 读取为空说明本通道没有子文件接收，要关闭
                                    logger.info(" PUT close " + channel);
                                    pp.closeChannelAndCancelKey(key);
                                    requestList.remove(rp);// 关闭通道后删除本通道请求
                                    ServerUtil.cleanPutMap(rp, putMap);
                                    if (ServerUtil.isAllPutFinished(rp, putMap)) {// 全部接收完成，创建线程组装文件
                                        new Thread(new ServerFileAssembler(rp, putMap, pathRootSave)).start();
                                        logger.info(" finish PUT " + rp);
                                    } else if (!ServerUtil.hasActivePutParser(rp, putMap)) {// 没有活动的解释器则通道异常文件接收失败
                                        logger.warning(" failed PUT " + rp);
                                    }
                                } else {
                                    pp.parse(array);
                                    if (pp.getStatus() == PutStatus.GET_OVER) {
                                        pp.attachAndRespondDone(key);
                                    }
                                }
                            }
                            break;
                        default:
                            rp.closeChannelAndCancelKey(key);
                            throw new IllegalParserStatusException("request either wait or end, but " + rp.getStatus());
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new FileServer(8080).start();
    }
}
