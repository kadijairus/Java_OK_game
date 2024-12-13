package ee.taltech.okgame;

import ee.taltech.okgame.players.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<Integer, Player> players = new HashMap<>();
    private Integer selfId = null;

    private PlayerManager() {}

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public void addPlayer(int playerId, String role, String playerName) {
        if (players.containsKey(playerId)) {
            return;
        }
        players.put(playerId, new Player(playerId, role, playerName));
        GameStateManager.getInstance().playerUpdated();
    }

    public Player getPlayer(int playerId) {
        return players.get(playerId);
    }

    public void setSelfId(int selfId) {
        this.selfId = selfId;
    }

    public Player getSelf() {
        return selfId != null ? players.get(selfId) : null;
    }

    public Optional<String> getPlayerNameByRole(String role) {
        return players.values().stream()
                .filter(player -> player.getPlayerRole().equals(role))
                .map(Player::getPlayerName)
                .findFirst();
    }


}
