package ee.taltech.okgame.server;

import com.esotericsoftware.kryonet.Connection;

import com.esotericsoftware.kryonet.Server;
import ee.taltech.okgame.server.ai.AI;
import ee.taltech.okgame.server.ai.PlayerAI;
import ee.taltech.okgame.server.ai.DeciderAI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import packet.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameSession {
    private final boolean isAiPlayer;
    private final boolean isAiDecider;

    private int sessionID;

    private boolean isActive;
    private boolean hasStarted;

    private Map<Integer, Connection> gameClients;
    private Map<Integer, PacketCardPositionInfo> gameState = new HashMap<>();
    Logger logger = Logger.getLogger(getClass().getName());
    public static final String LEFT_PLAYER = "LeftPlayer";
    public static final String RIGHT_PLAYER = "RightPlayer";
    public static final String DECIDER = "Decider";
    private static final String QUEUE_PLAYER = "QueuePlayer"; // currently not in use
    private List<String> availableRoles = new ArrayList<>(Arrays.asList(LEFT_PLAYER, RIGHT_PLAYER, DECIDER));
    private int playerCount = 0;
    private int currentRound = 1;
    private Set<Integer> playersEndedTurn = new HashSet<>();
    private int totalTeamPlayers = 2;
    private Map<Integer, PacketPlayerID> playerIDs = new HashMap<>();
    private final Random random = new Random();
    private static final int MIN_CARD_ID_POS = 101;
    private static final int MAX_CARD_ID_POS = 140;
    private static final int MIN_CARD_ID_NEG = 201;
    private static final int MAX_CARD_ID_NEG = 240;
    private static final int MIN_CARD_ID_MOD = 301;
    private static final int MAX_CARD_ID_MOD = 340;
    private static final int NUM_CARDS_PER_PLAYER = 3;
    private Map<Integer, List<Integer>> playerPositiveCardsMap = new HashMap<>();
    private Map<Integer, List<Integer>> playerModifierCardsMap = new HashMap<>();
    private Map<Integer, List<Integer>> playerNegativeCardsMap = new HashMap<>();
    private Map<Integer, String> connectionToName = new ConcurrentHashMap<>();
    private Map<String, Boolean> slotOccupancy = new HashMap<>();
    private Map <Integer, String> cardSlotPositionMap = new HashMap<>();
    private List<AI> playersAI = new ArrayList<>();
    private boolean hasPlayerAI = false;
    private boolean hasDeciderAI = false;
    private Server server;
    private String name;

    private double winnerScore;
    private double loserScore;

    public GameSession(String name, boolean aiPlayer, boolean aiDecider, int creatorId, Server server) {
        this.gameClients = new HashMap<>();
        this.isAiPlayer = aiPlayer;
        this.isAiDecider = aiDecider;
        this.isActive = true;
        this.hasStarted = false;
        this.server = server;
        this.name = name;
        setSessionIDFromName(name);
    }

    public List<Integer> getClients() {
        List<Integer> clientIds = new ArrayList<>();
        for (Connection conn : gameClients.values()) {
            clientIds.add(conn.getID());
        }
        return clientIds;
    }

    public void addPlayer(Connection connection, String playerName) {
        if (gameClients.size() >= 3) {
            throw new IllegalStateException("Lobby is full.");
        }
        gameClients.put(connection.getID(), connection);
        connectionToName.put(connection.getID(), playerName);
        System.out.println("Player" + playerName + "has been added to lobby" + name);
        checkStartGame();
    }

    private void checkStartGame() {
        int requiredPlayers = 3;  // Default number of players needed

        // Adjust the number of required players based on AI selections
        if (isAiPlayer) {
            requiredPlayers--;  // One less human needed if there's an AI player
        }
        if (isAiDecider) {
            requiredPlayers--;  // One less human needed if there's an AI decider
        }

        // Check if the current number of human players meets the required number to start the game
        if (gameClients.size() >= requiredPlayers && !hasStarted) {
            startGame();
        }
    }

    private void startGame() {
        hasStarted = true;
        isActive = true;
        addAIPlayersToGame();
        assignRolesToAllPlayers();
        distributeCards();
        sendPlayerCardsMap(playerPositiveCardsMap);
        broadcastGameReady();
        System.out.println("Game has started");
    }

    private void broadcastGameReady() {
        PacketGameReady packet = new PacketGameReady();
        for (Connection conn : gameClients.values()) {
            conn.sendTCP(packet);
        }
    }

    public void removePlayer(Connection connection) {
        gameClients.remove(connection.getID());
        if (gameClients.isEmpty()) {
            clearGameData();
            endSession();
        }
    }

    public void endSession() {
        isActive = false;
        notifyLobbyManagerSessionEnded(); // Notify LobbyManager
    }

    public void notifyLobbyManagerSessionEnded() {
        LobbyManager.getInstance().removeLobby(name); // Remove lobby by name
    }

    public int getSessionID() {
        return sessionID;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean hasStarted() {return hasStarted;}

    public void handleGamePacket(Connection connection, Object object) {
        if (object instanceof PacketIsPlayPressed packet) {
            // put to some list with id and player name. At this point just moved to the other screen in the game.
            // Consider not using this packet name when moving to lobby as name is a bit confusing.
            logger.info("*** Player count is: " + playerCount);
            if (playerCount >= 3) {
                server.sendToTCP(connection.getID(), "NO MORE THAN 3 PLAYERS ALLOWED!");
                logger.info("No more than 3 players allowed");
            }
        }

        if (object instanceof PacketCardPositionInfo packet) {
            gameState.put(packet.cardID, packet);
            sendToAllInSession(packet);
            sendGameState();
            sendGameStateAI();
            updateAI();

            for (AI someAI : playersAI) {
                if (!someAI.getRole().equals(DECIDER)) {
                    PacketCardPositionInfo packetFromAi = someAI.getCardAndSlotToPlay();
                    gameState.put(packetFromAi.cardID, packetFromAi);
                    // Send new game state with AI-s card
                    sendGameState();
                    // Decider needs to know
                    sendGameStateAI();
                }
            }
        }
        if (object instanceof PacketEndTurnPressed) {
            int playerId = connection.getID();
            playersEndedTurn.add(playerId);
            if (playersEndedTurn.size() == totalTeamPlayers) {
                // All players have ended their turn
                advanceRound(); // Advance the round and send updates
            }
        }
        if (object  instanceof PacketPlayerExitMessage) {
            gameClients.remove(connection.getID());

            // Acknowledge the exit to the exiting client
            connection.sendTCP(new PacketExitAcknowledgement());
            // Inform other clients about the player exit
            for (Connection conn : gameClients.values()) {
                // PacketPlayerExitMessage exitMessage = new PacketPlayerExitMessage(connection.getID());
                logger.info("\n*** Sending exit message to player " + conn.getID());
                if (conn != connection) { // Don't send to the exiting player
                    conn.sendTCP(new PacketPlayerExitMessage(conn.getID()));
                }
            }
            if (gameClients.isEmpty()) {
                clearGameData();
                endSession(); // End session and notify LobbyManager
            }
            // a method also to be added to the lobby so that the lobby is freed up again for joining.
        }
        if (object  instanceof PacketSlotOccupancy packet) {
            updateServerSlotMap(packet.slotOccupancy, connection.getID());
        }
        if (object  instanceof PacketRequestSlotOccupancy packet) {
            // Respond to slot occupancy request
            boolean isOccupied = checkSlotOccupancy(packet.slotName);
            PacketRespondSlotOccupancy response = new PacketRespondSlotOccupancy(packet.slotName, isOccupied);
            connection.sendTCP(response); // Respond only to the requester
        }
        if (object instanceof PacketRightPlayerWins packet) {
            playerToJson(packet.playerName, +5, +1);
            playerToJson(packet.loserName, +1, +1);
            winnerScore = calculateAverageGrade(packet.playerName);
            loserScore = calculateAverageGrade(packet.loserName);
            sendPlayerAverageScore(winnerScore, loserScore);
            System.out.println("THIS IS THE AVERAGE FOR " + packet.playerName + calculateAverageGrade(packet.playerName));
            System.out.print("THIS IS THE AVERAGE FOR LOSER: " + packet.loserName + calculateAverageGrade(packet.loserName));
            sendToAllInSession(packet);
        }
        if (object instanceof PacketLeftPlayerWins packet) {
            playerToJson(packet.playerName, +5, +1);
            playerToJson(packet.loserName, +1, +1);
            winnerScore = calculateAverageGrade(packet.playerName);
            loserScore = calculateAverageGrade(packet.loserName);
            sendPlayerAverageScore(winnerScore, loserScore);
            System.out.println("THIS IS THE AVERAGE FOR " + packet.playerName + calculateAverageGrade(packet.playerName));
            System.out.print("THIS IS THE AVERAGE FOR LOSER: " + packet.loserName + calculateAverageGrade(packet.loserName));
            sendToAllInSession(packet);
        }
        if (object instanceof PacketCardSlotPosition packet) {
            updateCardSlotPositions(packet.cardSlot);
        }


    }
    private void setSessionIDFromName(String name) {
        // Extract the numeric part from the lobby name.
        Matcher matcher = Pattern.compile("\\d+$").matcher(name);
        if (matcher.find()) {
            this.sessionID = Integer.parseInt(matcher.group());
        } else {
            logger.warning("Failed to extract session ID from lobby name: " + name);
            this.sessionID = -1;  // Default or error value if no ID found
        }
    }

    public static void playerToJson(String playerName, double fullScore, int gameCounter) {
        try {
            String filePath = "server" + File.separator + "json" + File.separator + "PlayersJson.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray jsonArray = new JSONArray(jsonContent);

            boolean playerExists = false;
            double currentScore;
            int currentGameCount;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject playerObj = jsonArray.getJSONObject(i);
                if (playerObj.getString("name").equals(playerName)) {
                    playerExists = true;
                    currentScore = playerObj.getDouble("fullScore");
                    currentGameCount = playerObj.getInt("gameCounter");
                    playerObj.put("fullScore", currentScore + fullScore);
                    playerObj.put("gameCounter", currentGameCount + gameCounter);
                    break;
                }
            }

            if (!playerExists) {
                JSONObject newPlayer = new JSONObject();
                newPlayer.put("name", playerName);
                newPlayer.put("fullScore", fullScore);
                newPlayer.put("gameCounter", gameCounter);
                jsonArray.put(newPlayer);

                FileWriter fileWriter = new FileWriter(filePath);
                fileWriter.write(jsonArray.toString(4));
                fileWriter.close();
            } else {
                FileWriter fileWriter = new FileWriter(filePath);
                fileWriter.write(jsonArray.toString(4));
                fileWriter.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    // The following will be methods from GameServer which are single game based and with lobby belong in GameSession.

    private void createAI(String role) {
        AI newAI = role.equals(DECIDER) ? new DeciderAI() : new PlayerAI();
        newAI.setRole(role);
        playersAI.add(newAI);
        logger.info("AI created with role: " + role);
    }

    /**
     * Make each AI move one step in tree
     */
    public void updateAI() {
        for (AI someAI : playersAI) {
            someAI.update();
        }
    }

    /**
     * Send new gamestate to each AI
     */
    public void sendGameStateAI() {
        for (AI someAI : playersAI) {
            someAI.getAiBlackboard().setGameState(this.gameState);
        }
    }

    /**
     *
     *
     */
    public void addAIPlayersToGame() throws RuntimeException {
        try {
            if (isAiPlayer) {
                hasPlayerAI = true;
                totalTeamPlayers--;
                String roleAI = LEFT_PLAYER; // Assuming LEFT_PLAYER for AI for simplicity
                availableRoles.remove(roleAI); // Remove the AI role from available roles
                String nameAI = "Airi Tehisaru";
                int idAI = -2;
                PacketPlayerID playerAiIdPacket = createPlayerIDPacket(idAI, roleAI, nameAI);
                playerIDs.put(idAI, playerAiIdPacket);
                broadcastNewPlayerInfo(idAI, roleAI, nameAI);
                createAI(roleAI);
            }
            if (isAiDecider) {
                hasDeciderAI = true;
                String roleAI = DECIDER;
                availableRoles.remove(roleAI); // Remove the DECIDER role from available roles
                String nameAI = "Aive Tehisaru";
                int idAI = -3;
                PacketPlayerID playerAiIdPacket = createPlayerIDPacket(idAI, roleAI, nameAI);
                playerIDs.put(idAI, playerAiIdPacket);
                broadcastNewPlayerInfo(idAI, roleAI, nameAI);
                createAI(roleAI);
            }
        } catch (Exception e) {
            throw new RuntimeException("PlayerAI was not added to game: " + e);
        }
    }

    private void assignRolesToAllPlayers() {
        for (Map.Entry<Integer, Connection> entry : gameClients.entrySet()) {
            String playerName = connectionToName.get(entry.getKey());
            assignPlayerRoles(entry.getValue(), playerName);
        }
    }

    private void assignPlayerRoles(Connection connection, String playerName) {
        if (availableRoles.isEmpty()) {
            logger.severe("No available roles to assign!");
            return;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(availableRoles.size());
        String playerRole = availableRoles.remove(randomIndex); // Assign and remove the role

        PacketPlayerID playerIdPacket = createPlayerIDPacket(connection.getID(), playerRole, playerName);
        playerIDs.put(connection.getID(), playerIdPacket);

        informPlayerAboutSelf(playerIdPacket);  // Notify player of their role
        broadcastNewPlayerInfo(connection.getID(), playerRole, playerName);  // Notify others
    }

    private void distributeCards() {
        // Iterate over all players to distribute cards according to their roles
        for (Map.Entry<Integer, PacketPlayerID> entry : playerIDs.entrySet()) {
            int playerId = entry.getKey();
            String playerRole = entry.getValue().getPlayerRole();
            boolean notDecider = !playerRole.equals(DECIDER);

            // Check if it's not the decider to distribute the appropriate cards
            if (notDecider) {
                if (currentRound == 1 || currentRound == 2 ) {
                    distributePosCardsToPlayers(playerId);
                } else if (currentRound == 3) {
                    distributeNegCardsToPlayers(playerId);
                } else if (currentRound == 4 || currentRound == 5) {
                    distributeModifierCardsToPlayers(playerId);
                }
            }
        }

        // Log distributed cards for debugging
        logger.info("\n*** Player cards distributed for round " + currentRound);
    }

    private void distributePosCardsToPlayers(int playerId) {
        logger.info("Starting to distribute positive cards to player " + playerId);

        List<Integer> playerCards = new ArrayList<>();
        List<Integer> availableCardIds = getListOfAvailableCards(MIN_CARD_ID_POS, MAX_CARD_ID_POS, playerPositiveCardsMap);

        // Log the available card IDs before distribution
        logger.info("Available positive cards for player " + playerId + " before distribution: " + availableCardIds);

        for (int i = 0; i < NUM_CARDS_PER_PLAYER; i++) {
            if (!availableCardIds.isEmpty()) {
                int randomIndex = random.nextInt(availableCardIds.size());
                int cardId = availableCardIds.remove(randomIndex);
                playerCards.add(cardId);
            } else {
                logger.warning("No more available positive cards to distribute.");
                break;
            }
        }

        playerPositiveCardsMap.put(playerId, playerCards);

        // Log the cards that the player received
        logger.info("Player " + playerId + " received positive cards: " + playerCards);

        // Check the map state after distribution to this player
        logger.info("Current state of playerPositiveCardsMap: " + playerPositiveCardsMap);
    }


    private void distributeNegCardsToPlayers(int playerId) {
        List<Integer> playerCards = new ArrayList<>();
        List<Integer> availableCardIds = getListOfAvailableCards(MIN_CARD_ID_NEG, MAX_CARD_ID_NEG, playerNegativeCardsMap);

        for (int i = 0; i < NUM_CARDS_PER_PLAYER; i++) {
            int randomIndex = random.nextInt(availableCardIds.size());
            int cardId = availableCardIds.remove(randomIndex);
            playerCards.add(cardId);
        }
        playerNegativeCardsMap.put(playerId, playerCards);
    }

    private void distributeModifierCardsToPlayers(int playerId) {
        List<Integer> playerCards = new ArrayList<>();
        List<Integer> availableCardIds = getListOfAvailableCards(MIN_CARD_ID_MOD, MAX_CARD_ID_MOD, playerModifierCardsMap);

        for (int i = 0; i < NUM_CARDS_PER_PLAYER; i++) {
            int randomIndex = random.nextInt(availableCardIds.size());
            int cardId = availableCardIds.remove(randomIndex);
            playerCards.add(cardId);
        }
        playerModifierCardsMap.put(playerId, playerCards);
    }


    public List<Integer> getListOfAvailableCards(int minId, int maxId, Map<Integer, List<Integer>> mapOfCardsGivenToPlayers) {
        List<Integer> availableCardIds = new ArrayList<>();
        List<Integer> cardsGivenToPlayers = mapOfCardsGivenToPlayers.values().stream()
                .flatMap(List::stream)
                .toList();
        for (int i = minId; i <= maxId; i++) {
            if (!cardsGivenToPlayers.contains(i)) {
                availableCardIds.add(i);
            }
        }
        return availableCardIds;
    }

    private PacketPlayerID createPlayerIDPacket(int playerID, String playerRole, String playerName) {
        logger.info("\n*** Player " + playerName + " has been assigned a role of " + playerRole + "\n");
        PacketPlayerID playerIdPacket = new PacketPlayerID();
        playerIdPacket.playerID = playerID;
        playerIdPacket.playerRole = playerRole;
        playerIdPacket.playerName = playerName;
        return playerIdPacket;
    }

    private void advanceRound() {
        currentRound++;
        for (AI ai : this.playersAI) {
            ai.getAiBlackboard().setCurrentRound(this.currentRound);
        }
        if (currentRound == 2) {
            distributeCards();
        } else if (currentRound == 3) {
            distributeCards();
        } else if (currentRound == 4 || currentRound == 5) {
            distributeCards();
        }
        PacketRoundNumber packet = new PacketRoundNumber();
        packet.roundNumber = currentRound;
        if(currentRound == 2) {
            sendPlayerCardsMap(playerPositiveCardsMap);
        } else if (currentRound == 3) {
            sendPlayerCardsMap(playerNegativeCardsMap);
        } else if (currentRound == 4 || currentRound == 5) {
            sendPlayerCardsMap(playerModifierCardsMap);
        }

        // Send the round update packet to all clients, including the GameStateManager if applicable
        sendToAllInSession(packet);

        if (currentRound == 6) {
            String winnerRole = null;
            for (AI someAi : playersAI) {
                if (someAi.getRole().equals(DECIDER)) {
                    someAi.update();
                    winnerRole = someAi.getAiBlackboard().getWinner();
                }
            }
            createAndSendAiDecidersWinnerPacket(winnerRole);
        }

        // Reset the turn end tracking for the next round
        playersEndedTurn.clear();
        // printMapContents(cardSlotPositionMap);

    }

    private String determineWinner() {
        for (AI ai : playersAI) {
            if (ai.getRole().equals(DECIDER)) {
                return ai.getAiBlackboard().getWinner();
            }
        }
        return null;
    }

    private void createAndSendAiDecidersWinnerPacket(String winnerRole) {
        String leftPlayerName = null;
        String rightPlayerName = null;
        if (winnerRole != null) {
            for (Map.Entry<Integer, PacketPlayerID> entry : playerIDs.entrySet()) {
                PacketPlayerID entryValue = entry.getValue();
                if (entryValue.getPlayerRole().equals(LEFT_PLAYER)) {
                    leftPlayerName = entryValue.getPlayerName();
                }
                if (entryValue.getPlayerRole().equals(RIGHT_PLAYER)) {
                    rightPlayerName = entryValue.getPlayerName();
                }
            }

            if (winnerRole.equals(LEFT_PLAYER)) {
                PacketLeftPlayerWins packet = new PacketLeftPlayerWins();
                packet.leftPlayerWins = true;
                packet.playerName = leftPlayerName != null ? leftPlayerName : "vasakul";
                packet.loserName = rightPlayerName != null ? rightPlayerName : "paremal";
                playerToJson(leftPlayerName, +5, +1);
                playerToJson(rightPlayerName, +1, +1);
                winnerScore = calculateAverageGrade(leftPlayerName);
                loserScore = calculateAverageGrade(rightPlayerName);
                sendPlayerAverageScore(winnerScore, loserScore);
                sendToAllInSession(packet);
            }
            if (winnerRole.equals(RIGHT_PLAYER)) {
                PacketRightPlayerWins packet = new PacketRightPlayerWins();
                packet.rightPlayerWins = true;
                packet.playerName = rightPlayerName;
                packet.loserName = leftPlayerName;
                playerToJson(rightPlayerName, +5, +1);
                playerToJson(leftPlayerName, +1, +1);
                winnerScore = calculateAverageGrade(rightPlayerName);
                loserScore = calculateAverageGrade(leftPlayerName);
                sendPlayerAverageScore(winnerScore, loserScore);
                sendToAllInSession(packet);
            }
        }
    }

    public void sendPlayerAverageScore(double playerAverageScore, double loserAverageScore) {
        PacketPlayerAverageScore packet = new PacketPlayerAverageScore(playerAverageScore, loserAverageScore);
        sendToAllInSession(packet);
    }

    private double calculateAverageGrade(String playerName) {
        try {
            String filePath = "server" + File.separator + "json" + File.separator + "PlayersJson.json";
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray jsonArray = new JSONArray(jsonContent);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject playerObj = jsonArray.getJSONObject(i);
                if (playerObj.getString("name").equals(playerName)) {
                    double currentScore = playerObj.getDouble("fullScore");
                    int currentGameCount = playerObj.getInt("gameCounter");

                    return currentScore / currentGameCount;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }


    public void informPlayerAboutSelf(PacketPlayerID playerIDPacket) {
        server.sendToTCP(playerIDPacket.playerID, playerIDPacket);
    }


    public void broadcastNewPlayerInfo(int playerId, String role, String name) {
        PacketOtherPlayersInfo newPlayerInfo = new PacketOtherPlayersInfo();
        newPlayerInfo.addPlayer(playerId, role, name);
        for (Connection conn : gameClients.values()) {
            if (conn.getID() != playerId) {
                server.sendToTCP(conn.getID(), newPlayerInfo);
            }
        }
    }

    public List<String> getPlayerNames() {
        return gameClients.values().stream()
                .map(conn -> connectionToName.get(conn.getID()))
                .collect(Collectors.toList());
    }



    public void clearGameData() {
        gameClients.clear();
        playerCount = 0;
        gameState.clear();
        currentRound = 1;
        playerPositiveCardsMap.clear();
        playerNegativeCardsMap.clear();
        playerModifierCardsMap.clear();
        cardSlotPositionMap.clear();
        totalTeamPlayers = 2;
        hasPlayerAI = false;
        hasDeciderAI = false;
        playersAI.clear();
        availableRoles = new ArrayList<>(Arrays.asList(LEFT_PLAYER, RIGHT_PLAYER));
        playerIDs.clear();
    }

    public void initializeSlots() {
        slotOccupancy.put("LeftSlot0", false);
        slotOccupancy.put("LeftSlot1", false);
        slotOccupancy.put("LeftSlot2", false);
        slotOccupancy.put("LeftSlot3", false);
        slotOccupancy.put("LeftSlot4", false);
        slotOccupancy.put("LeftSlot5", false);
        slotOccupancy.put("RightSlot0", false);
        slotOccupancy.put("RightSlot1", false);
        slotOccupancy.put("RightSlot2", false);
        slotOccupancy.put("RightSlot3", false);
        slotOccupancy.put("RightSlot4", false);
        slotOccupancy.put("RightSlot5", false);
    }



    public void updateServerSlotMap(Map<String, Boolean> clientUpdates, int clientId) {
        clientUpdates.forEach((slotName, isOccupied) -> {
            Boolean currentStatus = slotOccupancy.get(slotName);
            if (currentStatus == null || !currentStatus.equals(isOccupied)) {
                slotOccupancy.put(slotName, isOccupied);
                broadcastSlotUpdate(slotName, isOccupied, clientId);
                // printMapContents(cardSlotPositionMap);
            }
        });
    }

    private void broadcastSlotUpdate(String slotName, boolean isOccupied, int excludeClientId) {
        PacketSlotOccupancy packet = new PacketSlotOccupancy(Collections.singletonMap(slotName, isOccupied));

        // Iterate over the playerConnections in the current session
        for (Connection conn : gameClients.values()) {
            if (conn.getID() != excludeClientId) {  // Exclude the sender
                conn.sendTCP(packet);
            }
        }
    }
    private boolean checkSlotOccupancy(String slotName) {
        // Assuming 'serverSlotMap' contains the current occupancy status of each slot
        Boolean status = slotOccupancy.get(slotName);
        return status != null && status; // Return true if slot is occupied
    }

    private void updateCardSlotPositions(Map<Integer, String> cardSlot) {
        cardSlot.forEach((cardId, slotName) -> {
            // Assuming a server-side map called 'cardSlotPositionMap' exists and is initialized elsewhere
            cardSlotPositionMap.put(cardId, slotName);
            for (AI someAI : playersAI) {
                someAI.getAiBlackboard().setSlotOccupancy(cardSlotPositionMap);
            }
        });
    }

    private void sendGameState() {
        PacketCardPositionMap packet = new PacketCardPositionMap();
        packet.setCardPositionInfoMap(gameState);

        // Iterate over the playerConnections in the current session to send the game state
        for (Connection conn : gameClients.values()) {
            conn.sendTCP(packet);
        }
    }


    public void sendPlayerCardsMap(Map<Integer, List<Integer>> playerCardsMap) {
        PacketPlayerCardsMap packet = new PacketPlayerCardsMap(playerCardsMap);
        sendToAllInSession(packet);
        for (AI someAi : playersAI) {
            if (!someAi.getRole().equals(DECIDER)) {
                List<Integer> cardsToAi = playerCardsMap.get(-2);
                someAi.getAiBlackboard().setAiCards(cardsToAi);
            }
        }
    }

    public void printMapContents(Map<Integer, String> map) {
        if (map.isEmpty()) {
            logger.info("The map is currently empty.");
        } else {
            logger.info("Current contents of the map:");
            map.forEach((key, value) -> logger.info("Card ID: " + key + " - Slot: " + value));
        }
    }

    public void sendToAllInSession(Object packet) {
        for (Connection conn : gameClients.values()) {
            logger.info("Sending packet to all clients: " + packet.toString());
            conn.sendTCP(packet);
            logger.info("Sent packet to client with ID: " + conn.getID());
        }
    }

    public String getPlayerNameByConnectionId(int connectionId) {
        return connectionToName.get(connectionId);
    }



}
