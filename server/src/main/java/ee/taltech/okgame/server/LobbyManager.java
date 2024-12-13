package ee.taltech.okgame.server;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Preferences;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import packet.*;

public class LobbyManager {

    private Map<String, GameSession> lobbies = new ConcurrentHashMap<>();
    private Map<Integer, String> connectionToLobby = new ConcurrentHashMap<>(); // Maps connection IDs to lobby names

    private Map<Integer, String> connectionToName = new ConcurrentHashMap<>();
    private static LobbyManager instance;
    private Server server;


    private LobbyManager() {
    }

    // Public method to get the instance
    public static synchronized LobbyManager getInstance() {
        if (instance == null) {
            instance = new LobbyManager();
        }
        return instance;
    }

    public synchronized void initialize(Server server) {
        if (this.server == null) {
            this.server = server;
            server.addListener(new Listener() {
                @Override
                public void disconnected(Connection connection) {
                    handleDisconnection(connection);
                }
            });
        } else {
            throw new IllegalStateException("LobbyManager has already been initialized with a server.");
        }
    }

    public void createLobby(String lobbyName, boolean aiPlayer, boolean aiDecider, int creatorId) {
        if (!lobbies.containsKey(lobbyName)) {
            GameSession gameSession = new GameSession(lobbyName, aiPlayer, aiDecider, creatorId, server);
            lobbies.put(lobbyName, gameSession);
            connectionToLobby.put(creatorId, lobbyName);
            server.sendToTCP(creatorId, new PacketCreateLobbyResponse());
        } else {
            server.sendToTCP(creatorId, new PacketCreateLobbyResponse());
        }
    }

    public boolean addPlayerToLobby(String lobbyName, int playerId, Connection connection) {
        GameSession gameSession = lobbies.get(lobbyName);
        if (gameSession != null && !connectionToLobby.containsKey(playerId)) {
            String playerName = connectionToName.getOrDefault(playerId, "Nimetu");
            connectionToLobby.put(playerId, lobbyName);
            gameSession.addPlayer(connection, playerName);
            broadcastLobbyUpdate(lobbyName);  // Call this method to update all players in the lobby
            return true;
        }
        return false;
    }


    public List<Integer> getClientsInLobby(String name) {
        GameSession gameSession = lobbies.get(name);
        if (gameSession != null) {
            return gameSession.getClients();
        }
        return new ArrayList<>();
    }

    public GameSession getLobby(int lobbyID) {
        return lobbies.get(lobbyID);
    }

    public void removeLobby(String lobbyName) {
        GameSession session = lobbies.remove(lobbyName);
        if (session != null) {
            for (Integer connectionId : connectionToLobby.keySet()) {
                if (connectionToLobby.get(connectionId).equals(lobbyName)) {
                    connectionToLobby.remove(connectionId);
                }
            }
        }
    }

    public Collection<GameSession> getAllLobbies() {
        return lobbies.values();
    }
    // Handling packets and forwarding packets to sessions for handling

    public void handlePacket(Connection connection, Object packet) {
        if (packet instanceof PacketMoveToLobbyPressed) {
            PacketMoveToLobbyPressed movePacket = (PacketMoveToLobbyPressed) packet;
            connectionToName.put(connection.getID(), movePacket.getPlayerName());  // Update the map with the player's name
            System.out.println("Mängija " + movePacket.getPlayerName() + " liitus ooteruumiga.");
            sendLobbyListToPlayer(connection);
        }

        if (packet instanceof PacketIsPlayPressed) {
            // put to some list with id and player name. At this point just moved to the other screen in the game.
            // Consider not using this packet name when moving to lobby as name is a bit confusing.
        }
        if (packet instanceof PacketCreateLobbyRequest) {
            // Handle lobby creation, joining, or updating
            handleCreateLobbyRequest(connection, (PacketCreateLobbyRequest) packet);
        }
        if (packet instanceof PacketJoinLobbyRequest) {
            // Handle lobby creation, joining, or updating
            handleJoinLobbyRequest(connection, (PacketJoinLobbyRequest) packet);
        }
        if (packet instanceof PacketCardPositionInfo ) {
            routeToGameSession(connection, (PacketCardPositionInfo) packet);
        }
        if (packet instanceof PacketEndTurnPressed) {
            routeToGameSession(connection, (PacketEndTurnPressed) packet);
        }
        if (packet instanceof PacketPlayerExitMessage) {
            routeToGameSession(connection, (PacketPlayerExitMessage) packet);
            // a method also to be added to the lobby so that the lobby is freed up again for joining.
        }
        if (packet instanceof PacketSlotOccupancy) {
            routeToGameSession(connection, (PacketSlotOccupancy) packet);
        }
        if (packet instanceof PacketRequestSlotOccupancy) {
            routeToGameSession(connection, (PacketRequestSlotOccupancy) packet);
        }
        if (packet instanceof PacketRightPlayerWins) {
            routeToGameSession(connection, (PacketRightPlayerWins) packet);
        }
        if (packet instanceof PacketLeftPlayerWins) {
            routeToGameSession(connection, (PacketLeftPlayerWins) packet);
        }
        if (packet instanceof PacketCardSlotPosition) {
            routeToGameSession(connection, (PacketCardSlotPosition) packet);
        }

    }

    private void routeToGameSession(Connection connection, GamePacket packet) {
        String lobbyName = connectionToLobby.get(connection.getID());
        if (lobbyName != null) {
            GameSession session = lobbies.get(lobbyName);
            if (session != null) {
                session.handleGamePacket(connection, packet);
            }
        }
    }


    private void handleJoinLobbyRequest(Connection connection, PacketJoinLobbyRequest packet) {
        String lobbyName = packet.lobbyName;
        if (lobbies.containsKey(lobbyName)) {
            boolean success = addPlayerToLobby(lobbyName, connection.getID(), connection);
            if (success) {
                // Broadcast lobby update to all players in the lobby
                broadcastLobbyUpdate(lobbyName);
            } else {
                // Player could not be added
                server.sendToTCP(connection.getID(), new PacketCreateLobbyResponse());
            }
        } else {
            // Lobby does not exist
            server.sendToTCP(connection.getID(), new PacketCreateLobbyResponse());
        }
    }

    private void handleCreateLobbyRequest(Connection connection, PacketCreateLobbyRequest packet) {
        System.out.println("Received create lobby request from " + connection.getID());
        String lobbyName = packet.lobbyName;
        boolean isAiPlayer = packet.isAiPlayer;
        boolean isAiDecider = packet.isAiDecider;
        System.out.println("Attempting to create lobby: " + lobbyName);

        if (!lobbies.containsKey(lobbyName)) {
            GameSession gameSession = new GameSession(lobbyName, isAiPlayer, isAiDecider, connection.getID(), server);
            lobbies.put(lobbyName, gameSession);
            boolean success = addPlayerToLobby(lobbyName, connection.getID(), connection);
            System.out.println("Lobby created successfully: " + lobbyName + "id-ga" + gameSession.getSessionID());
            server.sendToTCP(connection.getID(), new PacketCreateLobbyResponse());
            System.out.println("Initial player added to newly created lobby: " + lobbyName);


            // Broadcast the initial lobby state
            broadcastLobbyUpdate(lobbyName);
        } else {
            // Lobby already exists; handle this scenario, possibly denying the creation
            server.sendToTCP(connection.getID(), new PacketCreateLobbyResponse());
        }
    }

    private void broadcastLobbyUpdate(String lobbyName) {
        GameSession gameSession = lobbies.get(lobbyName);
        if (gameSession != null) {
            List<String> playerNames = gameSession.getPlayerNames();
            PacketLobbyUpdate lobbyUpdate = new PacketLobbyUpdate();
            lobbyUpdate.lobbyName = lobbyName;  // Use lobby name instead of ID
            lobbyUpdate.playerNames = playerNames;
            lobbyUpdate.isGameStarting = false;

            Connection[] connections = server.getConnections();
            for (Connection connection : connections) {
                server.sendToTCP(connection.getID(), lobbyUpdate);
            }
        }
    }
    private void sendLobbyListToPlayer(Connection connection) {
        PacketSendLobbyList packet = new PacketSendLobbyList();
        for (Map.Entry<String, GameSession> entry : lobbies.entrySet()) {
            String lobbyName = entry.getKey();
            GameSession session = entry.getValue();
            packet.lobbyNames.add(lobbyName);
            packet.playerCounts.put(lobbyName, session.getPlayerNames().size());
        }

        server.sendToTCP(connection.getID(), packet);
    }


    /**
     * If disconnected, remove client.
     */

    /**
     * If disconnected, remove client.
     */
    public void handleDisconnection(Connection connection) {
        String lobbyName = connectionToLobby.get(connection.getID());
        if (lobbyName != null) {
            GameSession session = lobbies.get(lobbyName);
            if (session != null) {
                session.removePlayer(connection);
            }
        }
        connectionToLobby.remove(connection.getID());
        connectionToName.remove(connection.getID());
    }





}
