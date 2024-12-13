package packet;

import java.util.Map;
import java.util.HashMap;


public class PacketSlotOccupancy implements GamePacket{
    public Map<String, Boolean> slotOccupancy;

    public PacketSlotOccupancy() {
    }

    // Constructor for single slot update
    public PacketSlotOccupancy(String slotName, boolean isOccupied) {
        this.slotOccupancy = new HashMap<>();
        this.slotOccupancy.put(slotName, isOccupied);
    }

    public PacketSlotOccupancy(Map<String, Boolean> slotOccupancy) {
        this.slotOccupancy = slotOccupancy;
    }
}
