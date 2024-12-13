package ee.taltech.okgame.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import ee.taltech.okgame.FontManager;
import ee.taltech.okgame.GameClient;
import ee.taltech.okgame.GameStateManager;
import ee.taltech.okgame.Slot;
import ee.taltech.okgame.packet.PacketCardPositionInfo;
import ee.taltech.okgame.packet.PacketCardSlotPosition;
import ee.taltech.okgame.rules.RulesRoundFour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;


public class Card extends Group {

    Vector2 screenSize = GameStateManager.getInstance().getCurrentScreenSize();
    private float worldWidth = screenSize.x;
    private float worldHeight = screenSize.y;
    private static Image frontSide; // Photo side
    private Label backSide;
    private boolean isFrontVisible = true;
    private Vector2 lastValidPosition = new Vector2();
    private float targetWidth = 0.156f;
    private float targetHeight = 0.139f;
    private int cardWidth = (int) (worldWidth * targetWidth);
    private int cardHeight = (int) (worldHeight * targetHeight);
    public int cardID;
    Logger logger = Logger.getLogger(getClass().getName());

    private static int playerId;
    private int currentPlayerId;
    private static Map<Integer, List<Integer>> cardsMap;
    private Vector2 originalPosition = new Vector2();
    private static final String DESCRPTION = "description";
    /**
     * Vector is used to send absolute, not relative coordinates.
     */

    /**
     * Card constructor.
     *
     * @param frontTexture
     * @param backText
     * @param playerId
     */
    public Card(Texture frontTexture, String backText, int playerId) {

        Card.playerId = playerId;

        frontSide = new Image(frontTexture);
        frontSide.setSize(cardHeight, cardHeight);  // Half the card width for landscape layout
        frontSide.setPosition(0, 0); // Positioned on the left
        this.addActor(frontSide);

        // Create a white background for the text side
        Pixmap pixmap = new Pixmap(cardHeight, cardHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE); // Set the color to white
        pixmap.fill(); // Fill the pixmap with the white color
        Texture whiteTexture = new Texture(pixmap); // Create a texture from pixmap
        pixmap.dispose(); // Dispose pixmap as it's no longer needed
        Image textBackground = new Image(whiteTexture); // Create an image actor for the white background
        textBackground.setSize(cardHeight, cardHeight);
        textBackground.setPosition(cardHeight, 0); // Positioned on the right, same as the text
        this.addActor(textBackground);  // Add the white background before the text label

        // Create the label (back side, but now just right side text)
        BitmapFont font = new BitmapFont();
        LabelStyle labelStyle = new LabelStyle(FontManager.getInstance().getFontOpenSansRegular16(), Color.BLACK); // Customize color as needed
        backSide = new Label(backText, labelStyle);
        backSide.setWrap(true); // Enable text wrapping to fit within the label's bounds
        backSide.setWidth(cardHeight); // Adjust width to fit within the 150x150 limit, leaving some margin
        backSide.setPosition((float) cardHeight, (float) (cardHeight * 0.43)); // Adjust position to leave some margin and center vertically
        backSide.setAlignment(Align.topLeft); // Align text to top-left within the label bounds
        backSide.setAlignment(Align.center); // Center text horizontally within the label bounds
        this.addActor(backSide);  // Ensure the text label is added after the background, so it's drawn on top

        // Adjust the size of the white background to match the label's size and position
        textBackground.setSize(cardHeight, cardHeight);
        textBackground.setPosition(cardHeight, 0); // Keep the position to the right.


        // Set the size of the whole group to encompass both the image and text side by side
        this.setSize(cardWidth, cardHeight); // Total landscape card size

        setupGestureListener();

    }

    /**
     * PlayerID (and role - in the future) is set based on data from the server
     *
     * @param playerId
     */
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    /**
     * Is there a need for this method?
     *
     * @param cardID
     */
    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public static List<Card> readCardsFromModifierJSON(String path, int playerId) {
        List<Card> personalCards = new ArrayList<>();
        Json json = new Json();
        // Read JSON file using libGDX file handling
        FileHandle fileHandle = Gdx.files.internal(path);
        String jsonData = fileHandle.readString("UTF-8");
        Array<JsonValue> cardArray = json.fromJson(Array.class, jsonData);
        System.out.println("\n[][][] Trying to get cardsMap");
        cardsMap = GameClient.cardsMap; // Get the IDs from server
        System.out.println("\n[][][] Trying to get cardsMap");
        System.out.println("These are the modifier cards: " + cardsMap); // Can be deleted later
        for (JsonValue cardValue : cardArray) {
            int cardID = cardValue.getInt("ID");
            if (cardsMap.get(playerId).contains(cardID)) {
                if (301 <= cardID && cardID <= 320) {
                    String positiveNegative = "Bad";
                    String description = cardValue.getString(DESCRPTION);
                    Texture texture = new Texture("Modifier" + positiveNegative + ".png");
                    // Create a new card, add it to the list.
                    Card card = new Card(texture, description, playerId);
                    card.setCardID(cardID); // Set the ID of the card
                    personalCards.add(card);
                } else if (cardsMap.get(playerId).contains(cardID) && 321 <= cardID && cardID <= 340) {
                    String positiveNegative = "Good";
                    String description = cardValue.getString(DESCRPTION);
                    Texture texture = new Texture("Modifier" + positiveNegative + ".png");
                    // Create a new card, add it to the list.
                    Card card = new Card(texture, description, playerId);
                    card.setCardID(cardID); // Set the ID of the card
                    personalCards.add(card);
                }
            }
        }
        return personalCards;
    }

    /**
     * Method to read from PositiveJSON, will be used when different pictures used for Pos and Neg.
     *
     * @param path     of the file to be read.
     * @param playerId of the player.
     * @return the cards for given player.
     */
    public static List<Card> readCardsFromPositiveJSON(String path, int playerId) {
        List<Card> personalCards = new ArrayList<>();
        Json json = new Json();
        // Read JSON file using libGDX file handling
        FileHandle fileHandle = Gdx.files.internal(path);
        String jsonData = fileHandle.readString("UTF-8");
        Array<JsonValue> cardArray = json.fromJson(Array.class, jsonData);
        System.out.println("Trying to get cardsMap");
        cardsMap = GameClient.cardsMap; // Get the IDs from server
        System.out.println("Trying to get cardsMap");
        System.out.println("These are the Positive cards: " + cardsMap); // Can be deleted later
        for (JsonValue cardValue : cardArray) {
            int cardID = cardValue.getInt("ID");
            if (cardsMap.get(playerId).contains(cardID)) {
                String pictureName = cardValue.getString("ID").substring(1);
                String description = cardValue.getString(DESCRPTION);
                Texture texture = new Texture("Teammate" + pictureName + ".png");
                // Create a new card, add it to the list.
                Card card = new Card(texture, description, playerId);
                card.setCardID(cardID); // Set the ID of the card
                personalCards.add(card);
            }
        }

        return personalCards;
    }

    /**
     * Method to read from NegativeJSON, will be used when different pictures used for Pos and Neg.
     *
     * @param path     of the file to be read.
     * @param playerId of the player.
     * @return the cards for given player.
     */

    public static List<Card> readCardsFromNegativeJSON(String path, int playerId) {
        List<Card> personalCards = new ArrayList<>();
        Json json = new Json();
        // Read JSON file using libGDX file handling
        FileHandle fileHandle = Gdx.files.internal(path);
        String jsonData = fileHandle.readString("UTF-8");
        Array<JsonValue> cardArray = json.fromJson(Array.class, jsonData);
        cardsMap = GameClient.cardsMap; // Get the IDs from server
        System.out.println("These are the Negative cards: " + cardsMap); // Can be deleted later
        for (JsonValue cardValue : cardArray) {
            int cardID = cardValue.getInt("ID");
            if (cardsMap.get(playerId).contains(cardID)) {
                String pictureName = cardValue.getString("ID").substring(1);
                String description = cardValue.getString(DESCRPTION);
                Texture texture = new Texture("Teammate" + "2" + pictureName + ".png");
                // Create a new card, add it to the list.
                Card card = new Card(texture, description, playerId);
                card.setCardID(cardID); // Set the ID of the card
                personalCards.add(card);
            }
        }

        return personalCards;
    }

    /**
     * Method is used to create card object from json based ID only.
     * Card objects are created from json to keep the ID the same and avoid sending card objects to server (only ID).
     * @param path
     * @param playerId
     * @param searchCardId
     * @return
     */
    public static Card readCardFromJSONById(String path, int playerId, int searchCardId) {
        Json json = new Json();
        FileHandle fileHandle = Gdx.files.internal(path);
        String jsonData = fileHandle.readString("UTF-8");
        Array<JsonValue> cardArray = json.fromJson(Array.class, jsonData);

        for (JsonValue cardValue : cardArray) {
            int cardID = cardValue.getInt("ID");
            if (cardID == searchCardId) {
                String pictureName = String.valueOf(cardID).substring(1);
                String description = cardValue.getString(DESCRPTION);
                if (cardID < 200) {
                    Texture texture = new Texture("Teammate" + pictureName + ".png");
                    Card card = new Card(texture, description, playerId);
                    card.setCardID(cardID);
                    return card;
                } else {
                    Texture texture = new Texture("Teammate" + "2" + pictureName + ".png");
                    Card card = new Card(texture, description, playerId);
                    card.setCardID(cardID);
                    return card;
                }
            }
        }
        return null; // No card found with the given ID
    }

    public static Card readCardFromModifierJSONById(String path, int playerId, int searchCardId) {
        Json json = new Json();
        FileHandle fileHandle = Gdx.files.internal(path);
        String jsonData = fileHandle.readString("UTF-8");
        Array<JsonValue> cardArray = json.fromJson(Array.class, jsonData);

        for (JsonValue cardValue : cardArray) {
            int cardID = cardValue.getInt("ID");
            if (cardID == searchCardId) {
                String description = cardValue.getString(DESCRPTION);
                if (301 <= cardID && cardID <= 320) {
                    Texture texture = new Texture("ModifierBad.png");
                    Card card = new Card(texture, description, playerId);
                    card.setCardID(cardID);
                    return card;
                }
                else {
                    Texture texture = new Texture("ModifierGood.png");
                    Card card = new Card(texture, description, playerId);
                    card.setCardID(cardID);
                    return card;
                }
            }
        }
        return null; // No card found with the given ID
    }


    /**
     * Create 3 random cards for the player.
     * Random selection moves to  the server in the future.
     * @param playerId
     * @return
     */
    public static List<Card> getPersonalCards(int playerId) {
        List<Card> personalCards = new ArrayList<>(); // Declare outside to make it accessible
        if (GameStateManager.getInstance().getCurrentRound() == 1 || GameStateManager.getInstance().getCurrentRound() == 2) {
            personalCards = readCardsFromPositiveJSON("PositiveCardsJson.json", playerId);
        }
        if (GameStateManager.getInstance().getCurrentRound() == 3) {
            personalCards = readCardsFromNegativeJSON("NegativeCardsJson.json", playerId);
        } else if (GameStateManager.getInstance().getCurrentRound() == 4 || GameStateManager.getInstance().getCurrentRound() == 5) {
            personalCards = readCardsFromModifierJSON("ModifierCardsJson.json", playerId);
        }
        List<Card> randomCards = new ArrayList<>();
        int nrOfRandomElements = 3;
        Random randomEl = new Random();
        for (int i = 0; i < nrOfRandomElements; i++) {
            int randomIndex = randomEl.nextInt(personalCards.size());
            // Get a random card from existing cards.
            Card randomCard = personalCards.get(randomIndex);
            // Add the random card into the list of three random cards.
            randomCards.add(randomCard);
            // Remove the chosen random card from personalCards, so it wouldn't be chosen again.
            personalCards.remove(randomIndex);
        }
        return randomCards;
    }

    public int getCardID() {
        return cardID;
    }


    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            // System.out.println("Card added to stage - ID: " + this.cardID);
        } else {
            // System.out.println("Card removed from stage - ID: " + this.cardID + this);
        }
    }

    public static int getPlayerId() {
        return playerId;
    }

    private void setupGestureListener() {

        int cardID = this.getId();

        this.addListener(new ActorGestureListener() {

            /**
             * Player has clicked on the card.
             * @param event
             * @param x
             * @param y
             * @param pointer
             * @param button
             */
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                super.touchDown(event, x, y, pointer, button);
                originalPosition.set(getX(), getY());
            }

            /**
             * Is this the holding and moving card action?
             * Single card PacketCardPositionInfo could be sent from here to show smooth movement to other players.
             * @param event
             * @param x
             * @param y
             * @param deltaX
             * @param deltaY
             */
            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                moveBy(deltaX, deltaY);
            }

            /**
             * Card is moved to new position and released.
             * @param event
             * @param x
             * @param y
             * @param pointer
             * @param button
             */


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                int currentRound = GameStateManager.getInstance().getCurrentRound();
                if (currentRound == 4 && !GameStateManager.getInstance().isMoveMade()) {
                    Vector2 stageCoordinates = localToStageCoordinates(new Vector2(x, y));
                    Slot slot = GameStateManager.getInstance().determineSlot(stageCoordinates.x, stageCoordinates.y);

                    if (slot != null ) {
                        boolean localOccupancy = slot.isOccupied();
                        System.out.println("TouchUp - Round 4: Checking slot: " + slot.getSlotName() + ", Locally Occupied: " + localOccupancy);

                        if (!localOccupancy) {
                            RulesRoundFour.performAction(Card.this, slot);
                            GameStateManager.getInstance().setSlotOccupancy(slot.getSlotName(), true);
                            GameStateManager.getInstance().setMoveMade(true);

                            GameClient.getInstance().requestSlotOccupancy(slot.getSlotName(), isOccupied -> {
                                System.out.println("Server response for slot " + slot.getSlotName() + ": " + (isOccupied ? "Occupied" : "Not Occupied"));
                                if (isOccupied) {
                                    Card.this.resetCardPosition();
                                    GameStateManager.getInstance().setSlotOccupancy(slot.getSlotName(), false);
                                    GameStateManager.getInstance().setMoveMade(false);
                                    System.out.println("Reverting placement: Server indicates slot occupied.");
                                }

                            });

                            PacketCardPositionInfo cardPositionInfo = new PacketCardPositionInfo();
                            cardPositionInfo.cardID = Card.this.cardID;
                            cardPositionInfo.x = (int) ((slot.getGlobalX() / worldWidth) * 1920);
                            cardPositionInfo.y = (int) ((slot.getGlobalY() / worldHeight) * 1080);
                            GameClient.getInstance().sendTCP(cardPositionInfo);
                            GameClient.getInstance().sendTCP(new PacketCardSlotPosition(Card.this.getCardID(), slot.getSlotName()));
                        } else {
                            System.out.println("Slot " + slot.getSlotName() + " is already occupied.");
                            Card.this.resetCardPosition();
                            GameStateManager.getInstance().setMoveMade(false);
                        }
                    } else {
                        System.out.println("No valid slot found at coordinates (" + stageCoordinates.x + ", " + stageCoordinates.y + ").");
                        Card.this.resetCardPosition();
                        GameStateManager.getInstance().setMoveMade(false);
                    }

                } else if (currentRound == 5 && !GameStateManager.getInstance().isMoveMade()) {
                    Vector2 stageCoordinates = localToStageCoordinates(new Vector2(x, y));
                    Slot slot = GameStateManager.getInstance().determineSlot(stageCoordinates.x, stageCoordinates.y);

                    if (slot != null) {
                        boolean localOccupancy = slot.isOccupied();
                        System.out.println("TouchUp - Round 4: Checking slot: " + slot.getSlotName() + ", Locally Occupied: " + localOccupancy);

                        if (!localOccupancy) {
                            RulesRoundFour.performAction(Card.this, slot);
                            GameStateManager.getInstance().setSlotOccupancy(slot.getSlotName(), true);
                            GameStateManager.getInstance().setMoveMade(true);

                            GameClient.getInstance().requestSlotOccupancy(slot.getSlotName(), isOccupied -> {
                                System.out.println("Server response for slot " + slot.getSlotName() + ": " + (isOccupied ? "Occupied" : "Not Occupied"));
                                if (isOccupied) {
                                    Card.this.resetCardPosition();
                                    GameStateManager.getInstance().setSlotOccupancy(slot.getSlotName(), false);
                                    GameStateManager.getInstance().setMoveMade(false);
                                    System.out.println("Reverting placement: Server indicates slot occupied.");
                                }
                            });
                            PacketCardPositionInfo cardPositionInfo = new PacketCardPositionInfo();
                            cardPositionInfo.cardID = Card.this.cardID;
                            cardPositionInfo.x = (int) ((slot.getGlobalX() / worldWidth) * 1920);
                            cardPositionInfo.y = (int) ((slot.getGlobalY() / worldHeight) * 1080);
                            GameClient.getInstance().sendTCP(cardPositionInfo);
                            GameClient.getInstance().sendTCP(new PacketCardSlotPosition(Card.this.getCardID(), slot.getSlotName()));
                        } else {
                            System.out.println("Slot " + slot.getSlotName() + " is already occupied.");
                            Card.this.resetCardPosition();
                            GameStateManager.getInstance().setMoveMade(false);
                        }
                    } else {
                        System.out.println("No valid slot found at coordinates (" + stageCoordinates.x + ", " + stageCoordinates.y + ").");
                        Card.this.resetCardPosition();
                        GameStateManager.getInstance().setMoveMade(false);
                    }
                }

                else {
                    // Handling for rounds 1 to 3 remains unchanged
                    Vector2 stageCoordinates = localToStageCoordinates(new Vector2(x, y));
                    GameStateManager.getInstance().handleCardDrop(Card.this, stageCoordinates.x, stageCoordinates.y, playerId);

                    Vector2 finalCoords = getParent() == null ? new Vector2(getX(), getY()) : getParent().localToStageCoordinates(new Vector2(getX(), getY()));
                    PacketCardPositionInfo cardPositionInfo = new PacketCardPositionInfo();
                    cardPositionInfo.cardID = Card.this.cardID;
                    cardPositionInfo.x = (int) ((finalCoords.x / worldWidth) * 1920);
                    cardPositionInfo.y = (int) ((finalCoords.y / worldHeight) * 1080);
                    GameClient.getInstance().sendTCP(cardPositionInfo);
                }
            }




        });
    }

    /**
     * Get unique ID of the card (same as in json file)
     * @return
     */
    public int getId() {
        return this.cardID;
    }

    /**
     * Method to set the position of the card
     */
    public void resetCardPosition() {
        // Move the card back to its original position
        setPosition(originalPosition.x, originalPosition.y);
    }

    public void setPositionToMatchSlot(Slot slot) {
        if (slot == null) {
            return;
        }
        // Update the slot's position just in case it hasn't been updated recently.
        slot.updateGlobalPosition();

        // Use the global coordinates directly
        float x = slot.getGlobalX();
        float y = slot.getGlobalY();

        // Since these coordinates are global, we need to convert them if the card has a parent other than the stage
        if (getParent() != null) {
            Vector2 localCoords = getParent().stageToLocalCoordinates(new Vector2(x, y));
            setPosition(localCoords.x, localCoords.y);
        } else {
            // If the parent is the stage or there is no parent, set directly
            setPosition(x, y);
        }
    }

}
