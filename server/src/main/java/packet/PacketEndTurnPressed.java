package packet;

public class PacketEndTurnPressed implements GamePacket{

    public boolean endTurnPressed;

    /**
     * Packet is used to inform the server that player has completed the round. (endTurnPressed changed to True).
     */
    public PacketEndTurnPressed() {
        this.endTurnPressed= false;
    }
}

