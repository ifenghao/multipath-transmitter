package server.utils;

import server.GetMapKey;
import server.PutMapKey;
import server.parsers.*;
import server.serverexceptions.NoMatchedException;

import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by zfh on 16-3-15.
 */
public class ServerUtil {
    public static RequestParser getMatchedRequestParser(SocketChannel channel, List<RequestParser> requestList) {
        for (RequestParser rpExist : requestList) {
            if (rpExist.getChannel().isConnected() &&
                    rpExist.getChannel().equals(channel)) {
                return rpExist;
            }
        }
        throw new NoMatchedException("rp not exist");
    }

    /*
    **********************************PUT util***********************************
     */
    private static int getTotalPackages(List<PutParser> putList) {
        for (PutParser pp : putList) {
            if (pp.getStatus() != PutStatus.WAIT_HEADER) {
                return pp.getTotalPackages();
            }
        }
        return 0;
    }

    public static boolean hasMatchedPutMapKey(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap) {
        if (putMap.isEmpty()) {
            return false;
        }
        Set<PutMapKey> keySet = putMap.keySet();
        for (PutMapKey key : keySet) {
            if (key.getFileNameOriginal().equals(rp.getFileName()) &&
                    key.getClientIdCode() == rp.getClientIdCode()) {
                return true;
            }
        }
        return false;
    }

    public static PutMapKey getMatchedPutMapKey(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap) {
        Set<PutMapKey> keySet = putMap.keySet();
        for (PutMapKey key : keySet) {
            if (key.getFileNameOriginal().equals(rp.getFileName()) &&
                    key.getClientIdCode() == rp.getClientIdCode()) {
                return key;
            }
        }
        throw new NoMatchedException("GetMapKey not exist");
    }

    public static PutParser getMatchedPutParser(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap) {
        PutMapKey putMapKey = getMatchedPutMapKey(rp, putMap);
        List<PutParser> putList = putMap.get(putMapKey);
        for (PutParser ppExist : putList) {
            if ((ppExist.getStatus() != PutStatus.FINISHED) &&
                    ppExist.getChannel().equals(rp.getChannel())) {
                return ppExist;
            }
        }
        throw new NoMatchedException("pp not exist");
    }

    public static boolean hasNextSubFile(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap) {
        PutMapKey putMapKey = getMatchedPutMapKey(rp, putMap);
        List<PutParser> putList = putMap.get(putMapKey);
        return putList.size() < getTotalPackages(putList);
    }

    public static void cleanPutMap(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap) {
        PutMapKey putMapKey = ServerUtil.getMatchedPutMapKey(rp, putMap);
        for (SocketChannel channel : putMapKey.getChannelList()) {
            if (channel.isOpen()) {
                return;
            }
        }
        List<PutParser> putList = putMap.get(putMapKey);
        Iterator<PutParser> ppIterator = putList.iterator();// 删除元素时必须使用迭代器
        while (ppIterator.hasNext()) {
            if (ppIterator.next().getStatus() != PutStatus.FINISHED) {
                ppIterator.remove();// 无用的解析器要从列表中删除
            }
        }
    }

    public static boolean isAllPutFinished(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap) {
        PutMapKey putMapKey = getMatchedPutMapKey(rp, putMap);
        List<PutParser> putList = putMap.get(putMapKey);
        if (putList.size() != getTotalPackages(putList)) {// 总包数不够则未完成
            return false;
        }
        for (PutParser ppExist : putList) {
            if (ppExist.getStatus() != PutStatus.FINISHED) {// 只要有一个子文件未完成则总文件未完成
                return false;
            }
        }
        return true;
    }

    /*
    **********************************GET util***********************************
     */
    public static boolean hasMatchedGetMapKey(RequestParser rp, Map<GetMapKey, List<GetParser>> getMap) {
        if (getMap.isEmpty()) {
            return false;
        }
        Set<GetMapKey> keySet = getMap.keySet();
        for (GetMapKey key : keySet) {
            if (key.getFileName().equals(rp.getFileName()) && key.getClientIdCode() == rp.getClientIdCode()) {
                return true;
            }
        }
        return false;
    }

    public static GetMapKey getMatchedGetMapKey(RequestParser rp, Map<GetMapKey, List<GetParser>> getMap) {
        Set<GetMapKey> keySet = getMap.keySet();
        for (GetMapKey key : keySet) {
            if (key.getFileName().equals(rp.getFileName()) && key.getClientIdCode() == rp.getClientIdCode()) {
                return key;
            }
        }
        throw new NoMatchedException("GetMapKey not exist");
    }

    public static GetParser getMatchedGetParser(RequestParser rp, Map<GetMapKey, List<GetParser>> getMap) {
        GetMapKey key = getMatchedGetMapKey(rp, getMap);
        List<GetParser> getList = getMap.get(key);
        for (GetParser gpExist : getList) {
            if ((gpExist.getStatus() != GetStatus.FINISHED) && gpExist.getChannel().equals(rp.getChannel())) {
                return gpExist;
            }
        }
        throw new NoMatchedException("gp not exist");
    }

    public static boolean isAllGetFinished(RequestParser rp, Map<GetMapKey, List<GetParser>> getMap) {
        GetMapKey getMapKey = getMatchedGetMapKey(rp, getMap);
        List<GetParser> getList = getMap.get(getMapKey);
        for (GetParser gpExist : getList) {
            if (gpExist.getStatus() != GetStatus.FINISHED) {// 只要有一个子文件未完成则总文件未完成
                return false;
            }
        }
        return true;
    }
}
