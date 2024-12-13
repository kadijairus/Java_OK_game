package ee.taltech.okgame.packet;

import java.io.Serializable;

public class PacketPlayerAverageScore implements GamePacket{
    private double playerAverageScore;
    private double loserAverageScore;
    public PacketPlayerAverageScore() {
        this.playerAverageScore = playerAverageScore;
        this.loserAverageScore = loserAverageScore;
    }
    public double getPlayerAverageScore() {
        return playerAverageScore;
    }

    public double getLoserAverageScore() {
        return loserAverageScore;
    }
}
