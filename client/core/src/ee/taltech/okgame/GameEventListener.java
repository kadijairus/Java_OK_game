
package ee.taltech.okgame;

public interface GameEventListener {
    void onGameReady();
    void onPlayerIdReceived(int playerId, String playerRole, String playerName);
}

