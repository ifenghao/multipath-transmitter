import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zfh on 16-4-19.
 */
public class ChannelFileWritter {


    public static void main(String[] args) throws IOException {
        File file=new File("/home/zfh/find/combine.txt");
        file.createNewFile();
        ChannelFileReader reader1=new ChannelFileReader("/home/zfh/find/clientSend.txt",1);
        ChannelFileReader reader2=new ChannelFileReader("/home/zfh/find/clientSend2.txt",1);
        FileOutputStream fileOut=new FileOutputStream("/home/zfh/find/combine.txt",true);
        int bytes;
        int position=0;
        while ((bytes=reader1.read())!=-1){
            fileOut.write(reader1.getArray());
            position+=bytes;
        }
        while ((bytes=reader2.read())!=-1){
            fileOut.write(reader2.getArray());
            position+=bytes;
        }
        fileOut.close();
    }
}