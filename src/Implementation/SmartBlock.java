package Implementation;

import Interfaces.BlockManager;
import Interfaces.Id;
import Utils.FuncUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SmartBlock implements Interfaces.Block, Serializable {
    private SmartId bId;
    private SmartBlockManager blockManager;
    public static final int blockSize = 512;
    public SmartBlockMeta blockMeta;
    public SmartBlock(SmartId bId, SmartBlockManager blockManager) {
        this.bId = bId;
        this.blockManager = blockManager;
    }

    @Override
    public SmartId getIndexId() {
        return bId;
    }

    @Override
    public SmartBlockManager getBlockManager() {
        return blockManager;
    }

    @Override
    public byte[] read() throws IOException {
        int n;
        char a;
        int i = 0;
        byte[] data = new byte[10 * 1024];
        File thisBlockFile = new File("./BlockManager/" + blockManager.bmId.getId() + "/" + bId.getId() + "/" + bId.getId() + ".data");
        FuncUtils.ifFileNotExist(thisBlockFile);
        FileInputStream fileInputStream = new FileInputStream(thisBlockFile);
        while ((n = fileInputStream.read()) != -1) {
            data[i++] = (byte) n;
        }
        byte[] result = new byte[i];
        System.arraycopy(data, 0, result, 0, i);
        return result;
    }

    @Override
    public int blockSize() {
        return blockSize;
    }

    public SmartBlockMeta getBlockMeta() throws IOException, ClassNotFoundException {
        File file = new File("./BlockManager/" + blockManager.bmId.getId() + "/" + bId.getId() + "/" + bId.getId() + ".meta");

        System.out.println("./BlockManager/" + blockManager.bmId.getId() + "/" + bId.getId() + "/" + bId.getId() + ".meta");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        blockMeta = (SmartBlockMeta) ois.readObject();
        return blockMeta;
    }

    public void updateBlockMetadata(String checksum, int newBlockSize, SmartBlockMeta blockMeta) throws IOException {
        blockMeta.checkSum = checksum;
        blockMeta.size = newBlockSize;
        File file = new File("./BlockManager/" + blockManager.bmId.getId() + "/" + bId.getId() + "/" + bId.getId() + ".meta");
//        File file = new File("./FileManager/FM1/f1.meta");
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(blockMeta);
        oos.close();
    }
}
