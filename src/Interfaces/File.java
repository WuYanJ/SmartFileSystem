package Interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface File {
    int MOVE_CURR = 0;
    int MOVE_HEAD = 1;
    int MOVE_TAIL = 2;
    Id getFileId();
    FileManager getFileManager();
    byte[] read(int length) throws IOException, ClassNotFoundException;
    void write(byte[] b) throws IOException, ClassNotFoundException;
    default long pos() throws IOException, ClassNotFoundException {
        return move(0, MOVE_CURR);
    }
    long move(long offset, int where) throws IOException, ClassNotFoundException;
    //使⽤buffer的同学需要实现
    void close();
    long size() throws IOException, ClassNotFoundException;
    void setSize(long newSize) throws IOException, ClassNotFoundException;
}
