package Implementation;

import Interfaces.FileManager;
import Interfaces.Id;
import Utils.FuncUtils;
import Utils.MD5Util;
import Exception.ErrorCode;


import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmartFile implements Interfaces.File {
    SmartId fId;
    SmartFileManager fileManager;
    public int cursor; // 光标成员变量
    private SmartFileMeta fileMeta;
    byte[] buf;

    ArrayList<SmartBlock> logicBlock = new ArrayList<>();
    public SmartFile() {
    }

    public SmartFile(SmartId fId, SmartFileManager fileManager, String filename) {
        this.fId = fId;
        this.fileManager = fileManager;
        this.fileMeta = new SmartFileMeta(null, filename, 0, "2020-2-2", "2020-3-1");
    }
    public byte[] getBuf() throws IOException, ClassNotFoundException {
        return bufRead(0);
    }
//    每次用fid找meta文件读出来，并不是直接从对象读
    public SmartFileMeta getFileMeta() throws IOException, ClassNotFoundException {
        File file = new File("./FileManager/" + fileManager.fmId.getId() + "/" + fId.getId() + ".meta");

        System.out.println("./FileManager/" + fileManager.fmId.getId() + "/" + fId.getId() + ".meta");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        fileMeta = (SmartFileMeta) ois.readObject();
        return fileMeta;
    }

    @Override
    public SmartId getFileId() {
        return fId;
    }

    @Override
    public FileManager getFileManager() {
        return fileManager;
    }

    @Override
    public byte[] read(int length) throws IOException, ClassNotFoundException, ErrorCode {
        buf = bufRead(cursor);
        byte[] result = new byte[length];
        System.arraycopy(buf, 0, result, 0, length);
        System.out.println("result.length"+result.length);
        return result;
    }

    public byte[] bufRead(int bufCursor) throws IOException, ClassNotFoundException, ErrorCode {
        SmartId bmId;
        SmartId bId;
        // 从cursor开始读完整个文件
        System.out.println("cursor"+bufCursor);
        int blockStartIndex = bufCursor / SmartBlock.blockSize; // 从第 blockStartIndex 个 block 开始读
        int firstOffset = bufCursor - SmartBlock.blockSize * blockStartIndex; // 从第 offset 个 byte开始读
        byte[] toEnd = new byte[100*1024];

        String currentCheckSum;
        SmartBlock firstBlockToBeRead=null;
        // 在此checksum检查
        for(int k = 0; k < getFileMeta().logicBlocks.get(blockStartIndex).size(); k ++){
            bmId = getFileMeta().logicBlocks.get(blockStartIndex).get(k).getBlockManager().bmId;
            bId = getFileMeta().logicBlocks.get(blockStartIndex).get(k).getIndexId();
            currentCheckSum = MD5Util.md5HashCode("./BlockManager/" + bmId.getId() + "/" + bId.getId() + "/" + bId.getId() + ".data");
            System.out.println("-"+currentCheckSum);
            System.out.println(getFileMeta().logicBlocks.get(blockStartIndex).get(k).getBlockMeta().checkSum);
            if(currentCheckSum.equals(getFileMeta().logicBlocks.get(blockStartIndex).get(k).getBlockMeta().checkSum)){
                System.out.println("firstBlockToBeRead是第几个呢"+k);
                firstBlockToBeRead = getFileMeta().logicBlocks.get(blockStartIndex).get(k);
                break;
            }
        }
        if(firstBlockToBeRead == null){
            throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
        }
        int n;
        int i = 0;
        File firstBlockFile = new File("./BlockManager/" + firstBlockToBeRead.getBlockManager().bmId.getId() + "/" + firstBlockToBeRead.getIndexId().getId() + "/" + firstBlockToBeRead.getIndexId().getId() + ".data");
        FuncUtils.ifFileNotExist(firstBlockFile);
        FileInputStream fileInputStream = new FileInputStream(firstBlockFile);
        while(firstOffset -- != 0) {fileInputStream.read(); }
        while((n=fileInputStream.read()) != -1){
            toEnd[i++] = (byte) n;
        }
        SmartBlock currentBlockToBeRead = null;
        for (int j = blockStartIndex+1; j < getFileMeta().logicBlocks.size() ; j++) {
            System.out.println("getFileMeta().logicBlocks.size()"+getFileMeta().logicBlocks.size());
            // 每一次读logicblocks的某一串相同数据块时都checksum一下
            for(int k = 0; k < getFileMeta().logicBlocks.get(j).size(); k ++){
                bmId = getFileMeta().logicBlocks.get(j).get(k).getBlockManager().bmId;
                bId = getFileMeta().logicBlocks.get(j).get(k).getIndexId();
                currentCheckSum = MD5Util.md5HashCode("./BlockManager/" + bmId.getId() + "/" + bId.getId() + "/" + bId.getId() + ".data");
                if(currentCheckSum.equals(getFileMeta().logicBlocks.get(j).get(k).getBlockMeta().checkSum)){
                    currentBlockToBeRead = getFileMeta().logicBlocks.get(j).get(k);
                    break;
                }
            }
            if(currentBlockToBeRead == null){
                throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
            }
            System.arraycopy(currentBlockToBeRead.read(), 0, toEnd, i, currentBlockToBeRead.read().length);
            i += currentBlockToBeRead.read().length;
            System.out.println("i:"+i);
        }
        return toEnd;
    }

    @Override
    // TODO write方法只是把数据写进buffer，close的时候再一并写回
    public void write(byte[] b) throws IOException, ClassNotFoundException {

        int blockStartIndex = cursor / SmartBlock.blockSize; // 从第 blockStartIndex 个 block 插入

        System.out.println("从第几个block开始修改:"+blockStartIndex);
        SmartBlock blockStart;
        if(getFileMeta().logicBlocks == null) {
            newWrite(b);
            return;
        }
        blockStart = getFileMeta().logicBlocks.get(blockStartIndex).get(0);
        int firstOffset = cursor - SmartBlock.blockSize * blockStartIndex; // 从第 offset 个 byte
        int n;
        int i = 0;
        // 第一个要操作的block前面的数据remainsInBlock
        byte[] tempRemainsInBlock = new byte[10*1024];

        File blockStartFile = new File("./BlockManager/" + blockStart.getBlockManager().bmId.getId() + "/" + blockStart.getIndexId().getId() + "/" + blockStart.getIndexId().getId() + ".data");
        FuncUtils.ifFileNotExist(blockStartFile);
        FileInputStream fileInputStream = new FileInputStream(blockStartFile);
        while((n=fileInputStream.read()) != -1 && firstOffset -- != 0){
            tempRemainsInBlock[i++] = (byte) n;
        }
        byte[] remainsInBlock = new byte[i];
        System.arraycopy(tempRemainsInBlock, 0, remainsInBlock, 0, i);
        // + 要写进去的数据b
        // + 第一个要操作的block后面的所有数据latter
        byte[] latter = null;
        try{
            latter = read((int) (this.size() - cursor));
        } catch (ErrorCode e){
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }
        if(latter == null){
            latter = new byte[10*1024];
        }
        // 拼起来
        byte[] newLatterData = new byte[remainsInBlock.length + b.length + latter.length];
        System.arraycopy(remainsInBlock, 0, newLatterData, 0, remainsInBlock.length);
        System.arraycopy(b, 0, newLatterData, remainsInBlock.length, b.length);
        System.arraycopy(latter, 0, newLatterData, remainsInBlock.length + b.length, latter.length);
        // 随机找一个BM下的随机一个bx，把512byte数据写进这个block，把这个SmartBlock add进file的logicblocks中
        String blockManagerId;
        String blockId;
        FileOutputStream fileOutputStream;
        SmartBlock newLogicBlock;
        ArrayList<SmartBlock> newLogicBlocks = new ArrayList<>();
        File newBlockFile;
        File newBlockMetaFile;
        List<ArrayList<SmartBlock>> logicBlocks = getFileMeta().logicBlocks;
        SmartBlockMeta blockMeta;
        System.out.println("newLatterData"+newLatterData.length);
        int blockNum = newLatterData.length % SmartBlock.blockSize == 0 ? newLatterData.length / SmartBlock.blockSize : newLatterData.length / SmartBlock.blockSize + 1;

        System.out.println("blockStartIndex"+blockStartIndex);
        System.out.println("blockNum"+blockNum);
        int totalBlock = blockStartIndex + blockNum;
        System.out.println("本来有多少logicblock"+getFileMeta().logicBlocks.size());
        System.out.println("现在有多少logicblock"+totalBlock);
        //        补全logicblock的长度
        for(int m = 0;m < totalBlock - getFileMeta().logicBlocks.size() ; m++){
            logicBlocks.add(new ArrayList<>());
        }
//        需要粘上去的物理block数
        for(int j = 0; j < blockNum; j++) {
            //            把SmartBlock对象append到内层ArrayList操作在内层for循环执行
            for (int m = 0; m < SmartFileMeta.LOGIC_BLOCK_NUM; m++) {
                blockManagerId = "BM" + (int) (Math.random() * 5 + 1);
                File[] list = new File("./BlockManager/" + blockManagerId).listFiles();
                int newBlockIdNum;
                if (list == null) {
                    newBlockIdNum = 1;
                } else {
                    newBlockIdNum = list.length + 1;
                }
                blockId = "b" + newBlockIdNum;
                System.out.println("blockManagerId-" + blockManagerId + " newBlockIdNum-" + newBlockIdNum);
                newBlockFile = new File("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".data");
                newBlockMetaFile = new File("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".meta");
                FuncUtils.ifFileNotExist(newBlockFile);
                FuncUtils.ifFileNotExist(newBlockMetaFile);
                fileOutputStream = new FileOutputStream(newBlockFile);
                if (j == blockNum - 1) {
                    fileOutputStream.write(newLatterData, 512 * j, newLatterData.length - 512 * j);
                } else {
                    fileOutputStream.write(newLatterData, 512 * j, 512);
                }
                fileOutputStream.close();

                newLogicBlock = new SmartBlock(new SmartId(blockId), new SmartBlockManager(new SmartId(blockManagerId))); // 写的数据块
                blockMeta = new SmartBlockMeta();
                newLogicBlock.updateBlockMetadata(MD5Util.md5HashCode("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".data"), newLogicBlock.blockSize(), blockMeta);
                newLogicBlocks.add(newLogicBlock); // 一模一样的数据块们
            }
            System.out.println("冲冲冲" + blockStartIndex + j);
            logicBlocks.set(blockStartIndex + j, newLogicBlocks); // 把这一串一模一样的数据块存进logicBlocks最外层链表里
            newLogicBlocks = new ArrayList<>();
        }
        String lastModifiedTime = new Date().toString();
        int newFileSize =  cursor + b.length + latter.length;
        cursor += b.length;
        updateMetadata(lastModifiedTime, newFileSize, logicBlocks, fileMeta);
    }

    private void newWrite(byte[] b) throws IOException, ClassNotFoundException {
        int blockNum = b.length % SmartBlock.blockSize == 0 ? b.length / SmartBlock.blockSize : b.length / SmartBlock.blockSize + 1;
        List<ArrayList<SmartBlock>> logicBlocks = new ArrayList<>();
        ArrayList<SmartBlock> logicBlock = new ArrayList<>();;
        String blockManagerId;
        File[] list;
        String blockId;
        File newBlockFile;
        SmartBlock newLogicBlock;
        File newBlockMetaFile;
        SmartBlockMeta blockMeta;
        for(int j = 0;j < blockNum;j++) {
//            把SmartBlock对象append到内层ArrayList操作在内层for循环执行
            for(int m = 0;m < SmartFileMeta.LOGIC_BLOCK_NUM;m++) {
                blockManagerId = "BM" + (int) (Math.random() * 5 + 1);
                list = new File("./BlockManager/" + blockManagerId).listFiles();
                int newBlockIdNum;
                if (list == null) {
                    newBlockIdNum = 1;
                } else {
                    newBlockIdNum = list.length + 1;
                }
                blockId = "b" + newBlockIdNum;
                System.out.println("blockManagerId" + blockManagerId + " newBlockIdNum" + newBlockIdNum);
                newBlockFile = new File("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".data");
                newBlockMetaFile = new File("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".meta");
                FuncUtils.ifFileNotExist(newBlockMetaFile);
                FuncUtils.ifFileNotExist(newBlockFile);
                FileOutputStream fileOutputStream = new FileOutputStream(newBlockFile);
                if (j == blockNum - 1) {
                    fileOutputStream.write(b, 512 * j, b.length - 512 * j);
                } else {
                    fileOutputStream.write(b, 512 * j, 512);
                }
                newLogicBlock = new SmartBlock(new SmartId(blockId), new SmartBlockManager(new SmartId(blockManagerId))); // 写的数据块
                blockMeta = new SmartBlockMeta();
                newLogicBlock.updateBlockMetadata(MD5Util.md5HashCode("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".data"), newLogicBlock.blockSize(), blockMeta);
                logicBlock.add(newLogicBlock); // 一模一样的数据块们
            }
            logicBlocks.add(new ArrayList<>());
            logicBlocks.set(j, logicBlock);
            logicBlock = new ArrayList<>();
        }
        updateMetadata(new Date().toString(), b.length, logicBlocks, fileMeta);
    }

    /*
    1. 更新对象
    2. 写.meta文件
     */
    private void updateMetadata(String newLastModifiedTime, int newFileSize, List<ArrayList<SmartBlock>> newLogicBlocks, SmartFileMeta fileMeta) throws IOException {
        fileMeta.lastModifiedTime = newLastModifiedTime;
        System.out.println("newfilesize:"+newFileSize);
        fileMeta.fileSize = newFileSize;
        fileMeta.logicBlocks = newLogicBlocks;
        File file = new File("./FileManager/" + fileManager.fmId.getId() + "/" + fId.getId() + ".meta");
//        File file = new File("./FileManager/FM1/f1.meta");
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(fileMeta);
        oos.close();
    }


    @Override
    // 用来修改光标位置
    // 抛个errorcode？
    public long move(long offset, int where) throws IOException, ClassNotFoundException {
        if(size() == 0){
            cursor = 0;
            return cursor;
        }
        switch (where) {
            case 0:
                cursor = (cursor + offset) <= fileMeta.fileSize ? (int)(cursor + offset) : fileMeta.fileSize - 1;
                if((cursor + offset) > fileMeta.fileSize) {
                    System.out.println("您选择光标从当前位置开始移动，移动位置超出文件最大长度，已为您将光标移到文件尾部");
                }
                break;
            case 1:
                cursor = offset <= fileMeta.fileSize ? (int) offset : fileMeta.fileSize - 1;
                if(offset > fileMeta.fileSize) {
                    System.out.println("您选择光标从文件头部开始移动，移动位置超出文件最大长度，已为您将光标移到文件尾部");
                }
                break;
            case 2:
                cursor = offset < fileMeta.fileSize ? (int) (fileMeta.fileSize - 1 - offset) : 0;
                if(offset > fileMeta.fileSize) {
                    System.out.println("您选择光标从文件尾部开始移动，移动位置在文件头部之前，已为您将光标移到文件头部");
                }
                break;
        }
        return cursor;
    }

    // 清空buffer中的数据
    // TODO 把buf中的脏数据写回文件
    @Override
    public void close() {
        buf = null;
    }

    @Override
    public long size() throws IOException, ClassNotFoundException {
        System.out.println("size"+getFileMeta().fileSize);
        return getFileMeta().fileSize;
    }

    @Override
    public void setSize(long newSize) throws IOException, ClassNotFoundException {
        List<ArrayList<SmartBlock>> logicBlocks = new ArrayList<>();
        // 先得到最后一个logicBlock所在的data块
        SmartBlock lastBlock = getFileMeta().logicBlocks.get(getFileMeta().logicBlocks.size() - 1).get(0);
        File lastBlockData = new File("./BlockManager/" + lastBlock.getBlockManager().bmId.getId() + "/" + lastBlock.getIndexId().getId() + "/" + lastBlock.getIndexId().getId() + ".data");
        String blockManagerId;
        String blockId;
        File newBlockFile;
        FileOutputStream fileOutputStream;
        SmartBlock newLogicBlock;
        ArrayList newLogicBlocks = new ArrayList();

        if(newSize > getFileMeta().fileSize) {
            // 计算要新建多少block
            int extraSize = (int) (newSize - getFileMeta().fileSize);
            FileInputStream fileInputStream = new FileInputStream(lastBlockData);
            int n;
            int i = 0;
            byte[] data = new byte[10*1024];
            while((n=fileInputStream.read()) != 0){
                data[i++] = (byte) n;
            }
            byte[] newData = new byte[i + extraSize];
            System.arraycopy(data, 0, newData, 0, i);
            byte[] zeros = new byte[extraSize];
            int j = 0;
            for(int m = 0;m < extraSize;m++){
                zeros[j++] = 0;
            }
            System.arraycopy(zeros, 0, newData, newData.length, extraSize);

            int blockNum = newData.length % SmartBlock.blockSize == 0 ? newData.length / SmartBlock.blockSize:newData.length / SmartBlock.blockSize+1;
            // 新建block，赋bId

            for(int k = 0; j < blockNum; j++) {
                //            把SmartBlock对象append到内层ArrayList操作在内层for循环执行
                for (int m = 0; m < SmartFileMeta.LOGIC_BLOCK_NUM; m++) {
                    blockManagerId = "BM" + (int) (Math.random() * 5 + 1);
                    File[] list = new File("./BlockManager/" + blockManagerId).listFiles();
                    int newBlockIdNum;
                    if (list == null) {
                        newBlockIdNum = 1;
                    } else {
                        newBlockIdNum = list.length + 1;
                    }
                    blockId = "b" + newBlockIdNum;
                    System.out.println("blockManagerId-" + blockManagerId + " newBlockIdNum-" + newBlockIdNum);
                    newBlockFile = new File("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".data");
                    FuncUtils.ifFileNotExist(newBlockFile);
                    fileOutputStream = new FileOutputStream(newBlockFile);
                    if (k == blockNum - 1) {
                        fileOutputStream.write(newData, 512 * j, newData.length - 512 * j);
                    } else {
                        fileOutputStream.write(newData, 512 * j, 512);
                    }
                    fileOutputStream.close();
                    newLogicBlock = new SmartBlock(new SmartId(blockId), new SmartBlockManager(new SmartId(blockManagerId))); // 写的数据块
                    newLogicBlocks = new ArrayList<>();
                    System.out.println("+++++++" + newLogicBlock.getBlockManager().bmId.getId() + "/" + newLogicBlock.getIndexId().getId());
                    newLogicBlocks.add(newLogicBlock); // 一模一样的数据块们
                }
                logicBlocks.set(getFileMeta().logicBlocks.size() - 1 + k, newLogicBlocks);
            }
            updateMetadata(new Date().toString(), (int) newSize, logicBlocks, fileMeta);
        } else if (newSize < getFileMeta().fileSize) {
            // 先得到newSize的最后一个logicBlock所在的data块
            if(newSize % SmartBlock.blockSize == 0){
                int lastIndex = (int) (newSize / SmartBlock.blockSize) - 1;
                logicBlocks = getFileMeta().logicBlocks;
                for(int i = lastIndex;i < getFileMeta().logicBlocks.size();i++){
                    logicBlocks.remove(i);
                }
                updateMetadata(new Date().toString(), (int) newSize, logicBlocks, fileMeta);
            } else {
                int lastIndex = (int) (newSize / SmartBlock.blockSize);
                // 把last剩下的读出来，写进新block
                FileInputStream fileInputStream = new FileInputStream(lastBlockData);
                int remains = (int) (newSize % SmartBlock.blockSize);
                int n;
                int i = 0;
                byte[] remainBytes = new byte[(int) (newSize - newSize / SmartBlock.blockSize)];
                while((n=fileInputStream.read())!=0 && remains--!=0){
                    remainBytes[i++] = (byte) n;
                }
                for(int p = lastIndex;p < getFileMeta().logicBlocks.size();p++){
                    logicBlocks.remove(p);
                }

                for(int z = 0;z < SmartFileMeta.LOGIC_BLOCK_NUM;z++) {
                    blockManagerId = "BM" + (int) (Math.random() * 5 + 1);
                    File[] list = new File("./BlockManager/" + blockManagerId).listFiles();
                    int newBlockIdNum;
                    if (list == null) {
                        newBlockIdNum = 1;
                    } else {
                        newBlockIdNum = list.length + 1;
                    }
                    blockId = "b" + newBlockIdNum;
                    newBlockFile = new File("./BlockManager/" + blockManagerId + "/" + blockId + "/" + blockId + ".data");
                    FuncUtils.ifFileNotExist(newBlockFile);
                    SmartBlock newLast = new SmartBlock(new SmartId(blockId), new SmartBlockManager(new SmartId(blockManagerId)));
                    fileOutputStream = new FileOutputStream(newBlockFile);
                    fileOutputStream.write(remainBytes, 0, remainBytes.length);
                    fileOutputStream.close();
                    logicBlock.add(newLast);
                }
                logicBlocks.add(logicBlock);
            }
        } else return;
    }
}
