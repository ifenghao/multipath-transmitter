package server.utils;

import server.PutMapKey;
import server.parsers.PutParser;
import server.parsers.PutStatus;
import server.parsers.RequestParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by zfh on 16-3-26.
 */
public class ServerFileAssembler implements Runnable {
    private Map<PutMapKey, List<PutParser>> putMap;
    private List<PutParser> putList;
    private PutMapKey putMapKey;
    private String pathRootSave;

    public ServerFileAssembler(RequestParser rp, Map<PutMapKey, List<PutParser>> putMap, String pathRootSave) {
        this.putMap = putMap;
        this.putMapKey = ServerUtil.getMatchedPutMapKey(rp, putMap);
        this.putList = putMap.get(putMapKey);
        this.pathRootSave = pathRootSave;
    }

    @Override
    public void run() {
        putList.sort(new Comparator<PutParser>() {// 按照编号排序
            @Override
            public int compare(PutParser o1, PutParser o2) {
                long packageNumber1 = o1.getPackageNumber();
                long packageNumber2 = o2.getPackageNumber();
                if (packageNumber1 < packageNumber2) {
                    return -1;
                } else if (packageNumber1 > packageNumber2) {
                    return 1;
                }
                return 0;
            }
        });
        long lengthCounter = 0;
        File file = new File(pathRootSave + putMapKey.getFileNameChanged());
        try {
            file.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(file, true);
            for (PutParser pp : putList) {
                Path path = Paths.get(pp.getPathRootSave() + pp.getFileName());
                byte[] fileData = Files.readAllBytes(path);
                fileOut.write(fileData);// 写入总文件
                if (fileData.length == pp.getSubFileLength()) {
                    lengthCounter += fileData.length;
                }
                path.toFile().delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (PutParser pp : putList) {
            if (pp.getStatus() == PutStatus.FINISHED) {
                long totalLength = pp.getTotalFileLength();
                if (lengthCounter == totalLength) {
                    System.out.println("subFiles checked successfully!");
                } else {
                    System.out.println("received:" + lengthCounter + ",expected:" + totalLength);
                }
                new File(putMapKey.getPathRootSaveChanged()).delete();// 删除.dir文件夹
                break;
            }
        }
        putMap.remove(putMapKey);
    }
}
