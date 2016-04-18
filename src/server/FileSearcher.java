package server;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by zfh on 16-3-24.
 */
public class FileSearcher {
    public static boolean isExist(String pathRootFind, final String fileNameFind){
        if (!pathRootFind.endsWith(File.separator)) {
            pathRootFind += File.separator;
        }
        int fileNameStart=fileNameFind.lastIndexOf(File.separator);
        final String fileName=fileNameFind.substring(fileNameStart+1);
        File path=new File(pathRootFind);
        String[] fileList=path.list(new FilenameFilter() {
            private String fileNameExpected=fileName;
            @Override
            public boolean accept(File dir, String name) {
                return fileNameExpected.equals(name);
            }
        });
        return fileList.length>0;
    }

    public static String[] getFiles(String pathRootFind){
        if (!pathRootFind.endsWith(File.separator)) {
            pathRootFind += File.separator;
        }
        final String pathRoot=pathRootFind;
        File path=new File(pathRoot);
        return path.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file=new File(pathRoot+name);
                return file.isFile();
            }
        });
    }

    public static void main(String[] args) {
        System.out.println(isExist("/home/zfh/find/", "/zfh/clientSend.txt"));
        for (String s:getFiles("/home/zfh/find/")){
            System.out.println(s);
        }
    }
}
