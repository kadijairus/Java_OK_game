package packet;

public class PacketCreateLobbyResponse implements GamePacket{
    public boolean success;
    public int lobbyId;
    public String message;  // Optional: error message or other notifications
}
