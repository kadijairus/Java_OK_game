package packet;

public class PacketRespondSlotOccupancy implements GamePacket{
    public String slotName;
    public boolean isOccupied;

    public PacketRespondSlotOccupancy() {
    }

    public PacketRespondSlotOccupancy(String slotName, boolean isOccupied) {
        this.slotName = slotName;
        this.isOccupied = isOccupied;
    }
}

