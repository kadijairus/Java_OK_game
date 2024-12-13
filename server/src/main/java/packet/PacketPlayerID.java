package packet;

public class PacketPlayerID implements GamePacket{

    public int playerID;
    public String playerRole;
    public String playerName;

    /**
     * Packet is used by server to send player ID to client
     */
    public PacketPlayerID() {
        this.playerID = 0;
        this.playerRole = "";
        this.playerName = "";
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public String getPlayerRole() {
        return this.playerRole;
    }

    public String getPlayerName() {
        return this.playerName;
    }

}
