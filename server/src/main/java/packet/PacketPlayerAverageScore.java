package packet;

public class PacketPlayerAverageScore implements GamePacket{
    private double playerAverageScore;
    private double loserAverageScore;
    public PacketPlayerAverageScore(double playerAverageScore, double loserAverageScore) {
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
