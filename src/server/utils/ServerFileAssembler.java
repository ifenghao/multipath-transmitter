package server.utils;

import server.PutMapKey;
import server.parsers.PutParser;
import server.parsers.PutStatus;
import server.parsers.RequestParser;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by zfh on 16-3-26.
 */
public class ServerFileAssembler implements Runnable{
    private Map<PutMapKey, List<PutParser>> putMap;
    private List<PutParser> putList;
    private PutMapKey putMapKey;
    private String pathRootSave;
    private String subPathRoot;

    public ServerFileAssembler(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap, String pathRootSave) {
        this.putMap=putMap;
        this.putMapKey = ServerUtil.getMatchedPutMapKey(rp, putMap);
        this.putList = putMap.get(putMapKey);
        this.pathRootSave=pathRootSave;
        this.subPathRoot=putMapKey.getPathRootSaveChanged();
    }

    @Override
    public void run() {
        putList.sort(new Comparator<PutParser>() {// 按照编号排序
            @Override
            public int compare(PutParser o1, PutParser o2) {
                return o1.getPackageNumber() - o2.getPackageNumber();
            }
        });
        int lengthCounter = 0;
        byte[] fileData = new byte[0];
        for (PutParser ppExist : putList) {
            ContentBuilder cb = new ContentBuilder(ppExist.getFileName(), ppExist.getPathRootSave());
            if (cb.getFileLength() == ppExist.getSubFileLength()) {
                lengthCounter += ppExist.getSubFileLength();
            }
            fileData = ContentBuilder.concatArrays(fileData, cb.getFileData());
            cb.delete();
        }
        for (PutParser pp : putList) {
            if (pp.getStatus() == PutStatus.FINISHED) {
                int totalLength = pp.getTotalFileLength();
                if (lengthCounter == totalLength) {
                    System.out.println("subFiles checked successfully!");
                } else {
                    System.out.println("received:" + lengthCounter + ",expected:" + totalLength);
                }
                break;
            }
        }
        new File(subPathRoot).delete();// 删除.dir文件夹
        new ContentBuilder(putMapKey.getFileNameChanged(), pathRootSave, fileData).save();// 保存总文件
        putMap.remove(putMapKey);
    }
}
