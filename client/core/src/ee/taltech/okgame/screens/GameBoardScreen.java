package ee.taltech.okgame.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ee.taltech.okgame.*;
import ee.taltech.okgame.cards.Card;
import ee.taltech.okgame.cards.CardInteractionListener;
import ee.taltech.okgame.packet.PacketCardPositionInfo;


import java.util.*;

import static ee.taltech.okgame.cards.Card.readCardFromJSONById;
import static ee.taltech.okgame.cards.Card.readCardFromModifierJSONById;

/**
 * The GameBoardScreen class represents the main game screen for the card game.
 * It manages the rendering and interaction of the game board, player areas, and cards.
 * Implements {@link CardInteractionListener} and {@link GameUpdateListener} for managing card interactions and game state updates.
 */
public class GameBoardScreen extends ScreenAdapter implements CardInteractionListener, GameUpdateListener {
    private final GameStateManager gameStateManager;
    private Stage stage;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch spriteBatch;
    private Texture boardTexture;
    private Table otherTeamArea;
    private Table myTeamArea;
    private Table personalArea;
    Vector2 screenSize = GameStateManager.getInstance().getCurrentScreenSize();
    private float worldWidth = screenSize.x;
    private float worldHeight = screenSize.y;
    private Hud hud;
    private int currentPlayerId;
    private String playerRole;
    Label rightPlayerLabel;
    Label leftPlayerLabel;
    private BitmapFont font;
    FontManager fontManager;



    List<Table> cardSlotsMyTeam = new ArrayList<>();
    List<Table> cardSlotsOtherTeam = new ArrayList<>();
    List<Table> cardSlotsMyPersonal = new ArrayList<>();

    private List<CardInteractionListener> cardListeners = new ArrayList<>();

    public void onAreaNamesSet(String personalAreaName, String myTeamAreaName, String otherTeamAreaName) {

    }

    private Client client;

    public void addCardListener(CardInteractionListener listener) {
        cardListeners.add(listener);
    }

    private void notifyAreaNamesSet() {
        for (CardInteractionListener listener : cardListeners) {
            listener.onAreaNamesSet(personalArea.getName(), myTeamArea.getName(), otherTeamArea.getName());
        }
    }
    /**
     * Initializes a new GameBoardScreen with the specified game, player ID, and player role.
     * Sets up the game stage, viewport, and the initial UI elements of the game board.
     *
     * @param game The main game instance this screen is a part of.
     * @param currentPlayerId The ID of the player using this screen.
     * @param playerRole The role of the player in the game.
     */
    public GameBoardScreen(Game game, int currentPlayerId, String playerRole) {
        this.gameStateManager = GameStateManager.getInstance();
        Vector2 screenSize = gameStateManager.getCurrentScreenSize();
        this.worldWidth = screenSize.x;
        this.worldHeight = screenSize.y;

        this.camera = new OrthographicCamera();
        this.camera.position.set(worldWidth / 2, worldHeight / 2, 0); // Initialize camera position here
        this.viewport = new FitViewport(worldWidth, worldHeight, camera); // Use initialized camera
        this.stage = new Stage(viewport); // Only one stage initialization is needed

        this.spriteBatch = new SpriteBatch();
        this.boardTexture = new Texture("tablev2.png");

        this.currentPlayerId = currentPlayerId;
        this.playerRole = playerRole;
        this.font = new BitmapFont();
        GameStateManager.getInstance().setGameUpdateListener(this);
    }

    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
    /**
     * Called when the screen is shown. Sets up the game board and registers input processors.
     */
    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        table.setTouchable(Touchable.enabled);
        GameStateManager.getInstance().setGameBoardScreen(this);

        hud = new Hud(currentPlayerId, playerRole);

        // Color for player areas can be adjusted - look up RGB code. Alpha adjusts translucence (1 opaque, 0 translucent).
        otherTeamArea = createOtherTeamArea(new Color(0.5f, 0.5f, 0.5f, 0.4f)); //  gray
        myTeamArea = createYourTeamArea(new Color(0.5f, 0.5f, 0.5f, 0.4f)); // gray
        personalArea = createPersonalAreaWithSlots(new Color(0.33f, 0.42f, 0.18f, 0.4f)); // mossy green

        //Assign names to the separate areas for identification
        otherTeamArea.setName("otherTeamArea");
        myTeamArea.setName("myTeamArea");
        personalArea.setName("personalArea");
        myTeamArea.layout();
        otherTeamArea.setTouchable(Touchable.enabled);
        myTeamArea.setTouchable(Touchable.enabled);
        personalArea.setTouchable(Touchable.enabled);

        notifyAreaNamesSet();

        // Space between the areas. By increasing the padding the areas move away from the edge of the screen.
        float padValue = Math.min(worldWidth, worldHeight) * 0.02f;
        float sidePadding = worldWidth * 0.208f;
        table.add(otherTeamArea).expand().fill().pad(padValue);
        table.add(myTeamArea).expand().fill().pad(padValue).row();
        table.add(personalArea).expand().fillX().colspan(2).center().pad(padValue).padLeft(sidePadding).padRight(sidePadding);

        GameStateManager.getInstance().setOtherTeamArea(otherTeamArea);
        GameStateManager.getInstance().setMyTeamArea(myTeamArea); // Assuming you add a setter for myTeamArea in GameStateManager
        GameStateManager.getInstance().setMyPersonalArea(personalArea);

        stage.addActor(table);
        populateSlotsWithCards(personalArea, currentPlayerId);

        GameStateManager.getInstance().setOtherTeamArea(otherTeamArea);
        GameStateManager.getInstance().setMyTeamArea(myTeamArea); // Assuming you add a setter for myTeamArea in GameStateManager
        GameStateManager.getInstance().setMyPersonalArea(personalArea);

        stage.addActor(table);
        populateSlotsWithCards(personalArea, currentPlayerId);


        // Setup input processing to include both the hud and the gameboard stage
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.stage); // HUD stage first to capture UI interactions
        multiplexer.addProcessor(stage); // Then the game stage
        Gdx.input.setInputProcessor(multiplexer);
        scheduleSlotPositionUpdate();


        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.out.println("Clicked coordinates: x=" + x + ", y=" + y);
            }

        });

        updatePlayerLabels();
        stage.addAction(Actions.delay(0.1f, Actions.run(new Runnable() {
            @Override
            public void run() {
                // Now that the stage has been rendered at least once, the slots should have correct positions and sizes
                // Update the GameStateManager with the slot information for each area

                // Optional: Log slot positions and sizes to verify they're now correct
                for (Table slot : cardSlotsMyTeam) {
                    // System.out.println("Slot position and size after delay: x=" + slot.getX() + ", y=" + slot.getY() + ", width=" + slot.getWidth() + ", height=" + slot.getHeight());
                }
            }

        })));
        printSlotGlobalPositions();



    }
    public void onRoundChange() {
        if (gameStateManager.getCurrentRound() < 6) {
            populateSlotsWithCards(personalArea, currentPlayerId); // Repopulate slots with new cards
        }
    }


    private void printSlotGlobalPositions() {
        // Ensure this delay is enough for your stage to be rendered at least once
        stage.addAction(Actions.delay(0.1f, Actions.run(new Runnable() {
            @Override
            public void run() {
                for (Table slot : cardSlotsMyTeam) {
                    Vector2 localCoords = new Vector2(0, 0); // Bottom-left corner of the slot
                    Vector2 globalCoords = slot.localToStageCoordinates(new Vector2(localCoords));
                    // System.out.println("Global position of slot: x=" + globalCoords.x + ", y=" + globalCoords.y);
                }
            }
        })));
    }


    /**
     * Creates the LeftPlayer team cards area with card slots for up to six cards which are placed 2x3.
     * @param color
     * @return
     */
    /**
     * Creates the LeftPlayer team cards area with card slots for up to six cards which are placed 2x3.
     * @param color
     * @return
     */
    private Table createOtherTeamArea(Color color) {
        Table area = new Table();
        FontManager fontManager = FontManager.getInstance();
        Drawable background = new TextureRegionDrawable(new Texture("tablev2.png")).tint(color);
        area.background(background);

        // Set up the label for the left player
        leftPlayerLabel = new Label("Left Player", new LabelStyle(fontManager.getBoldFont28(), Color.WHITE));
        Optional<String> leftPlayerNameOpt = PlayerManager.getInstance().getPlayerNameByRole("LeftPlayer");
        String leftPlayerName = leftPlayerNameOpt.orElse("Left Player");
        leftPlayerLabel.setText(leftPlayerName);

        // Add the label to the area, make it span across all columns intended for slots below it
        area.add(leftPlayerLabel).expandX().fillX().align(Align.left).padTop(0.001f * worldHeight).padLeft(0.04f * worldWidth).colspan(2).row();

        // Create a texture for slot backgrounds
        Pixmap pixmap = new Pixmap(100, 140, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.95f, 0.95f, 0.95f, 0.15f);  // Light grey, semi-transparent
        pixmap.fill();
        Texture slotTexture = new Texture(pixmap);
        pixmap.dispose();

        Drawable slotBackground = new TextureRegionDrawable(new TextureRegion(slotTexture));

        // Adding slots and organizing them into 2x3 grid
        for (int i = 0; i < 6; i++) {
            Slot slot = new Slot(slotBackground, "LeftSlot" + i);
            gameStateManager.addSlot("LeftSlot" + i, slot);
            cardSlotsOtherTeam.add(slot);
            float slotWidth = 0.156f; // Relative width
            float slotHeight = 0.139f; // Relative height
            int slotWidthPixels = (int)(worldWidth * slotWidth);
            int slotHeightPixels = (int)(worldHeight * slotHeight);
            area.add(slot).size(slotWidthPixels, slotHeightPixels).pad(0.02f * worldWidth);
            if ((i + 1) % 2 == 0) area.row();  // Ensure a new row is started every two slots

            // Listener is added for each slot.
            slot.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if (!slot.isOccupied()) {
                        System.out.println("Card dropped on slot: " + slot.getSlotName());
                    }
                }
            });

        }

        return area;
    }



    /**
     * Creates the RightPlayer team cards area with card slots for up to six cards which are placed 2x3.
     * @param color
     * @return
     */
    private Table createYourTeamArea(Color color) {
        FontManager fontManager = FontManager.getInstance();
        Table area = new Table();
        Drawable background = new TextureRegionDrawable(new Texture("tablev2.png")).tint(color);
        area.background(background);
        rightPlayerLabel = new Label("Right Player", new LabelStyle(fontManager.getBoldFont28(), Color.WHITE));

        Optional<String> rightPlayerNameOpt = PlayerManager.getInstance().getPlayerNameByRole("RightPlayer");

        String rightPlayerName = rightPlayerNameOpt.orElse("Right Player");
        rightPlayerLabel.setText(rightPlayerName);
        float topPad = 0.009f * worldHeight;
        float leftPad = 0.005f * worldWidth;
        area.add(rightPlayerLabel).expandX().fillX().align(Align.left).padTop(0.001f * worldHeight).padLeft(0.04f * worldWidth).colspan(2).row();

        // Create a texture for slot backgrounds
        Pixmap pixmap = new Pixmap(100, 140, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.95f, 0.95f, 0.95f, 0.2f);  // Light grey, semi-transparent
        pixmap.fill();
        Texture slotTexture = new Texture(pixmap);
        pixmap.dispose();

        Drawable slotBackground = new TextureRegionDrawable(new TextureRegion(slotTexture));

        // Adding slots and organizing them into 2x3 grid
        for (int i = 0; i < 6; i++) {
            Slot slot = new Slot(slotBackground, "RightSlot" + i);
            gameStateManager.addSlot("RightSlot" + i, slot);
            cardSlotsMyTeam.add(slot);
            float slotWidth = 0.156f; // Relative width
            float slotHeight = 0.139f; // Relative height
            int slotWidthPixels = (int)(worldWidth * slotWidth);
            int slotHeightPixels = (int)(worldHeight * slotHeight);
            area.add(slot).size(slotWidthPixels, slotHeightPixels).pad(0.02f * worldWidth);
            if ((i + 1) % 2 == 0) area.row();  // Ensure a new row is started every two slots
        }

        return area;
    }

    /**
     * Creates personal cards area for three cards with 3 slots.
     * @param color
     * @return
     */
    private Table createPersonalAreaWithSlots(Color color) {
        FontManager fontManager = FontManager.getInstance();
        Table area = new Table();
        Drawable background = new TextureRegionDrawable(new Texture("tablev2.png")).tint(color);
        area.background(background);

        Label leftPlayerLabel = new Label("Sinu valikud", new LabelStyle(fontManager.getFontOpenSansRegular16(), Color.WHITE));
        float padLeft = 0.01f * worldWidth;
        area.add(leftPlayerLabel).align(Align.topLeft).padTop(0).padLeft(padLeft).expandX().row();

        // Assuming 3 slots for simplicity, adjust as needed
        for (int i = 0; i < 3; i++) {
            Table cardSlotMyPersonal = new Table();
            cardSlotsMyPersonal.add(cardSlotMyPersonal);

            int borderWidth = 2;
            float slotWidth = 0.156f; // Relative width
            float slotHeight = 0.139f; // Relative height

            // Convert to pixel dimensions
            int slotWidthPixels = (int)(worldWidth * slotWidth);
            int slotHeightPixels = (int)(worldHeight * slotHeight);

            Pixmap pixmap = new Pixmap(slotWidthPixels + borderWidth * 2, slotHeightPixels + borderWidth * 2, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.95f, 0.95f, 0.95f, 0.4f);
            pixmap.fillRectangle(0, 0, slotWidthPixels + borderWidth * 2, borderWidth);
            pixmap.fillRectangle(0, 0, borderWidth, slotHeightPixels + borderWidth * 2);
            pixmap.fillRectangle(slotWidthPixels + borderWidth, 0, borderWidth, slotHeightPixels + borderWidth * 2);
            pixmap.fillRectangle(0, slotHeightPixels + borderWidth, slotWidthPixels + borderWidth * 2, borderWidth);

            Texture texture = new Texture(pixmap);
            pixmap.dispose();

            TextureRegionDrawable slotBackground = new TextureRegionDrawable(new TextureRegion(texture));
            cardSlotMyPersonal.background(slotBackground);

            // Add each slot to the area in a single row, and add padding between slots
            float paddingTopBottom = worldHeight * (0.028f); // Calculate top and bottom padding
            float paddingLeftRight = worldWidth * (0.015f);
            area.add(cardSlotMyPersonal).size(slotWidthPixels, slotHeightPixels).pad(paddingTopBottom, paddingLeftRight, paddingTopBottom, paddingLeftRight);
        }
        area.row(); // Call this once after all slots are added to finalize the row

        return area;
    }

    /**
     Populates the PersonalArea with 3 new cards in the beginning of every round and also removes the cards
     that the player did not choose after a round has ended.
     * @param personalArea
     * @param currentPlayerId
     */
    private void populateSlotsWithCards(Table personalArea, int currentPlayerId) {
        if (Objects.equals(playerRole, "LeftPlayer") || Objects.equals(playerRole, "RightPlayer")) {
            List<Card> personalCards = Card.getPersonalCards(currentPlayerId);
            float slotWidth = 0.156f; // Relative width
            float slotHeight = 0.139f; // Relative height

            // Convert to pixel dimensions
            int slotWidthPixels = (int)(worldWidth * slotWidth);
            int slotHeightPixels = (int)(worldHeight * slotHeight);

            // Iterate over the slots and populate them with cards
            int index = 0; // Keep track of the card index
            for (Actor actor : personalArea.getChildren()) {
                if (actor instanceof Table && index < personalCards.size()) {
                    Table slot = (Table) actor;
                    Card card = personalCards.get(index++);
                    slot.clearChildren(); // Clear any previous card
                    slot.add(card).size(slotWidthPixels, slotHeightPixels);// Adjust size as needed
                }
            }
        }
    }


    /**
     * Refreshes the team areas based on the card ID-s and location information received from the server.
     *
     * PersonalArea cards are excluded, i.e. they are not being updated (y-axis is under 360).
     * @param positionInfoMap
     */
    public void refreshGameBoard(Map<Integer, PacketCardPositionInfo> positionInfoMap) {
        System.out.println("Refreshing game board...");
        System.out.println("Current Round: " + gameStateManager.getCurrentRound());
        System.out.println("Received position info map size: " + positionInfoMap.size());

        positionInfoMap.forEach((id, info) -> {
            System.out.println("Processing card with ID: " + id + " at X: " + info.x + ", Y: " + info.y);

            // Convert global coordinates to local coordinates based on the current screen size
            float localX = (info.x / 1920f) * worldWidth;
            float localY = (info.y / 1080f) * worldHeight;

            // Skip cards that are positioned too low on the screen
            if (localY < worldHeight * 0.333f) {
                System.out.println("Skipping card with ID: " + id + " due to low Y position: " + localY);
                return; // Skip the rest of this iteration
            }

            // Attempt to find an existing card by ID
            Card card = findCardById(id);
            if (card == null) {
                // Depending on the round, attempt to read the card data from a JSON file
                if (gameStateManager.getCurrentRound() == 1 || gameStateManager.getCurrentRound() == 2) {
                    card = readCardFromJSONById("PositiveCardsJson.json", currentPlayerId, id);
                } else if (gameStateManager.getCurrentRound() == 3) {
                    card = readCardFromJSONById("NegativeCardsJson.json", currentPlayerId, id);
                } else if (gameStateManager.getCurrentRound() == 4 || gameStateManager.getCurrentRound() == 5) {
                    card = readCardFromModifierJSONById("ModifierCardsJson.json", currentPlayerId, id);
                }
            }

            if (card != null) {
                Card finalCard = card; // Make card effectively final to use inside lambda
                Gdx.app.postRunnable(() -> {
                    // Add the card to the stage on the main thread
                    System.out.println("Adding card with ID: " + id + " to stage at X: " + localX + ", Y: " + localY);
                    stage.addActor(finalCard);
                    finalCard.setPosition(localX, localY);
                });
            } else {
                System.out.println("No card found or created for ID: " + id);
            }
        });
    }


    /**
     * Finds the cards based on their ID-s in the JSON.
     * @param id
     * @return
     */
    private Card findCardById(int id) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Card && ((Card) actor).getCardID() == id) {
                return (Card) actor;
            }
        }
        return null; // No card found with the given ID
    }


    // The getters are currently not being used as the locations are based on the coordinates.
    // However, they might prove necessary in the 4th and 5th round.
    public Table getOtherTeamArea() {
        return otherTeamArea;
    }

    public Table getMyTeamArea2() {
        return myTeamArea;
    }

    public Table getPersonalArea() {
        return personalArea;
    }
    public List<Table> getCardSlotsMyTeam() {
        return cardSlotsMyTeam;
    }

    public List<Table> getCardSlotsOtherTeam() {
        return cardSlotsOtherTeam;
    }

    public List<Table> getCardSlotsMyPersonal() {
        return cardSlotsMyPersonal;
    }
    public Vector2 getGlobalSlotPosition(Slot slot) {
        Vector2 localOrigin = new Vector2(0, 0); // Local origin of the slot
        return slot.localToStageCoordinates(localOrigin); // Convert to global stage coordinates
    }


    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.draw(boardTexture, 0, 0, worldWidth, worldHeight);
        for (Slot slot : GameStateManager.getInstance().getAllSlots().values()) {
            Vector2 globalPos = getGlobalSlotPosition(slot);
            font.draw(spriteBatch, "Slot: " + slot.getSlotName() + " (X: " + globalPos.x + ", Y: " + globalPos.y + ")",
                    globalPos.x, globalPos.y + 20);
        }
        spriteBatch.end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        hud.stage.act(Math.min(delta, 1.0f / 30f));
        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(worldWidth / 2, worldHeight / 2, 0); // Re-center the camera
        updateSlotPositions();
    }


    public void dispose() {
        stage.dispose();
        spriteBatch.dispose();
        boardTexture.dispose();
        GameClient.getInstance().dispose();
        GameStateManager.getInstance().setGameBoardScreen(null);
        hud.dispose();
    }


    public void updatePlayerLabels() {
        Optional<String> rightPlayerName = PlayerManager.getInstance().getPlayerNameByRole("RightPlayer");
        Optional<String> leftPlayerName = PlayerManager.getInstance().getPlayerNameByRole("LeftPlayer");

        rightPlayerLabel.setText(rightPlayerName.orElse("Parem"));
        leftPlayerLabel.setText(leftPlayerName.orElse("Vasak"));
    }

    private void updateSlotPositions() {
        // Ensure that all slots' positions are updated
        for (Slot slot : gameStateManager.getAllSlots().values()) {
            slot.updateGlobalPosition();
        }

        // Print updated positions for all slots
        System.out.println("All updated slot positions:");
        for (Map.Entry<String, Slot> entry : gameStateManager.getAllSlots().entrySet()) {
            Slot slot = entry.getValue();
            System.out.printf("Slot Name: %s, Global X: %.2f, Global Y: %.2f\n", entry.getKey(), slot.getGlobalX(), slot.getGlobalY());
        }
    }


    public void scheduleSlotPositionUpdate() {
        stage.addAction(Actions.delay(0.1f, Actions.run(new Runnable() {
            @Override
            public void run() {
                updateSlotPositions();
                System.out.println("Updated slot positions after stage setup.");
            }
        })));
    }



}
