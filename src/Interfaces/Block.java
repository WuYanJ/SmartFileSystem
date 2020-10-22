package Interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();
    byte[] read() throws IOException;
    int blockSize();
}
