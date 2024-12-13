package ee.taltech.okgame;

public interface PlayerIdReceivedListener {
    void onPlayerIdReceived(int playerId, String playerRole, String playerName);
}