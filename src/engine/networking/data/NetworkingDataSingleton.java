package engine.networking.data;

import engine.networking.entity.Player;

import java.util.ArrayList;

public class NetworkingDataSingleton {
    private static NetworkingDataSingleton networkingDataSingleton;
    private ArrayList<Player> players;
    private Player localPlayer;

    private NetworkingDataSingleton() {
        players = new ArrayList<>();
    }

    public static NetworkingDataSingleton getInstance() {
        if(networkingDataSingleton == null) networkingDataSingleton = new NetworkingDataSingleton();
        return networkingDataSingleton;
    }

    public void handlePlayer(Player receivedPlayer) {
        if(isPlayerAlreadyConnected(receivedPlayer)) {
            for(Player player : players) {
                if(player.id == receivedPlayer.id) {
                    player.position = receivedPlayer.position;
                    player.rotX = receivedPlayer.rotX;
                    player.rotY = receivedPlayer.rotY;
                    player.rotZ = receivedPlayer.rotZ;
                }
            }
        } else {
            addPlayer(receivedPlayer);
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public boolean isPlayerAlreadyConnected(Player newPlayer) {
        boolean ret = false;

        for(Player player : players) {
            if(player.id == newPlayer.id) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    public ArrayList<Player> getConnectedPlayers() {
        return players;
    }

    public void removePlayer(int id) {
        players.remove(id);
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public void setLocalPlayer(Player localPlayer) {
        this.localPlayer = localPlayer;
    }
}
