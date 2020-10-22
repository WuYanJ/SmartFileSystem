package Implementation;

import Interfaces.Id;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class SmartBlockManager implements Interfaces.BlockManager, Serializable {
    public SmartId bmId;
    public SmartBlockManager(SmartId bmId) {
        this.bmId = bmId;
    }

    @Override
    public SmartBlock getBlock(Id indexId) {
        return null;
    }

    @Override
    public SmartBlock newBlock(byte[] b) throws IOException {
        // BMx中的所有block
        File[] list = new File("./BlockManager/" + bmId).listFiles();
        int newBlockIdNum = list.length + 1;
        SmartId newBlockId = new SmartId("b" + newBlockIdNum);
        File block = new File("./BlockManager/" + bmId + "/" + newBlockId.id + ".data");
        FileOutputStream fileOutputStream = new FileOutputStream(block);
        if(!block.exists()) {
            //先得到文件的上级目录，并创建上级目录，在创建文件
            block.getParentFile().mkdir();
            try {
                //创建文件
                block.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileOutputStream.write(b);
        fileOutputStream.close();
        File blockMeta = new File("./BlockManager/" + bmId + "/" + newBlockId.id + ".meta");
        fileOutputStream = new FileOutputStream(blockMeta);
        if(!blockMeta.exists()) {
            blockMeta.getParentFile().mkdir();
            try {
                //创建文件
                blockMeta.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileOutputStream.write(b.length);
        fileOutputStream.close();
        SmartBlock newBlock = getBlock(newBlockId);
        return newBlock;
    }
}
