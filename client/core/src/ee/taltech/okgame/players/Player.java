package ee.taltech.okgame.players;

public class Player {
    private final int id;
    private final String role;
    private final String playerName;

    public Player(int id, String playerRole, String playerName) {
        this.id = id;
        this.role = playerRole;
        this.playerName = playerName;
    }

    public String getPlayerRole() {
        return this.role;
    }
    public String getPlayerName() {return this.playerName;}

    public int getPlayerId() {
        return this.id;
    }


}

