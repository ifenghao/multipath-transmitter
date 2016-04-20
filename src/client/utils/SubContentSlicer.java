package client.utils;

import java.io.IOException;

/**
 * Created by zfh on 16-3-12.
 */
public class SubContentSlicer {
    private ContentBuilder cb;// 读取到的总文件
    private String fileName;
    private long number = 0;// 包编号
    private long totalPackages;// 包总数
    private int subFileLength;
    private long totalFileLength;
    private boolean splitFinished = false;

    public SubContentSlicer(String fileName, String filePathRoot, int totalPackages) {
        this(fileName, filePathRoot, "UTF-8", totalPackages);
    }

    public SubContentSlicer(String fileName, String filePathRoot, String encoding, int totalPackages) {
        this.cb = new ContentBuilder(fileName, filePathRoot, encoding);
        this.fileName = fileName;
        this.totalPackages = totalPackages;
    }

    public SubContentSlicer(String fileName, String filePathRoot) {
        this(fileName, filePathRoot, "UTF-8");
    }

    public SubContentSlicer(String fileName, String filePathRoot, String encoding) {
        this.cb = new ContentBuilder(fileName, filePathRoot, encoding);
        this.fileName = fileName;
        this.totalFileLength = cb.getFileLength();
    }

    public void determineTotalPackage(int channels) {
        this.totalPackages = (long) Math.ceil((double) totalFileLength / 65536);// 发送子文件长度64K不得小于对方接收缓冲区大小4K
        if (totalPackages < channels) {
            totalPackages = channels;
        }
    }

    public byte[] next() {
        splitFinished = (number == totalPackages - 1);
        ChannelFileReader reader = cb.getReader();
        number++;
        try {
            subFileLength = reader.read();
            byte[] subFileData = reader.getArray();
            byte[] header = makeHeader().getBytes();
            return ContentBuilder.concatArrays(header, subFileData);
        } catch (IOException e) {
            return null;
        }
    }

    public String makeHeader() {
        return "FileName:" + cb.getFileName() + "\r\n" +
                "Encoding:" + cb.getEncoding() + "\r\n" +
                "PackageInfo:" + number + "/" + totalPackages + "\r\n" +// 包编号/包总数
                "LengthInfo:" + subFileLength + "/" + totalFileLength +// 子文件长度/总文件长度
                "\r\n\r\n";
    }

    public boolean isSplitFinished() {
        return splitFinished;
    }

    public String getFileName() {
        return fileName;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "SubContentSlicer{" +
                "fileName='" + fileName + '\'' +
                ", number=" + number +
                ", totalPackages=" + totalPackages +
                ", splitFinished=" + splitFinished +
                '}';
    }

    public static void main(String[] args) {
        SubContentSlicer subCb = new SubContentSlicer("clientMusic1.wav", "/home/zfh/", 3);
        for (int i = 0; i < 3; i++) {
            System.out.println(subCb.next());
        }
    }
}
