package ee.taltech.okgame.lobby;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private static final int MAX_PLAYERS = 3;
    private String name;
    private List<String> players;

    public Lobby(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    public void addPlayer(String playerName) {
        if (!players.contains(playerName) && players.size() < MAX_PLAYERS) {
            players.add(playerName);
        }
    }

    public void removePlayer(String playerName) {
        players.remove(playerName);
    }

    // Sets the entire list of players for the lobby.
    public void setPlayers(List<String> newPlayers) {
        this.players.clear();
        if (newPlayers.size() <= MAX_PLAYERS) {
            this.players.addAll(newPlayers);
        } else {
            // If there are more players than MAX_PLAYERS, add only up to the maximum allowed.
            this.players.addAll(newPlayers.subList(0, MAX_PLAYERS));
        }
    }

    public List<String> getPlayers() {
        return new ArrayList<>(players);
    }

    public String getName() {
        return name;
    }

    public boolean isFull() {
        return players.size() == MAX_PLAYERS;
    }
}
