package ee.taltech.okgame.server.ai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import packet.PacketCardPositionInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static ee.taltech.okgame.server.GameServer.LEFT_PLAYER;
import static ee.taltech.okgame.server.GameServer.RIGHT_PLAYER;

public class Blackboard {
    private AI playerAI;
    private static final Logger logger = Logger.getLogger("AI-BB");
    private Map<Integer, Integer> cardsFromJson = new HashMap<>();
    private Map<Integer, Integer> gameStatePositivity = new HashMap<>();
    private Map<Integer, Double> resultOfParallelTask = new HashMap<>();
    private int currentRound = 1;
    private Map<Integer, Integer> leftCardsPositivity = new HashMap<>();
    private Map<Integer, Integer> rightCardsPositivity = new HashMap<>();
    private double sumOfLeftCards = 0;
    private double sumOfRightCards = 0;
    private String winner;

    // Three cards given in each row. Not all of those appear on GameBoard
    private List<Integer> aiCards = new ArrayList<>();
    private List<String> occupiedSlots = new ArrayList<>();
    public Blackboard(AI playerAI) {
        this.playerAI = playerAI;
    }

    public AI getAI() {
        return playerAI;
    }

    /**
     * Read cards from JSON when new AI is created
     */
    public void putCardsFromJsonToBlackBoard() {
        cardsFromJson.putAll(getCardsFromJson("server" + File.separator + "json" + File.separator + "PositiveCardsJson.json"));
        cardsFromJson.putAll(getCardsFromJson("server" + File.separator + "json" + File.separator + "NegativeCardsJson.json"));
        cardsFromJson.putAll(getCardsFromJson("server" + File.separator + "json" + File.separator + "ModifierCardsJson.json"));
    }

    /**
     * Create single card score map
     * Different JSON file for first, third and fourth row.
     */
    public Map<Integer, Integer> getCardsFromJson(String path){
        try {
            logger.info("::: [AI BB] Reading JSON from " + path);
            String jsonData = new String(Files.readAllBytes(Paths.get(path)));
            JSONArray cardArray = new JSONArray(jsonData);

            // Map to store card ID and positivity
            Map<Integer, Integer> newCardsFromJson = new HashMap<>();

            for (int i = 0; i < cardArray.length(); i++) {
                JSONObject cardValue = cardArray.getJSONObject(i);
                int cardID = cardValue.getInt("ID");
                int cardPositivity = cardValue.getInt("positivity");
                newCardsFromJson.put(cardID, cardPositivity);
            }

            logger.info("::: [AI BB] Updated cards map from JSON");
            return newCardsFromJson;
        } catch (IOException | JSONException e) {
            logger.severe("Error while reading JSON file: " + e.getMessage());
            e.printStackTrace(); // Log the stack trace of the exception
            return Collections.emptyMap();
        }
    }

    /**
     * Get positivity by card ID
     */
    public int getPositivityById (int cardID) {
        try {
            Integer positivity = this.cardsFromJson.get(cardID);
            if (positivity != null) {
                return positivity;
            } else {
                logger.warning("::: [AI BB] Positivity not found for card " + cardID);
                return 5; // Return a default value
            }
        } catch (NullPointerException e) {
            logger.severe("::: [AI BB] NullPointerException: " + e.getMessage());
            return 5; // Return a default value
        }
    }

    /**
     * Update positivity map based on current game state
     */
    public void setGameState(Map<Integer, PacketCardPositionInfo> newGameState) {
        for (PacketCardPositionInfo packet : newGameState.values()) {
            int xCoordinate = packet.x;
            int cardsID = packet.cardID;
            int cardsPositivity = getPositivityById(cardsID);
            if (xCoordinate < 1000) {
                this.leftCardsPositivity.put(cardsID, cardsPositivity);
            } else {
                this.rightCardsPositivity.put(cardsID, cardsPositivity);
            }
            this.gameStatePositivity.put(cardsID, cardsPositivity);
        }
    }

    /**
     * To get positivity of other side by divison
     * @return double
     */
    public double getGameStatePositivity() {
        return this.gameStatePositivity.values().stream().reduce(0, Integer::sum);
    }

    /**
     * Map of cards on the game board cardID=positivity
     * @return
     */
    public Map<Integer, Integer> getLeftCardsPositivityMap() {
        return this.leftCardsPositivity;
    }

    /**
     * Map of cards on the game board cardID=positivity
     * @return
     */
    public Map<Integer, Integer> getRightCardsPositivityMap() {
        return this.rightCardsPositivity;
    }

    /**
     * Round of the game changes available slots
     */
    public void setCurrentRound(int round) {
        this.currentRound = round;
    }

    /**
     * Round of the game
     */
    public int getCurrentRound() {
        return this.currentRound;
    }

    /**
     * Three cards to choose from, changed in each round
     */
    public void setAiCards(List<Integer> aiCards) {
        logger.info("::: [" + this.playerAI + "] I got three cards " + aiCards);
        this.aiCards = aiCards;
    }

    /**
     * Three cards to choose from, changed in each round
     */
    public List<Integer> getAiCards() {
        return this.aiCards;
    }

    /**
     * Get sum positivity depending on AI role and round
     * Return sum on the side where AI plans to play new card
     * @return double
     */
    public double getSumOfCardsOnRelevantSide() {
        String role = this.getAI().getRole();
        if (role.equals(LEFT_PLAYER)) {
            if (currentRound < 3 || currentRound == 5) {
                return getSumOfLeftCards();
            }
            return getSumOfRightCards();
        }
        if (role.equals(RIGHT_PLAYER)) {
            if (currentRound < 3 || currentRound == 5) {
                return getSumOfRightCards();
            }
            return getSumOfLeftCards();
        }
        return 0;
    }

    /**
     * Sum sum of cards on game board.
     * Used when deciding card to play.
     */
    protected void setSumOfRightCards(double newSumOfRightCards) {
        this.sumOfRightCards = newSumOfRightCards;
    }

    /**
     * Sum of opponents cards on game board.
     * Used when deciding card to play.
     */
    protected double getSumOfRightCards() {
        return this.sumOfRightCards;
    }

    /**
     * Sum sum of cards on game board.
     * Used when deciding card to play.
     */
    protected void setSumOfLeftCards(double newSumOfLeftCards) {
        this.sumOfLeftCards = newSumOfLeftCards;
    }

    /**
     * Sum positivity of cards on game board.
     * Used when deciding card to play.
     */
    protected double getSumOfLeftCards() {
        return this.sumOfLeftCards;
    }

    /**
     * Server updates used slots
     */
    public void setSlotOccupancy(Map <Integer, String> cardSlotPositionMap) {
        this.occupiedSlots = new ArrayList<>(cardSlotPositionMap.values());
    }

    /**
     * Server updates used slots
     */
    public List<String> getOccupiedSlots() {
        return this.occupiedSlots;
    }

    /**
     * Changed positivity on relevant side depending on added card
     * @param cardToCheck int cardID
     * @param newPositivity double sumPositivity
     */
    public void setResultOfParallelTask(int cardToCheck, double newPositivity) {
        this.resultOfParallelTask.put(cardToCheck, newPositivity);
    }

    /**
     * Helper method for reusing round 4 leftover modifiers
     * @param cardID int
     */
    public void removeResultFromParallelTask(int cardID) {
        resultOfParallelTask.remove(cardID);
    }

    /**
     * Changed positivity on relevant side depending on added card
     * @return Map
     */
    public Map<Integer, Double> getResultOfParallelTask() {
        Map<Integer, Double> result = new HashMap<>(resultOfParallelTask);
        return result;
    }

    /**
     * Get winner decided by AI
     * @return String role
     */
    public String getWinner() {
        return this.winner;
    }

    /**
     * Set winner in BehaviorTree
     * @param winner String role
     */
    protected void setWinner(String winner) {
        this.winner = winner;
    }

}
