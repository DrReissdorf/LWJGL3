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
        kryo.register(MessagePosition.class);

        kryo.register(String.class);

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                System.out.println("Receivedlistener... Object Class: "+object.getClass());

                if (object instanceof TestMessage) {
                    TestMessage request = (TestMessage)object;
                    System.out.println("Name: "+request.name+" time: "+request.time+" date: "+request.date);
                    server.sendToUDP(connection.getID(), "TestMessage erhalten!");
                }

                if (object instanceof MessagePosition) {
                    MessagePosition request = (MessagePosition)object;
                    Vec3[] positionVectors = request.positions;

                    for(int i=0 ; i<positionVectors.length ; i++) {
                        System.out.println("Vector "+(i+1)+": "+positionVectors[i]);
                    }

                    server.sendToUDP(connection.getID(), "MessagePosition erhalten!");
                }
            }
        });
    }
}
