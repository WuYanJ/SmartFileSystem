package Implementation;

import java.io.Serializable;

public class SmartId implements Interfaces.Id, Serializable {
    String id;
    public SmartId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
