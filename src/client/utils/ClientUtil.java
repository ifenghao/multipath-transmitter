package client.utils;

import client.clientexceptions.NoMatchedException;
import client.parsers.*;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zfh on 16-3-17.
 */
public class ClientUtil {
    /*
    **********************************Receive util***********************************
     */
    private static long getTotalPackages(List<ClientReceiveParser> receiveList) {
        for (ClientReceiveParser crp : receiveList) {
            if (crp.getStatus() == ReceiveStatus.RECEIVING ||
                    crp.getStatus() == ReceiveStatus.RECEIVE_OVER ||
                    crp.getStatus() == ReceiveStatus.RESPOND_DONE ||
                    crp.getStatus() == ReceiveStatus.FINISHED) {
                return crp.getTotalPackages();
            }
        }
        return 0;
    }

    public static boolean isAllReceived(List<ClientReceiveParser> receiveList) {
        if (receiveList.size() < getTotalPackages(receiveList)) {
            return false;
        }
        for (ClientReceiveParser crp : receiveList) {
            if (crp.getStatus() != ReceiveStatus.FINISHED) {
                return false;
            }
        }
        return true;
    }

    public static ClientReceiveParser getMatchedReceiveParser(SocketChannel channel, List<ClientReceiveParser> receiveList) {
        for (ClientReceiveParser crp : receiveList) {
            if ((crp.getStatus() != ReceiveStatus.FINISHED) && crp.getChannel().equals(channel)) {
                return crp;
            }
        }
        throw new NoMatchedException("cp not exist");
    }

    public static boolean hasNextSubFile(List<ClientReceiveParser> receiveList) {
        return receiveList.size() < getTotalPackages(receiveList);
    }

    public static void cleanList(List<SocketChannel> channelList, List<ClientReceiveParser> receiveList) {
        for (SocketChannel channel : channelList) {
            if (channel.isOpen()) {
                return;
            }
        }
        Iterator<ClientReceiveParser> crpIterator = receiveList.iterator();// 删除元素时必须使用迭代器
        while (crpIterator.hasNext()) {
            if (crpIterator.next().getStatus() != ReceiveStatus.FINISHED) {
                crpIterator.remove();// 无用的解析器要从列表中删除
            }
        }
    }

    /*
    **********************************Send util***********************************
     */
    public static boolean isAllSent(SubContentSlicer slicer, List<ClientSendParser> parserList) {
        if (!slicer.isSplitFinished()) {
            return false;
        }
        for (ClientSendParser cp : parserList) {
            if (cp.getStatus() != SendStatus.FINISHED) {
                return false;
            }
        }
        return true;
    }

    public static ClientSendParser getMatchedSendParser(SocketChannel channel, List<ClientSendParser> sendList) {
        for (ClientSendParser cp : sendList) {
            if (cp.getChannel().equals(channel)) {
                return cp;
            }
        }
        throw new NoMatchedException("cp not exist");
    }

    /*
    **********************************Check util***********************************
     */
    public static boolean isAllChecked(List<ClientCheckParser> checkList) {
        for (ClientCheckParser ccp : checkList) {
            if (ccp.getStatus() != CheckStatus.FINISHED) {
                return false;
            }
        }
        return true;
    }

    public static ClientCheckParser getMatchedCheckParser(SocketChannel channel, List<ClientCheckParser> checkList) {
        for (ClientCheckParser ccp : checkList) {
            if ((ccp.getStatus() != CheckStatus.FINISHED) && ccp.getChannel().equals(channel)) {
                return ccp;
            }
        }
        throw new NoMatchedException("cp not exist");
    }
}
