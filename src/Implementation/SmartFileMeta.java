package Implementation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmartFileMeta implements Serializable {
    public List<ArrayList<SmartBlock>> logicBlocks;
    public String fileName;
    public int fileSize;
    String createTime;
    String lastModifiedTime;
    public static int LOGIC_BLOCK_NUM = 3;

    public SmartFileMeta() {
    }

    public SmartFileMeta(List<ArrayList<SmartBlock>> logicBlocks, String fileName, int fileSize, String createTime, String lastModifiedTime) {
        this.logicBlocks = logicBlocks;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.createTime = createTime;
        this.lastModifiedTime = lastModifiedTime;
    }
}
