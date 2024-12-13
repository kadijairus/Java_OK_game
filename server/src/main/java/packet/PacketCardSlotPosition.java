package packet;

import java.util.HashMap;
import java.util.Map;

public class PacketCardSlotPosition implements GamePacket{

    public Map<Integer, String> cardSlot;


    /**
     * Packet is currently not in use.
     * Could be send final positions of cards, not stable movements.
     */
    public PacketCardSlotPosition() {
    }
    public PacketCardSlotPosition(Integer cardId, String slotName) {
        this.cardSlot = new HashMap<>();
        this.cardSlot.put(cardId, slotName);
    }

    public PacketCardSlotPosition(Map<Integer, String> cardSlot) {
        this.cardSlot = cardSlot;
    }

}
