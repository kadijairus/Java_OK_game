package ee.taltech.okgame.packet;

public class PacketIsPlayPressed implements GamePacket{

    public boolean playPressed;
    public String playerName;
    public boolean isAiPlayer;
    public boolean isAiDecider;


    public PacketIsPlayPressed() {
    }

    /**
     * Packet is used to inform the server that player wants to start the game (playPressed changed to True) and needs ID and role.
     */
    public PacketIsPlayPressed(String playerName, boolean isAiPlayer, boolean isAiDecider) {
        this.playPressed = true; // Since this constructor is used to start the game
        this.playerName = playerName;
        this.isAiPlayer = isAiPlayer;
        this.isAiDecider = isAiDecider;
    }

}

