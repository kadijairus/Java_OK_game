package ee.taltech.okgame.packet;

public class PacketCreateLobbyRequest implements GamePacket {
    public String lobbyName;
    public boolean isAiPlayer;
    public boolean isAiDecider;

    // No-argument constructor for Kryo serialization
    public PacketCreateLobbyRequest() {
    }

    // Constructor with arguments for easy creation and setting fields
    public PacketCreateLobbyRequest(String lobbyName, boolean isAiPlayer, boolean isAiDecider) {
        this.lobbyName = lobbyName;
        this.isAiPlayer = isAiPlayer;
        this.isAiDecider = isAiDecider;
    }
}

