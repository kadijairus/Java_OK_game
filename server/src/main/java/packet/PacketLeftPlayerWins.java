package packet;

public class PacketLeftPlayerWins implements GamePacket{
    public boolean leftPlayerWins;
    public String playerName;
    public String loserName;

    /**
     * Packet is used to inform the server that decider has chosen the right player as winner (rightPlayerWins changed to True).
     */
    public PacketLeftPlayerWins() {
        this.leftPlayerWins = false;
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
