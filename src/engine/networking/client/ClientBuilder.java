package engine.networking.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import engine.networking.data.NetworkingDataSingleton;
import engine.networking.entity.Player;
import math.Vec3;
import singleton.HolderSingleton;

import java.io.IOException;

public class ClientBuilder {
    public static Client createClient() {
        Client client = new Client();
        client.start();

        try {
            client.connect(5000, "127.0.0.1", 54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cant connect to server!");
            System.exit(-1);
        }

        /* Register Classes. Order matters!! */
        Kryo kryoClient = client.getKryo();
        kryoClient.register(Vec3.class);
        kryoClient.register(Player.class);

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof Player) {
                    Player player = (Player)object;
                    NetworkingDataSingleton.getInstance().handlePlayer(player);
                }
            }
        });

        return client;
    }
}