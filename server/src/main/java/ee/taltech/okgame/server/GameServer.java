package ee.taltech.okgame.server;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import packet.*;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

public class GameServer {

    private Server server;
    private Map<Integer, Connection> gameClients = new HashMap<>();
    Logger logger = Logger.getLogger(getClass().getName());
    public static final String LEFT_PLAYER = "LeftPlayer";
    public static final String RIGHT_PLAYER = "RightPlayer";
    public static final String DECIDER = "Decider";

    private Map<Integer, PacketPlayerID> playerIDs = new HashMap<>();
    private final Random random = new Random();

    /**
     * Start() initializes server.
     * TCP is bound to 8080 port.
     */
    public GameServer() throws RuntimeException {
        server = new Server();
        LobbyManager.getInstance().initialize(server);
        Kryo kryo = server.getKryo();
        this.registerClasses(kryo);
        try {
            server.start();
            server.bind(8080);
            configureServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private void configureServer() {
        server.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                System.out.println("Connection established with ID: " + connection.getID());
            }
            @Override
            public void received(Connection connection, Object object) {
                LobbyManager.getInstance().handlePacket(connection, object);
            }

            @Override
            public void disconnected(Connection connection) {
                //LobbyManager.getInstance().handleDisconnection(connection);
                logger.info("\nPlayer nr. " + connection.getID() + " has left the building.\n");
                super.disconnected(connection);
                gameClients.remove(connection.getID());

            }
        });
    }

    public void clearServer() {
        gameClients.clear();
    }


    /**
     * Register kryo classes to use specific packet objects.
     * Packet classes are used to have the same data format for both client and server.
     */
    private void registerClasses(Kryo kryo) {
        kryo.register(PacketCardPositionInfo.class);
        kryo.register(PacketCardSlotPosition.class);
        kryo.register(PacketIsPlayPressed.class);
        kryo.register(PacketPlayerID.class);
        kryo.register(HashMap.class);
        kryo.register(Map.class);
        kryo.register(PacketCardPositionMap.class);
        kryo.register(PacketEndTurnPressed.class);
        kryo.register(PacketRoundNumber.class);
        kryo.register(PacketPlayerExitMessage.class);
        kryo.register(PacketExitAcknowledgement.class);
        kryo.register(PacketOtherPlayersInfo.class);
        kryo.register(PacketOtherPlayersInfo.PlayerInfo.class); // Register the inner class
        kryo.register(ArrayList.class);
        kryo.register(PacketPlayerCardsMap.class);
        kryo.register(PacketSlotOccupancy.class);
        kryo.register(PacketRequestSlotOccupancy.class);
        kryo.register(PacketRespondSlotOccupancy.class);
        kryo.register(PacketLeftPlayerWins.class);
        kryo.register(PacketRightPlayerWins.class);
        kryo.register(PacketPlayerAverageScore.class);
        kryo.register(Collections.singletonMap("key", true).getClass());
        kryo.register(PacketCreateLobbyRequest.class);
        kryo.register(PacketJoinLobbyRequest.class);
        kryo.register(PacketLobbyUpdate.class);
        kryo.register(PacketCreateLobbyResponse.class);
        kryo.register(GamePacket.class);
        kryo.register(PacketMoveToLobbyPressed.class);
        kryo.register(PacketSendLobbyList.class);
        kryo.register(PacketGameReady.class);
    }

    /**
     * Enables to run the server in main (initializing GameServer constructor).
     * @param args
     */
    public static void main (String[]args) {
        GameServer gameServer = new GameServer();
    }


}
