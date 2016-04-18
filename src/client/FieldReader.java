package client;

/**
 * Created by zfh on 16-3-25.
 */
public class FieldReader {
    // 子文件首部
    public String fileName;
    public String encoding;
    public String packageInfo;
    public String LengthInfo;
    // 客户端请求
    public String response;
    public String filesLength;

    public FieldReader(String header) {
        this.fileName = readField(header, "FileName:");
        this.encoding = readField(header, "Encoding:");
        this.packageInfo = readField(header, "PackageInfo:");
        this.LengthInfo = readField(header, "LengthInfo:");
        this.response = readField(header, "Response:");
        this.filesLength=readField(header,"FilesLength:");
    }

    private String readField(String request, String field) {
        int fieldStart = request.indexOf(field);
        int fieldEnd = request.indexOf("\r\n", fieldStart);
        if ((fieldStart == -1) || (fieldEnd == -1) || (fieldStart + field.length() == fieldEnd)) {
            return null;
        }
        return request.substring(fieldStart + field.length(), fieldEnd);
    }
}
