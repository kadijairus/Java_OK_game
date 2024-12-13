package packet;

public class PacketCardPositionInfo implements GamePacket{
    public int x, y, cardID;

    /**
     * Send card movement coordinates
     */
    public PacketCardPositionInfo() {
        this.x = 0;
        this.y = 0;
        this.cardID = 1;
    }
}