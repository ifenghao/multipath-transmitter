import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zfh on 16-4-18.
 */
public class FileReader {
    private FileInputStream fileIn;
    private long fileLength;
    private int bufferSize;
    private byte[] array;

    public FileReader(String fileName, int bufferSize) throws IOException {
        this.fileIn = new FileInputStream(fileName);
        this.fileLength=fileIn.getChannel().size();
        this.bufferSize = bufferSize;
    }

    public int readFileChannel() throws IOException {
        FileChannel fileChannel=fileIn.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        int bytes = fileChannel.read(byteBuffer);
        if (bytes == -1) {
            fileChannel.close();
            fileIn.close();
        }else {
            array = new byte[bytes];
            byteBuffer.flip();
            byteBuffer.get(array, 0, bytes);
        }
        return bytes;
    }

    public byte[] getArray() {
        return array;
    }

    public long getFileLength() {
        return fileLength;
    }

    public static void main(String[] args) throws IOException {
        FileReader reader = new FileReader("/home/zfh/find/clientMusic1.wav", 65536);
        long start=System.nanoTime();
        while (reader.readFileChannel()!=-1);
        long end=System.nanoTime();
        System.out.println(end-start);
    }
}
