package client.utils;

import client.parsers.ClientReceiveParser;
import client.parsers.ReceiveStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zfh on 16-3-26.
 */
public class ClientFileAssembler implements Runnable {
    private List<ClientReceiveParser> receiveList;
    private String fileName;
    private String pathRootSaveOriginal;

    public ClientFileAssembler(List<ClientReceiveParser> receiveList, String fileName, String pathRootSaveOriginal) {
        this.receiveList = receiveList;
        this.fileName = fileName;
        this.pathRootSaveOriginal = pathRootSaveOriginal;
    }

    @Override
    public void run() {
        receiveList.sort(new Comparator<ClientReceiveParser>() {// 按照编号排序
            @Override
            public int compare(ClientReceiveParser o1, ClientReceiveParser o2) {
                long packageNumber1 = o1.getPackageNumber();
                long packageNumber2 = o2.getPackageNumber();
                if (packageNumber1 < packageNumber2) {
                    return -1;
                } else if (packageNumber1 > packageNumber2) {
                    return 1;
                }
                return 0;
            }
        });// 包编号排序
        long lengthCounter = 0;
        File file = new File(pathRootSaveOriginal + fileName);
        try {
            file.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(file, true);
            for (ClientReceiveParser crp : receiveList) {
                Path path = Paths.get(crp.getPathRootSave() + crp.getFileName());
                byte[] fileData = Files.readAllBytes(path);
                fileOut.write(fileData);// 写入总文件
                if (fileData.length == crp.getSubFileLength()) {
                    lengthCounter += fileData.length;
                }
                path.toFile().delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ClientReceiveParser crp : receiveList) {
            if (crp.getStatus() == ReceiveStatus.FINISHED) {
                long totalLength = crp.getTotalFileLength();
                if (lengthCounter == totalLength) {
                    System.out.println("subFiles checked successfully!");
                } else {
                    System.out.println("received:" + lengthCounter + ",expected:" + totalLength);
                }
                new File(crp.getPathRootSave()).delete();// 删除.dir文件夹
                break;
            }
        }
    }
}
