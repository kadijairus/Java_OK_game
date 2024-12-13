package ee.taltech.okgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.jcraft.jogg.Packet;
import ee.taltech.okgame.lobby.LobbyManager;
import ee.taltech.okgame.packet.*;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
// import ee.taltech.okgame.players.Player1;


import ee.taltech.okgame.GameStateManager;
import ee.taltech.okgame.players.Player;

public class GameClient {

    private static GameClient instance;
    private Client client;
    public Map<Integer, PacketCardPositionInfo> gameState = new HashMap<>();
    public static Map<Integer, List<Integer>> cardsMap = new HashMap<>();

    public PacketCardPositionMap packetCardPositionMap = new PacketCardPositionMap();
    public PacketPlayerCardsMap packetPlayerCardsMap = new PacketPlayerCardsMap();
    public PacketCardPositionInfo packetCardPositionInfo = new PacketCardPositionInfo();
    public PacketPlayerID packetPlayerId = new PacketPlayerID();
    public PacketPlayerAverageScore packetPlayerAverageScore = new PacketPlayerAverageScore();
    private Player player;
    public double loserAverage;
    public double playerAverage;
    private PlayerIdReceivedListener playerIdReceivedListener;
    private Logger logger = Logger.getLogger(getClass().getName());
    private Music backgroundMusic;

    private final String SERVER_TALTECH = "193.40.255.25";

    public static final int SERVER_PORT = 8080;
    public interface ExitAcknowledgementListener {
        void onExitAcknowledged();
    }
    public interface LeftWinListener {
        void onLeftWin();
    }
    public interface RightWinListener {
        void onRightWin();
    }
    public interface PlayerExitListener {
        void onExitNotified();
    }
    private List<GameEventListener> listeners = new ArrayList<>();

    public void registerListener(GameEventListener listener) {
        listeners.add(listener);
    }

    private ExitAcknowledgementListener exitAcknowledgementListener;
    private RightWinListener rightWinListener;
    private LeftWinListener leftWinListener;
    private PlayerExitListener playerExitListener;

    public void setPlayerExitListener(PlayerExitListener listener) {
        this.playerExitListener = listener;
    }
    public void setLeftWinListener(LeftWinListener listener) {
        this.leftWinListener = listener;
    }
    public void setRightWinListener(RightWinListener listener) {
        this.rightWinListener = listener;
    }
    public void setExitAcknowledgementListener(ExitAcknowledgementListener listener) {
        this.exitAcknowledgementListener = listener;
    }



    // private final String SERVER_OTHER = "173.249.3.131";

    /**
     * New gameclient is created (one player, one client).
     */
    private GameClient() {
        client = new Client();
        Kryo kryo = client.getKryo();
        registerClasses(kryo);
        client.start();


        // Try to connect to localhost first.
        boolean isConnected = false;
        isConnected = connectToLocalhost();
        // If this fails, connect to remote server.
        if (!isConnected) {
            logger.warning("Failed to connect to localhost");
            connectToServer(SERVER_TALTECH, SERVER_PORT);
        }
        // backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("mountain-forest.mp3"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("a_team.mp3"));
        // Set loop to true for continuous playback
        backgroundMusic.setLooping(true);

        /**
         * Client listens to messages from server.
         */
        client.addListener(new Listener() {

            /**
             * Specific reactions for messages of specific class.
             */
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof PacketCardPositionMap) {
                    packetCardPositionMap = (PacketCardPositionMap) object;
                    gameState = packetCardPositionMap.getCardPositionInfoMap();
                    logger.info("\n--- Server says, that the gameState is: " + gameState + "\n");
                    GameStateManager.getInstance().updateGameState(gameState);
                }
                if (object instanceof PacketPlayerID) {
                    PacketPlayerID packet = (PacketPlayerID) object;
                    // This packet contains information about the player themselves.
                    PlayerManager.getInstance().addPlayer(packet.playerID, packet.playerRole, packet.playerName);
                    PlayerManager.getInstance().setSelfId(packet.playerID); // Mark as self
                    logger.info("\n--- Server says, my ID is " + packet.playerID + ", role " + packet.playerRole + ", and name " + packet.playerName + ".");
                    if (playerIdReceivedListener != null) {
                        playerIdReceivedListener.onPlayerIdReceived(packet.playerID, packet.playerRole, packet.playerName);
                    }
                } else if (object instanceof PacketOtherPlayersInfo) {
                    PacketOtherPlayersInfo packet = (PacketOtherPlayersInfo) object;
                    logger.info("Received PacketOtherPlayersInfo with players: " + packet.players.size());
                    // This packet contains information about other players.
                    for (PacketOtherPlayersInfo.PlayerInfo playerInfo : packet.players) {
                        PlayerManager.getInstance().addPlayer(playerInfo.id, playerInfo.role, playerInfo.name);
                        GameStateManager.getInstance().playerUpdated();
                        logger.info("Updating player: " + playerInfo.name + " with role: " + playerInfo.role);
                    }
                }

                if (object instanceof PacketCardPositionInfo) {
                    // Not used in the current state of the game.
                    packetCardPositionInfo = (PacketCardPositionInfo) object;
                }
                if (object instanceof PacketRoundNumber) {
                    PacketRoundNumber roundUpdate = (PacketRoundNumber) object;
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            GameStateManager gameStateManager = GameStateManager.getInstance();
                            gameStateManager.setCurrentRound(roundUpdate.roundNumber);
                            int roundNumber = roundUpdate.roundNumber;
                            logger.info("\n--- Server says, the round has changed to " + roundNumber + ". Updating GameStateManager.\n");
                            if (roundNumber == 2) {
                            }
                            if (roundNumber == 3) {
                            }
                            if (roundNumber >= 4) {
                            }
                        }
                    });
                }
                if (object instanceof PacketPlayerExitMessage) {
                    // TODO is not recognised as PacketPlayerExitMessage!
                    PacketPlayerExitMessage exitMessage = (PacketPlayerExitMessage)object;
                    Gdx.app.postRunnable(() -> {
                        if (playerExitListener != null) {
                            playerExitListener.onExitNotified();
                        }
                    });
                    logger.info("Client " + connection.getID() + " received notice that player " + exitMessage.getCurrentPlayerId() + " has left the game.");
                    logger.severe("\n--- CLOSING THE GAME!");
                    client.close();
                }
                if (object instanceof PacketExitAcknowledgement) {
                    Gdx.app.postRunnable(() -> {
                        if (exitAcknowledgementListener != null) {
                            exitAcknowledgementListener.onExitAcknowledged();
                        }
                    });
                }
                if (object instanceof PacketPlayerCardsMap) {
                    logger.info("\n--- Server gave me my cards\n");
                    packetPlayerCardsMap = (PacketPlayerCardsMap) object;
                    cardsMap = packetPlayerCardsMap.getPlayerCardsMap();
                } else {
                    logger.info("??? Debug! I received an Unknown Forwarded Object: " + object + " from Player " + connection.getID());
                }
                if (object instanceof PacketPlayerAverageScore) {
                    packetPlayerAverageScore = (PacketPlayerAverageScore) object;
                    loserAverage = packetPlayerAverageScore.getLoserAverageScore();
                    playerAverage = packetPlayerAverageScore.getPlayerAverageScore();
                    System.out.println("THIS IS THE LOSER AVERAGE: " + loserAverage);
                    System.out.println("THIS IS THE WINNER AVERAGE: " + playerAverage);
                }
                if (object instanceof PacketLeftPlayerWins) {
                    Gdx.app.postRunnable(() -> {
                        if (leftWinListener != null) {
                            leftWinListener.onLeftWin();
                        }
                    });
                }
                if (object instanceof PacketRightPlayerWins) {
                    Gdx.app.postRunnable(() -> {
                        if (rightWinListener != null) {
                            rightWinListener.onRightWin();
                        }
                    });
                }
                if (object instanceof PacketSlotOccupancy) {
                    PacketSlotOccupancy packet = (PacketSlotOccupancy) object; // Cast explicitly
                    // Update each slot status provided in the packet
                    for (Map.Entry<String, Boolean> entry : packet.slotOccupancy.entrySet()) {
                        String slotName = entry.getKey();
                        Boolean isOccupied = entry.getValue();
                        GameStateManager.getInstance().setSlotOccupancy(slotName, isOccupied);
                    }
                }
                if (object instanceof PacketLobbyUpdate) {
                    System.out.println("LobbyUpdate received.");
                    PacketLobbyUpdate packet = (PacketLobbyUpdate) object;
                    LobbyManager.getInstance().updateLobby(packet.lobbyName, packet.playerNames);
                }
                if (object instanceof PacketCreateLobbyResponse) {
                    System.out.println("Lobby loodud.");

                }
                if (object instanceof PacketGameReady) {
                    listeners.forEach(GameEventListener::onGameReady);
                } else if (object instanceof PacketPlayerID) {
                    PacketPlayerID pid = (PacketPlayerID) object;
                    listeners.forEach(l -> l.onPlayerIdReceived(pid.getPlayerID(), pid.getPlayerRole(), pid.getPlayerName()));


                }



            }
        });
    }

    private boolean connectToServer(String host, int port) {
        try {
            client.connect(5000, host, port);
            logger.info("Hooray! I am connected to remote server: " + host + ":" + port);
            // Do whatever you need with the socket (e.g., send/receive data)
            return true;
        } catch (IOException e) {
            logger.severe("Damn, failed to connect to remote server: " + host + ":" + port);
            return false;
        }
    }

    private boolean connectToLocalhost() {
        try {
            client.connect(5000, "localhost", 8080);
            logger.info("I am connected to localhost");
            return true;
        } catch (IOException e) {
            logger.severe("Damn, failed to connect to localhost...");
            return false;
        }
    }


    /**
     * GameClient instance getter.
     * @return
     */
    public static synchronized GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
        }
        return instance;
    }

    /**
     * Method  is called from GameClient to register kryo classes
     * @param kryo
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
        kryo.register(java.util.Collections.singletonMap("key", true).getClass());
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
     * Message sent to server via TCP.
     * @param object
     */
    public void sendTCP(Object object) {
        client.sendTCP(object);
    }

    /**
     * Listening to when ID is sent from server.
     * @param listener
     */
    public void setPlayerIdReceivedListener(PlayerIdReceivedListener listener) {
        logger.info("Created playerID listener");
        this.playerIdReceivedListener = listener;
    }

    // Method to request the occupancy status of a slot
    public void requestSlotOccupancy(String slotName, Consumer<Boolean> callback) {
        client.sendTCP(new PacketRequestSlotOccupancy(slotName));
        // Assuming there's a mechanism to handle the response and execute the callback with the result
        // This might involve storing the callback in a Map against a request ID or slot name, then calling it when the response is received
    }


    // Method to handle occupancy response, this should be called within the received method of your client listener
    public void handleSlotOccupancyResponse(PacketRespondSlotOccupancy packet) {
        // Assuming you have a method or callback to process the response
        // Update your client-side logic accordingly, e.g., enabling/disabling UI elements based on occupancy
    }
    public void playBackgroundMusic() {
        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }
    public void stopBackgroundMusic() {
        backgroundMusic.stop();
    }

    public void dispose() {
        backgroundMusic.dispose();
    }

    /**
     * Ending client object.
     */
    public void close() {
        client.stop();
    }
}
