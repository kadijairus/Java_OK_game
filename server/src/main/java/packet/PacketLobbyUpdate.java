package packet;

import java.util.List;

public class PacketLobbyUpdate implements GamePacket{
    public String lobbyName;
    public List<String> playerNames;  // Update with player names or IDs
    public boolean isGameStarting;
}
