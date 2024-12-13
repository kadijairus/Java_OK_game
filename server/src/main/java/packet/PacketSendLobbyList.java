package packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketSendLobbyList implements GamePacket {
    public List<String> lobbyNames;
    public Map<String, Integer> playerCounts;

    public PacketSendLobbyList() {
        this.lobbyNames = new ArrayList<>();
        this.playerCounts = new HashMap<>();
    }
}
