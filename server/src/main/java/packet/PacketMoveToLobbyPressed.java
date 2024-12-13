package packet;

public class PacketMoveToLobbyPressed implements GamePacket {
    public boolean playPressed;
    public String playerName;

    // Default constructor
    public PacketMoveToLobbyPressed() {
    }

    // Constructor with playerName
    public PacketMoveToLobbyPressed(String playerName) {
        this.playPressed = true;  // Set true when packet is created
        this.playerName = playerName;
    }

    // Getter and setter methods
    public boolean isPlayPressed() {
        return playPressed;
    }

    public void setPlayPressed(boolean playPressed) {
        this.playPressed = playPressed;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}