package ee.taltech.okgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import ee.taltech.okgame.cards.Card;
import ee.taltech.okgame.packet.PacketCardPositionInfo;
import ee.taltech.okgame.players.Player;
import ee.taltech.okgame.rules.RulesRoundOne;
import ee.taltech.okgame.rules.RulesRoundOneB;
import ee.taltech.okgame.rules.RulesRoundTwo;
import ee.taltech.okgame.rules.RulesRoundTwoB;
import ee.taltech.okgame.rules.RulesRoundThree;
import ee.taltech.okgame.rules.RulesRoundThreeB;
import ee.taltech.okgame.screens.GameBoardScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameStateManager {
    private static GameStateManager instance;

    private GameUpdateListener updateListener;

    private GameBoardScreen gameBoardScreen;

    private int currentRound = 1;
    private Vector2 currentScreenSize = new Vector2(1600, 900);

    private Table otherTeamArea;
    private Table myTeamArea;
    private Table myPersonalArea;

    private Map<Integer, Boolean> playerMovesThisRound = new HashMap<>();
    private Map<String, Slot> slots = new HashMap<>();
    private boolean moveMade = false;
    private List<RoundNumberChangeListener> roundChangeListeners = new ArrayList<>();
    private boolean isAwaitingServerConfirmation = false;


    /**
     * New GameStateManager.
     * @return
     */
    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }
    public void setGameUpdateListener(GameUpdateListener listener) {
        this.updateListener = listener;
    }

    /**
     * New visible GameBoardScreen is set.
     * @param screen
     */
    public void setGameBoardScreen(GameBoardScreen screen) {
        this.gameBoardScreen = screen;
    }

    /**
     * Visible GameBoard is updated based on map from server.
     * @param positionInfoMap
     */
    public void updateGameState(Map<Integer, PacketCardPositionInfo> positionInfoMap) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (gameBoardScreen != null) {
                    gameBoardScreen.refreshGameBoard(positionInfoMap);
                }
            }
        });
    }

    public void startNewRound() {
        playerMovesThisRound.clear(); // Reset the moves tracking
        // Any other round initialization code
    }


    public void setOtherTeamArea(Table area) {
        this.otherTeamArea = area;
    }

    public Table getOtherTeamArea() {
        return this.otherTeamArea;
    }

    public void setMyTeamArea(Table area) {
        this.myTeamArea = area;
    }

    public Table getMyTeamArea() {
        return this.myTeamArea;
    }

    public void setMyPersonalArea(Table area) {
        this.myPersonalArea = area;
    }

    public Table getMyPersonalArea(Table area) {
        return this.myPersonalArea;
    }


    // Determines the name of the area based on the global coordinates and returns a string that is used by the rules.
    public String determineArea(float dropX, float dropY) {
        // Check if the drop is within the bounds of myTeamArea
        if (myTeamArea != null && dropX >= myTeamArea.getX() && dropX <= myTeamArea.getX() + myTeamArea.getWidth()
                && dropY >= myTeamArea.getY() && dropY <= myTeamArea.getY() + myTeamArea.getHeight()) {
            return "myTeamArea";
        }
        // Check if the drop is within the bounds of otherTeamArea
        else if (otherTeamArea != null && dropX >= otherTeamArea.getX() && dropX <= otherTeamArea.getX() + otherTeamArea.getWidth()
                && dropY >= otherTeamArea.getY() && dropY <= otherTeamArea.getY() + otherTeamArea.getHeight()) {
            return "otherTeamArea";
        }
        // Check if the drop is within the bounds of myPersonalArea
        else if (myPersonalArea != null && dropX >= myPersonalArea.getX() && dropX <= myPersonalArea.getX() + myPersonalArea.getWidth()
                && dropY >= myPersonalArea.getY() && dropY <= myPersonalArea.getY() + myPersonalArea.getHeight()) {
            return "myPersonalArea";
        }
        return null;
    }

    public void handleCardDrop(Card card, float dropX, float dropY, int playerId) {

        if (Boolean.TRUE.equals(playerMovesThisRound.get(playerId))) {
            System.out.println("Player " + playerId + " has already made a move this round.");
            card.resetCardPosition(); // Prevent the move
            setMoveMade(false);
            return;
        }

        Player player = PlayerManager.getInstance().getSelf();
        String playerRole = player.getPlayerRole();
        String areaName = determineArea(dropX, dropY);
        boolean moveMade = false;

        // Temporary solution with rules up to 3 rounds.
        switch (currentRound) {
            case 1:
                if (playerRole.equals("RightPlayer") && RulesRoundOne.isDropValid(areaName, dropX, dropY)) {
                    RulesRoundOne.performAction(card);
                    moveMade = true;
                } else if (playerRole.equals("LeftPlayer") && RulesRoundOneB.isDropValid(areaName, dropX, dropY)) {
                    RulesRoundOneB.performAction(card);
                    moveMade = true;
                } else {
                    card.resetCardPosition();
                }
                break;
            case 2:
                // Second round rules for each player.
                if (playerRole.equals("RightPlayer") && RulesRoundTwo.isDropValid(areaName, dropX, dropY)) {
                    RulesRoundTwo.performAction(card);
                    moveMade = true;
                } else if (playerRole.equals("LeftPlayer") && RulesRoundTwoB.isDropValid(areaName, dropX, dropY)) {
                    RulesRoundTwoB.performAction(card);
                    moveMade = true;
                } else {
                    card.resetCardPosition();
                }
                break;
            case 3:
                // Third round rules for each player.
                if (playerRole.equals("RightPlayer") && RulesRoundThree.isDropValid(areaName, dropX, dropY)) {
                    RulesRoundThree.performAction(card);
                    moveMade = true;
                } else if (playerRole.equals("LeftPlayer") && RulesRoundThreeB.isDropValid(areaName, dropX, dropY)) {
                    RulesRoundThreeB.performAction(card);
                    moveMade = true;
                } else {
                    card.resetCardPosition();
                }
                break;
            case 4:
                printAllSlotDetails();
                // Fourth round rules, shared by all players.
                break;
            case 5:
                break;
            default:
                System.out.println("Unknown round. No rules applied.");
                card.resetCardPosition();
                setMoveMade(false);
                break;
        }

        if (moveMade) {
            playerMovesThisRound.put(playerId, true); // Record that the player has made a move
            setMoveMade(true);
        } else {
            System.out.println("Move was not valid or player already moved this round.");
            card.resetCardPosition();
            setMoveMade(false);
        }
    }

    public static Slot determineSlot(float dropX, float dropY) {
        Map<String, Slot> slots = GameStateManager.getInstance().getAllSlots();
        for (Map.Entry<String, Slot> entry : slots.entrySet()) {
            Slot slot = entry.getValue();
            // Update global position before checking
            slot.updateGlobalPosition();
            // Debugging print statement
            System.out.println("1. Checking slot '" + slot.getSlotName() + "' (Actor Name: " + slot.getName() + ") at position (" + slot.getGlobalX() + ", " + slot.getGlobalY() + ") with width " + slot.getWidth() + " and height " + slot.getHeight() + " against drop coordinates (" + dropX + ", " + dropY + ")");
            if (dropX >= slot.getGlobalX() && dropX <= (slot.getGlobalX() + slot.getWidth()) &&
                    dropY >= slot.getGlobalY() && dropY <= (slot.getGlobalY() + slot.getHeight())) {
                System.out.println("Drop within slot '" + slot.getSlotName() + "'");
                return slot; // Return the slot if it matches the criteria
            }
        }
        System.out.println("No suitable slot found for drop coordinates (" + dropX + ", " + dropY + ")");
        return null; // Return null if no suitable slot is found
    }


    public void addRoundChangeListener(RoundNumberChangeListener listener) {
        roundChangeListeners.add(listener);
    }

    public void removeRoundChangeListener(RoundNumberChangeListener listener) {
        roundChangeListeners.remove(listener);
    }

    public void setCurrentRound(int roundNumber) {
        this.currentRound = roundNumber;
        startNewRound();
        notifyRoundChange();
        notifyRoundNumberChange();
    }
    protected void notifyRoundNumberChange() {
        for (RoundNumberChangeListener listener : roundChangeListeners) {
            listener.onRoundChange(currentRound);
        }
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void notifyRoundChange() {
        if (updateListener != null) {
            updateListener.onRoundChange();
        }
        // Perform any other actions needed game-wide due to the round change
    }

    public void playerUpdated() {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (gameBoardScreen != null) {
                    gameBoardScreen.updatePlayerLabels();
                }
            }
        });
        System.out.println("GameStateManager: Notifying GameBoardScreen to update player labels.");
    }
    public Vector2 getCurrentScreenSize() {
        return currentScreenSize;
    }


    public void setCurrentScreenSize(int width, int height) {
        this.currentScreenSize.set(width, height);
    }

    public void addSlot(String slotName, Slot slot) {
        slots.put(slotName, slot);
    }

    public Slot getSlot(String slotName) {
        return slots.get(slotName);
    }

    public Map<String, Slot> getAllSlots() {
        return new HashMap<>(slots);  // Return a copy to prevent external modifications
    }

    public boolean getSlotOccupancy(String slotName) {
        Slot slot = slots.get(slotName);
        return slot != null && slot.isOccupied();
    }


    public void setSlotOccupancy(String slotName, boolean occupied) {
        Slot slot = slots.get(slotName);
        if (slot != null) {
            slot.setOccupied(occupied);
            System.out.println("Updated occupancy for slot '" + slotName + "' to " + (occupied ? "occupied" : "not occupied"));
        } else {
            System.out.println("Attempted to set occupancy for non-existent slot '" + slotName + "'");
        }
    }

    public void printDebugInfo(Card card, Slot slot) {
        String occupiedStatus = slot.isOccupied() ? "occupied" : "not occupied";
        String message = String.format(
                "Debug Info: Card '%s' with ID %d was transferred to slot '%s' at position (x=%.2f, y=%.2f), " +
                        "which was %s. The card's new position is set to (x=%.2f, y=%.2f).",
                card.getName(),  // Assuming Card has a getName() method
                card.getId(),
                slot.getSlotName(),
                slot.getX(), slot.getY(),
                occupiedStatus,
                card.getX(), card.getY()
        );
        System.out.println(message);
    }

    public void printAllSlotDetails() {
        System.out.println("Current Slot Occupancy Details:");
        for (Map.Entry<String, Slot> entry : slots.entrySet()) {
            Slot slot = entry.getValue();
            System.out.printf("Slot Name: %s, Position: (X: %.2f, Y: %.2f), Width: %.2f, Height: %.2f, Occupied: %b\n",
                    entry.getKey(), slot.getGlobalX(), slot.getGlobalY(), slot.getWidth(), slot.getHeight(), slot.isOccupied());
        }
    }
    public void setMoveMade(boolean moveMade) {
        this.moveMade = moveMade;
    }

    public boolean isMoveMade() {
        return moveMade;
    }

    public void printAllSlotOccupancy() {
        System.out.println("Printing all slot occupancy details:");
        Map<String, Slot> slots = getAllSlots();
        for (Map.Entry<String, Slot> entry : slots.entrySet()) {
            Slot slot = entry.getValue();
            System.out.println("Slot Name: " + slot.getSlotName() + ", Occupied: " + slot.isOccupied());
        }
    }
    // Method to set the awaiting confirmation status
    public void setAwaitingServerConfirmation(boolean awaiting) {
        this.isAwaitingServerConfirmation = awaiting;
    }

    // Method to check if the system is awaiting server confirmation
    public boolean isAwaitingServerConfirmation() {
        return isAwaitingServerConfirmation;
    }






}
