package Utils;

import Implementation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuncUtils {
    /*
    把.meta文件读进对象
     */
    public static SmartFileMeta fileMeta2Object(SmartFile file, SmartFileManager fileManager) {
        return null;
    }

    /*
    把对象读进.meta文件
     */
    public static void object2fileMeta(SmartFileMeta fileMeta, SmartId fId) {
        return;
    }

//    输入File cursor，输出后面所有数据
//    public static byte[] cursor2end(SmartFile smartFile, int cursor, int firstBlockIndex) throws IOException, ClassNotFoundException {
//        FileInputStream fileInputStream;
//        SmartBlock firstBlockToBeRead = smartFile.getFileMeta().logicBlocks.get(firstBlockIndex).get(0);
//        int n = 0;
//        while(n++ != cursor){
//            fileInputStream.read();
//        }
//        while((n=fileInputStream.read())!=-1){
//            a = (char)n;
//            sb.append(a);
//        }
//    }
    public static void ifFileNotExist(File file){
        if(!file.exists()) {
            //先得到文件的上级目录，并创建上级目录，在创建文件
            if(!file.getParentFile().exists()) {
                file.getParentFile().getParentFile().mkdir();
            }
            file.getParentFile().mkdir();
            try {
                //创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
