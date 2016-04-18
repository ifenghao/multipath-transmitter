package server;

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
    public String clientIdCode;
    public String method;
    public String channelInfo;
    public String response;

    public FieldReader(String request) {
        this.fileName = readField(request, "FileName:");
        this.encoding = readField(request, "Encoding:");
        this.packageInfo = readField(request, "PackageInfo:");
        this.LengthInfo = readField(request, "LengthInfo:");
        this.clientIdCode = readField(request, "IdCode:");
        this.method = readField(request, "Method:");
        this.channelInfo = readField(request, "ChannelInfo:");
        this.response=readField(request,"Response:");
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
