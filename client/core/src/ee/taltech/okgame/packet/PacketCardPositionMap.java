package ee.taltech.okgame.packet;

import java.util.HashMap;
import java.util.Map;

public class PacketCardPositionMap implements GamePacket{

    private Map<Integer, PacketCardPositionInfo> cardPositionInfoMap;

    public PacketCardPositionMap() {
        this.cardPositionInfoMap = new HashMap<>();
    }

    /**
     * Change card positions on the gameboard.
     * Packet is used to send data of card positions to all players.
     * @param cardPositionInfoMap
     */
    public void setCardPositionInfoMap(Map<Integer, PacketCardPositionInfo> cardPositionInfoMap) {
        this.cardPositionInfoMap = cardPositionInfoMap;
    }
    public Map<Integer, PacketCardPositionInfo> getCardPositionInfoMap() {
        return cardPositionInfoMap;
    }
}
