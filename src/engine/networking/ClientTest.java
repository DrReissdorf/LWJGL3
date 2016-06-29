package engine.networking;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import math.Vec3;

import java.io.IOException;
import java.util.Date;

public class ClientTest {
    public static void main(String args[]) throws InterruptedException {
        Client client = new Client();
        client.start();

        try {
            client.connect(5000, "127.0.0.1", 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Register Classes. Order matters!! */
        Kryo kryoClient = client.getKryo();
        kryoClient.register(TestMessage.class);
        kryoClient.register(Date.class);

        kryoClient.register(Vec3.class);
        kryoClient.register(Vec3[].class);
        kryoClient.register(MessagePosition.class);

        kryoClient.register(String.class);

        TestMessage request = new TestMessage();
        request.name = "A CLIENT SENDS HIS REGARDS";
        request.time = System.currentTimeMillis();
        request.date = new Date();
        client.sendUDP(request);

        MessagePosition pos = new MessagePosition();

        Vec3[] positions = new Vec3[3];
        positions[0] = new Vec3(1f,2f,3f);
        positions[1] = new Vec3();
        positions[2] = new Vec3();
        pos.positions = positions;
        client.sendUDP(pos);

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof TestMessage) {
                    TestMessage response = (TestMessage)object;
                    System.out.println(response.name);
                }

                if (object instanceof String) {
                    System.out.println((String)object);
                }
            }
        });


        client.getUpdateThread().join();

    }
}
