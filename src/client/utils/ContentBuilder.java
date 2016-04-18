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
    private int fileLength;
    private byte[] fileData;
    private String header;// 要包含分包信息，依靠slicer的设定

    public ContentBuilder(String fileName, String filePathRoot) {// 需要读取文件
        this(fileName, filePathRoot, "UTF-8");
    }

    public ContentBuilder(String fileName, String filePathRoot, String encoding) {// 需要读取文件
        this.filePathRoot = filePathRoot;
        this.encoding = encoding;
        this.fileName = fileName;
        Path path = Paths.get(filePathRoot + fileName);
        try {
            byte[] data = Files.readAllBytes(path);
            this.fileData = data;
            this.fileLength = data.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ContentBuilder(String fileName, String pathRootSave, byte[] fileData) {
        this(fileName, pathRootSave, fileData, "UTF-8");
    }

    public ContentBuilder(String fileName, String pathRootSave, byte[] fileData, String encoding) {// 使用文件数据构建对象
        this.filePathRoot = pathRootSave;
        this.encoding = encoding;
        this.fileName = fileName;
        this.fileLength = fileData.length;
        this.fileData = fileData;
    }

    public void save() {// 将内存中的cb保存到文件系统中
        try {
            FileOutputStream fileOut = new FileOutputStream(filePathRoot + fileName);
            fileOut.write(fileData);
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete() {// 将读取到cb中的文件删除
        File file = new File(filePathRoot + fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            return true;
        }
        return false;
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

    public int getFileLength() {
        return fileLength;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }


    public byte[] getContent() {
        return concatArrays(getHeader().getBytes(), getFileData());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentBuilder that = (ContentBuilder) o;

        if (!filePathRoot.equals(that.filePathRoot)) return false;
        if (!header.equals(that.header)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filePathRoot.hashCode();
        result = 31 * result + header.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ContentBuilder{" +
                "header='" + header + '\'' +
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
