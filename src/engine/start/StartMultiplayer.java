package engine.start;

import com.esotericsoftware.kryonet.Client;
import engine.logic.Multiplayer;
import engine.networking.client.ClientBuilder;
import engine.networking.data.NetworkingDataSingleton;
import engine.networking.entity.Player;
import singleton.HolderSingleton;

public class StartMultiplayer {
    public static void main(String[] args) {
        Client client = ClientBuilder.createClient();

        Player localPlayer = new Player("First", HolderSingleton.getInstance().getMainCamera().getRoot().getPosition(),0,0,0);
        localPlayer.id = client.getID();
        NetworkingDataSingleton.getInstance().setLocalPlayer(localPlayer);

        new DebugThread().start();
        new Multiplayer(client).run();
    }

    private static class DebugThread extends Thread {
        public void run() {
            long oldTime = System.currentTimeMillis();
            long tempTime;

            System.out.println("\nUpdateThread started");

            while(true) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tempTime = System.currentTimeMillis();

                if(tempTime - oldTime > 1000) {
                    oldTime = tempTime;

                    System.out.println("\nUpdateThread : "+this.getName());
                    for(Player player : NetworkingDataSingleton.getInstance().getConnectedPlayers()) {
                        if(player.id != NetworkingDataSingleton.getInstance().getLocalPlayer().id) {
                            System.out.println("Player ID: "+player.id + " - Player Name: " +player.name);
                        }
                    }
                }
            }
        }
    }
}
