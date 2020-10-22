import Implementation.*;
import Interfaces.Block;
import Interfaces.Id;
import Utils.FuncUtils;
import org.junit.Test;
import Exception.ErrorCode;
import java.io.BufferedReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        SmartId fmId;
//        String fileName;
//        System.out.println("Please select an operation:" +
//                "\n A for smart-cat, B for smart-hex, C for smart-write, D for smart-copy.");
//        Scanner input = new Scanner(System.in);
//        char opCode = input.next().charAt(0);
//        switch (opCode) {
//            case 'A':
//                System.out.println("FileManager ID:");
//                fmId = new SmartId(input.next());
//                System.out.println("File name:");
//                fileName = input.next();
//                smart_cat(fileName, new SmartFileManager(fmId));
//                input.close();
//                break;
//            case 'B':
//                System.out.println("BlockManager ID:");
//                SmartId bmId = new SmartId(input.next());
//                System.out.println("Block ID:");
//                SmartId bId = new SmartId(input.next());
//                smart_hex(new SmartBlock(bId, new SmartBlockManager(bmId)));
//                break;
//            case 'C':
//                System.out.println("FileManager ID:");
//                fmId = new SmartId(input.next());
//                System.out.println("File name:");
//                fileName = input.next();
//                System.out.println("Where to insert:");
//                int index = input.nextInt();
//                smart_write(fileName, new SmartFileManager(fmId), index);
//                break;
//            case 'D':
//                System.out.println("D");
//                break;
//        }

//
//        File file = new File("./FileManager/FM1/f1.meta");
//        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
//
//        SmartFileMeta fileMeta = new SmartFileMeta(null, "filename", 0, null, null);
//        oos.writeObject(fileMeta);
//        oos.close();
//
//        smart_write("filename", new SmartFileManager(new SmartId("FM1")), 1700);

        smart_cat("filename", new SmartFileManager(new SmartId("FM1")));

//        smart_copy("filename", new SmartFileManager(new SmartId("FM1")), "toFileName", new SmartFileManager(new SmartId("FM2")));
 //         单独测试modifyFileMeta方法
//        modifyFileMeta(new SmartFile(new SmartId("f1"), new SmartFileManager(new SmartId("FM1"))), new SmartFileManager(new SmartId("FM1")), 100);

//         单独测试smart_hex方法
//        SmartBlock block = new SmartBlock(new SmartId("b1"), new SmartBlockManager(new SmartId("BM1")));
//        smart_hex(block);
    }

    // 直接读取filedata
    public static void smart_cat(String fileName, SmartFileManager fileManager) throws IOException, ClassNotFoundException {
        // 通过参数找到smartFile，
        SmartFile smartFile = fileManager.getFileByName(fileName);
        // 获取用户输入：从文件什么位置；读取多大长度
//        Scanner input = new Scanner(System.in);
//        System.out.println("从哪里开始读取数据？A. 文件头 B. 文件光标位置");
//        String s = input.next();
//        int where = s.equals("A")? 0:1;
        byte[] fileBytes = null;
        try{
            fileBytes = smartFile.read((int) (smartFile.size() - smartFile.cursor));
        } catch (ErrorCode e){
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }
        System.out.println(new String(fileBytes));
    }

    // 读取block的data并⽤16进制的形式打印到控制台
    public static void smart_hex(Block block) throws IOException {
        SmartBlock smartBlock = (SmartBlock) block;
        SmartId bmId = smartBlock.getBlockManager().bmId;
        File blockData = new File("./BlockManager/" + bmId.getId() + "/" + smartBlock.getIndexId().getId() + "/" + smartBlock.getIndexId().getId() + ".data");
        InputStream inputStream = new FileInputStream(blockData);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] flush = new byte[1024 * 10]; //设置缓冲，这样便于传输，大大提高传输效率
        int len = -1; //设置每次传输的个数,若没有缓冲的数据大，则返回剩下的数据，没有数据返回-1
        while((len = inputStream.read(flush)) != -1) {
            byteArrayOutputStream.write(flush,0,len);//每次读取len长度数据后，将其写出
        }
        byteArrayOutputStream.flush(); //刷新管道数据
        byte[] byteArray = byteArrayOutputStream.toByteArray(); //最终获得的字节数组

        for (int i = 0; i < byteArray.length; i++) {
            String hex = Integer.toHexString(byteArray[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase());
        }
    }

    // 将写⼊指针移动到指定位置后，开始读取⽤户数据，并且写⼊到⽂件中
    // index 是光标移动多少位（从文件头开始移动）
    public static void smart_write(String fileName, SmartFileManager fileManager, int index) throws IOException, ClassNotFoundException {
        // 读取用户输入
        Scanner inputStringScanner = new Scanner(System.in);
        System.out.println("Input String that you want to insert:");
        String insertString = inputStringScanner.nextLine();
        // 找到要写的文件,这个smartFile是有id和FManager的
        SmartFile smartFile = fileManager.getFileByName(fileName);
        // 移动光标
        smartFile.move(index, Interfaces.File.MOVE_HEAD);
        System.out.println("cursor:" + smartFile.cursor);
        // 写数据
        smartFile.write(insertString.getBytes());
    }

//    直接复制file的metadata
    public static void smart_copy(String fromName, SmartFileManager fromFileManager, String toName,  SmartFileManager toFileManager) throws IOException, ClassNotFoundException {
        // 找到fromFile
        SmartFile fromSmartFile = fromFileManager.getFileByName(fromName);
        File fromFile = new File("./FileManager/" + fromFileManager.getFmId().getId() + "/" + fromSmartFile.getFileId().getId() + ".meta");
//        fromfile的metadata读出来
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fromFile));
        SmartFileMeta fileMeta = (SmartFileMeta) ois.readObject();
        fileMeta.fileName = toName;
        File[] list = new File("./FileManager/" + toFileManager.getFmId().getId()).listFiles();
        // 用接下去的fileID新建toFile
        int newFileId;
        if(list == null) {
            newFileId = 1;
        } else {
            newFileId = list.length + 1;
        }
        SmartId toFileId = new SmartId("f" + newFileId);
        File toFile = new File("./FileManager/" + toFileManager.getFmId().getId() + "/" + toFileId.getId() + ".meta");
        FuncUtils.ifFileNotExist(toFile);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(toFile));
        oos.writeObject(fileMeta);
        oos.close();
    }
}
