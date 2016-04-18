package client.utils;

import client.parsers.ClientReceiveParser;
import client.parsers.ReceiveStatus;

import java.io.File;
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
                return o1.getPackageNumber() - o2.getPackageNumber();
            }
        });// 包编号排序
        int lengthCounter = 0;
        byte[] fileData = new byte[0];
        for (ClientReceiveParser crp : receiveList) {
            ContentBuilder cb = new ContentBuilder(crp.getFileName(), crp.getPathRootSave());
            if (cb.getFileLength() == crp.getSubFileLength()) {
                lengthCounter += crp.getSubFileLength();
            }
            fileData = ContentBuilder.concatArrays(fileData, cb.getFileData());
            cb.delete();
        }
        for (ClientReceiveParser crp : receiveList) {
            if (crp.getStatus() == ReceiveStatus.FINISHED) {
                int totalLength = crp.getTotalFileLength();
                if (lengthCounter == totalLength) {
                    System.out.println("subFiles checked successfully!");
                } else {
                    System.out.println("received:" + lengthCounter + ",expected:" + totalLength);
                }
                String subPathRoot = crp.getPathRootSave();
                new File(subPathRoot).delete();// 删除.dir文件夹
                break;
            }
        }
        new ContentBuilder(fileName, pathRootSaveOriginal, fileData).save();// 保存总文件
    }
}
