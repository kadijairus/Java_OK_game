package packet;

import java.util.ArrayList;

public class PacketOtherPlayersInfo implements GamePacket{
    public ArrayList<PlayerInfo> players = new ArrayList<>();

    public void addPlayer(int id, String role, String name) {
        players.add(new PlayerInfo(id, role, name));
    }

    public static class PlayerInfo {
        public int id;
        public String role;
        public String name;

        public PlayerInfo() {
        }


        public PlayerInfo(int id, String role, String name) {
            this.id = id;
            this.role = role;
            this.name = name;
        }
    }
}
