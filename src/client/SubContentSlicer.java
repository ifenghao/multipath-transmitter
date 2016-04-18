package client;

/**
 * Created by zfh on 16-3-12.
 */
public class SubContentSlicer {
    private ContentBuilder cb;// 读取到的总文件
    private String fileName;
    private int number=0;// 包编号
    private int totalPackages;// 包总数
    private int subFileLength;
    private int previousLength=0;
    private int totalFileLength;
    private boolean splitFinished=false;

    public SubContentSlicer(String fileName, String filePathRoot, int totalPackages) {
        this(fileName, filePathRoot, "UTF-8", totalPackages);
    }

    public SubContentSlicer(String fileName, String filePathRoot, String encoding, int totalPackages) {
        this.cb = new ContentBuilder(fileName, filePathRoot, encoding);
        this.fileName=fileName;
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
        this.totalPackages = totalFileLength / 65536;// 发送子文件长度64K不得小于对方接收缓冲区大小4K
        if (totalPackages < channels) {
            totalPackages = channels;
        }
    }

    public ContentBuilder next() {
        splitFinished=(number == totalPackages - 1);
        if (splitFinished) {
            subFileLength = cb.getFileLength() - previousLength;// 最后一组发送剩余所有的数据
        } else {
            subFileLength = cb.getFileLength() / totalPackages;// 子文件长度可以逐次改变
        }
        byte[] subFileData = new byte[subFileLength];
        System.arraycopy(cb.getFileData(), previousLength, subFileData, 0, subFileLength);
        previousLength += subFileLength;
        ContentBuilder subCb=new ContentBuilder(cb.getFileName(), cb.getFilePathRoot(), subFileData);
        subCb.setHeader(makeHeader());
        number++;
        return subCb;
    }

    public String makeHeader() {
        return "FileName:"+cb.getFileName() + "\r\n" +
                "Encoding:"+cb.getEncoding() + "\r\n" +
                "PackageInfo:"+number + "/" + totalPackages + "\r\n" +// 包编号/包总数
                "LengthInfo:"+subFileLength + "/" + cb.getFileLength() +// 子文件长度/总文件长度
                "\r\n\r\n";
    }

    public boolean isSplitFinished() {
        return splitFinished;
    }

    public String getFileName() {
        return fileName;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "SubContentSlicer{" +
                "fileName='" + fileName + '\'' +
                ", number=" + number +
                ", totalPackages=" + totalPackages +
                ", splitFinished=" + splitFinished +
                ", previousLength=" + previousLength +
                '}';
    }

    public static void main(String[] args) {
        SubContentSlicer subCb=new SubContentSlicer("clientMusic1.wav","/home/zfh/",3);
        for (int i = 0; i < 3; i++) {
            System.out.println(subCb.next());
        }
    }
}
