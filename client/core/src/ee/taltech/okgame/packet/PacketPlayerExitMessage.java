package ee.taltech.okgame.packet;

public class PacketPlayerExitMessage implements GamePacket{
    public int currentPlayerId; // Or use a unique player ID

    // No-argument constructor for Kryo serialization
    public PacketPlayerExitMessage() {
    }

    // Argument constructor
    public PacketPlayerExitMessage(int currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    // Getter method
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
}
