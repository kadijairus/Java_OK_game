package ee.taltech.okgame.lobby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyManager {
    private Map<String, Lobby> lobbies;
    private static LobbyManager instance;
    private List<LobbyUpdateListener> listeners = new ArrayList<>();

    private LobbyManager() {
        lobbies = new HashMap<>();
        listeners = new ArrayList<>();
    }

    public static synchronized LobbyManager getInstance() {
        if (instance == null) {
            instance = new LobbyManager();
        }
        return instance;
    }

    public void addLobby(String lobbyName) {
        if (!lobbies.containsKey(lobbyName)) {
            lobbies.put(lobbyName, new Lobby(lobbyName));
        }
    }

    public void removeLobby(String lobbyName) {
        lobbies.remove(lobbyName);
    }

    public Lobby getLobby(String lobbyName) {
        return lobbies.get(lobbyName);
    }

    public boolean lobbyExists(String lobbyName) {
        return lobbies.containsKey(lobbyName);
    }

    public List<Lobby> getAllLobbies() {
        return new ArrayList<>(lobbies.values());
    }

    public List<String> getPlayersInLobby(String lobbyName) {
        if (lobbies.containsKey(lobbyName)) {
            return lobbies.get(lobbyName).getPlayers();
        }
        return new ArrayList<>(); // Return an empty list if the lobby does not exist
    }

    public void updateLobby(String lobbyName, List<String> playerNames) {
        Lobby lobby = lobbies.get(lobbyName);
        if (lobby != null) {
            lobby.setPlayers(playerNames);
            notifyListeners();
        } else {
            // Optionally handle the case where the lobby doesn't exist
            Lobby newLobby = new Lobby(lobbyName);
            newLobby.setPlayers(playerNames);
            lobbies.put(lobbyName, newLobby);
            notifyListeners();
        }
    }


    private String findLobbyNameById(int lobbyId) {
        // Placeholder return
        return "SomeLobbyName";
    }

    private void notifyListeners() {
        for (LobbyUpdateListener listener : listeners) {
            listener.onLobbyUpdate();
        }
    }

    public void addListener(LobbyUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LobbyUpdateListener listener) {
        listeners.remove(listener);
    }
}
