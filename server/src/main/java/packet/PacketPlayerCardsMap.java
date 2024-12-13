package packet;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class PacketPlayerCardsMap implements Serializable, GamePacket{
    private Map<Integer, List<Integer>> playerCardsMap;

    public PacketPlayerCardsMap(Map<Integer, List<Integer>> playerCardsMap) {
        this.playerCardsMap = playerCardsMap;
    }

    public Map<Integer, List<Integer>> getPlayerCardsMap() {
        return playerCardsMap;
    }
}
