package engine.networking;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import math.Vec3;

import java.io.IOException;
import java.util.Date;

public class ServerTest {
    public static void main(String args[]) throws IOException {
        Server server = new Server();
        server.start();
        server.bind(54555, 54777);

        System.out.println("Server started");

        Kryo kryo = server.getKryo();
        kryo.register(TestMessage.class);
        kryo.register(Date.class);

        kryo.register(Vec3.class);
        kryo.register(Vec3[].class);
        kryo.register(float[].class);
        kryo.register(MessagePosition.class);


        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                System.out.println("Receivedlistener...");

                if (object instanceof TestMessage) {
                    TestMessage request = (TestMessage)object;
                    System.out.println("Name: "+request.name+" time: "+request.time+" date: "+request.date);
                }

                if (object instanceof MessagePosition) {
                    MessagePosition request = (MessagePosition)object;
                    Vec3[] floats = request.positions;

                    for(int i=0 ; i<floats.length ; i++) {
                        System.out.println("Floats: "+i+": "+floats[i]);
                    }
                }
            }
        });
    }
}
