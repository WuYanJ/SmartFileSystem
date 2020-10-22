package Implementation;

import java.io.Serializable;

public class SmartBlockMeta implements Serializable {
    String checkSum;
    int size;

    public SmartBlockMeta() {
    }

    public SmartBlockMeta(String checkSum, int size) {
        this.checkSum = checkSum;
        this.size = size;
    }
}
