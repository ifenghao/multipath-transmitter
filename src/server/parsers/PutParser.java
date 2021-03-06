package server.parsers;

import server.utils.FieldReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by zfh on 16-3-14.
 */
public class PutParser extends Parser {
    private String fileName;// 子文件名称 originFileName.tmpX
    private String encoding;
    private long packageNumber;
    private long totalPackages;
    private int subFileLength;
    private long totalFileLength;
    private String pathRootSave;// 子文件路径 originPath/originFileName.dir/
    private String totalHeader = "";
    private int alreadyReadLength = 0;
    private PutStatus status;

    public PutParser(SocketChannel channel, String pathRootSave) {
        super(channel);
        if (!pathRootSave.endsWith(File.separator)) {
            pathRootSave += File.separator;
        }
        this.pathRootSave = pathRootSave;
        this.status = PutStatus.WAIT_HEADER;
    }

    @Override
    public void parse(byte[] array) {// 将读取到的数据写入文件
        if (status == PutStatus.WAIT_HEADER) {
            String header = new String(array);
            int headerEnd = header.indexOf("\r\n\r\n");
            if (headerEnd == -1) {// 子文件首部没有一次发送完成
                totalHeader += header;
                return;
            }
            totalHeader += header.substring(0, headerEnd + 4);
            FieldReader fieldReader = new FieldReader(totalHeader);
            fileName = fieldReader.fileName;
            encoding = fieldReader.encoding;
            String packageInfo = fieldReader.packageInfo;
            String lengthInfo = fieldReader.LengthInfo;
            if (fileName == null || encoding == null || packageInfo == null || lengthInfo == null) {
                throw new NullPointerException("header incorrect");
            }
            int solidus1 = packageInfo.indexOf("/");
            this.packageNumber = Long.parseLong(packageInfo.substring(0, solidus1));
            this.totalPackages = Long.parseLong(packageInfo.substring(solidus1 + 1));
            int solidus2 = lengthInfo.indexOf("/");
            this.subFileLength = Integer.parseInt(lengthInfo.substring(0, solidus2));
            this.totalFileLength = Long.parseLong(lengthInfo.substring(solidus2 + 1));
            fileName += ".tmp" + packageNumber;// 子文件附属编号
            int restLength = array.length - (headerEnd + 4);
            byte[] restFileData = new byte[restLength];
            System.arraycopy(array, headerEnd + 4, restFileData, 0, restLength);
            array = restFileData;
            status = PutStatus.GETTING;
        }
        if (status == PutStatus.GETTING) {
            int arrayLength = array.length;
            byte[] fileData = new byte[arrayLength];
            System.arraycopy(array, 0, fileData, 0, arrayLength);
            try {// alreadyReadLength==0第一次写文件使用覆盖模式，alreadyReadLength>0继续写文件使用追加模式
                FileOutputStream fileOut = new FileOutputStream(pathRootSave + fileName, alreadyReadLength > 0);
                fileOut.write(fileData);
                fileOut.flush();
                fileOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            alreadyReadLength += arrayLength;
            if (alreadyReadLength > subFileLength) {
                throw new RuntimeException("alreadyReadLength > subFileLength");
            }
            if (alreadyReadLength == subFileLength) {
                status = PutStatus.GET_OVER;
            }
        }
    }

    public void attachAndRespondDone(SelectionKey key) {
        String response = "Response:Done\r\n";
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);// 转换为写模式发送响应
        status = PutStatus.RESPOND_DONE;
    }

    public void changeReadAndGet(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        status = PutStatus.WAIT_HEADER;
    }

    public void finishAndChangeRead(SelectionKey key) {
        key.interestOps(SelectionKey.OP_READ);
        status = PutStatus.FINISHED;
    }

    public void closeChannelAndCancelKey(SelectionKey key) throws IOException {
        status = null;
        getChannel().close();
        key.cancel();
    }

    public String getFileName() {
        return fileName;
    }

    public String getEncoding() {
        return encoding;
    }

    public long getPackageNumber() {
        return packageNumber;
    }

    public long getTotalPackages() {
        return totalPackages;
    }

    public int getSubFileLength() {
        return subFileLength;
    }

    public long getTotalFileLength() {
        return totalFileLength;
    }

    public String getPathRootSave() {
        return pathRootSave;
    }

    public int getAlreadyReadLength() {
        return alreadyReadLength;
    }

    public PutStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PutParser putParser = (PutParser) o;

        if (packageNumber != putParser.packageNumber) return false;
        if (subFileLength != putParser.subFileLength) return false;
        if (totalFileLength != putParser.totalFileLength) return false;
        if (totalPackages != putParser.totalPackages) return false;
        if (!encoding.equals(putParser.encoding)) return false;
        if (!fileName.equals(putParser.fileName)) return false;
        if (!pathRootSave.equals(putParser.pathRootSave)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + encoding.hashCode();
        result = 31 * result + (int) (packageNumber ^ (packageNumber >>> 32));
        result = 31 * result + (int) (totalPackages ^ (totalPackages >>> 32));
        result = 31 * result + subFileLength;
        result = 31 * result + (int) (totalFileLength ^ (totalFileLength >>> 32));
        result = 31 * result + pathRootSave.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "server.parsers.PutParser{" +
                "fileName='" + fileName + '\'' +
                ", encoding='" + encoding + '\'' +
                ", packageNumber=" + packageNumber +
                "/" + totalPackages +
                ", subFileLength=" + subFileLength +
                "/" + totalFileLength +
                ", status=" + status +
                ", channel=" + getChannel() +
                '}';
    }
}
