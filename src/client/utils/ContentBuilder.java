package client.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zfh on 16-2-26.
 */
public class ContentBuilder {
    private String filePathRoot;
    private String encoding;
    private String fileName;
    private long fileLength;
    private ChannelFileReader reader;

    public ContentBuilder(String fileName, String filePathRoot) {// 需要读取文件
        this(fileName, filePathRoot, "UTF-8");
    }

    public ContentBuilder(String fileName, String filePathRoot, String encoding) {// 需要读取文件
        this.filePathRoot = filePathRoot;
        this.encoding = encoding;
        this.fileName = fileName;
        try {
            this.reader=new ChannelFileReader(filePathRoot + fileName,65536);
            this.fileLength = reader.getFileLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePathRoot() {
        return filePathRoot;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public ChannelFileReader getReader() {
        return reader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentBuilder that = (ContentBuilder) o;

        if (fileLength != that.fileLength) return false;
        if (!encoding.equals(that.encoding)) return false;
        if (!fileName.equals(that.fileName)) return false;
        if (!filePathRoot.equals(that.filePathRoot)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filePathRoot.hashCode();
        result = 31 * result + encoding.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + (int) (fileLength ^ (fileLength >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ContentBuilder{" +
                "encoding='" + encoding + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                '}';
    }

    public static byte[] concatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static boolean createDir(String dirName) {
        File dir = new File(dirName);
        return dir.mkdirs();
    }

    public static String createDir(String pathRoot, String fileName) {
        Pattern suffixPattern = Pattern.compile("\\.\\w+");
        Matcher matcher = suffixPattern.matcher(fileName);
        String nameBody;
        String suffix;
        if (matcher.find()) {
            nameBody = fileName.substring(0, matcher.start());
            suffix = fileName.substring(matcher.start());
        } else {
            nameBody = fileName;
            suffix = "";
        }
        int folderNumber = 0;
        String wholePath = pathRoot + fileName + ".dir" + File.separator;
        String wholeFile = pathRoot + fileName;
        File path = new File(wholePath);
        File file = new File(wholeFile);
        while (path.exists() || file.exists()) {
            folderNumber++;
            wholePath = pathRoot + nameBody + "-copy" + folderNumber + suffix + ".dir" + File.separator;
            wholeFile = pathRoot + nameBody + "-copy" + folderNumber + suffix;
            path = new File(wholePath);
            file = new File(wholeFile);
        }
        path.mkdirs();
        if (folderNumber == 0) {
            return fileName;
        }
        return nameBody + "-copy" + folderNumber + suffix;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            System.out.println(createDir("/home/zfh/test/", "client.abc.def"));
        }
    }
}
