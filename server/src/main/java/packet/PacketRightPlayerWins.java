package packet;

public class PacketRightPlayerWins implements GamePacket{
    public boolean rightPlayerWins;
    public String playerName;
    public String loserName;

    /**
     * Packet is used to inform the server that decider has chosen the right player as winner (rightPlayerWins changed to True).
     */
    public PacketRightPlayerWins() {
        this.rightPlayerWins = false;
        this.playerName = "";
        this.loserName = "";
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getLoserName() {
        return loserName;
    }
}
