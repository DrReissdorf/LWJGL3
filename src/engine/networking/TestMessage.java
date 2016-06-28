package engine.networking;

import java.util.Date;

public class TestMessage {
    public String name;
    public long time;
    public Date date;

    public TestMessage() {
        this.name = null;
        this.time = 0;
        this.date = null;
    }

    public TestMessage(String name, long time, Date date) {
        this.name = name;
        this.time = time;
        this.date = date;
    }
}
