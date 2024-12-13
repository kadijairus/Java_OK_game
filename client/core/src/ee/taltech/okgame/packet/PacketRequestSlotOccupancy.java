package ee.taltech.okgame.packet;
public class PacketRequestSlotOccupancy implements GamePacket {
    public String slotName;

    public PacketRequestSlotOccupancy() {
    }

    public PacketRequestSlotOccupancy(String slotName) {
        this.slotName = slotName;
    }
}
