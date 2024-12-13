package ee.taltech.okgame.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryonet.Client;
import ee.taltech.okgame.*;
import ee.taltech.okgame.lobby.Lobby;
import ee.taltech.okgame.lobby.LobbyManager;
import ee.taltech.okgame.lobby.LobbyUpdateListener;
import ee.taltech.okgame.packet.PacketCreateLobbyRequest;
import ee.taltech.okgame.packet.PacketIsPlayPressed;
import ee.taltech.okgame.packet.PacketJoinLobbyRequest;
import java.util.List;

public class LobbyScreen extends ApplicationAdapter implements Screen, LobbyUpdateListener, GameEventListener {
    private OKGame game;
    private GameStateManager gameStateManager;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private GameClient client;  // Ensure you have this for sending packets

    private Texture backgroundTexture;
    private Texture createButtonTexture;
    private Texture joinButtonTexture;

    private ScrollPane lobbyScrollPane;  // To display lobbies in a scrollable list
    private Table lobbyListTable;  // For listing lobbies


    private Table leftLobbies;
    private Table rightLobbies;
    private Label createGameButtonText;
    private Texture createGameButtonTexture;
    private Texture createGameButtonTextTexture;
    private Image createGameButton;
    private int playerId;
    private String playerRole;
    private String playerName;


    public LobbyScreen(OKGame game, String playerName) {
        this.game = game;
        this.client = GameClient.getInstance();
        this.gameStateManager = GameStateManager.getInstance();
        GameClient.getInstance().registerListener(this);
        initializeUI();
        LobbyManager.getInstance().addListener(this);

    }

    private void initializeUI() {
        float worldWidth = Gdx.graphics.getWidth();
        float worldHeight = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.position.set(worldWidth / 2, worldHeight / 2, 0);
        viewport = new FitViewport(worldWidth, worldHeight, camera);
        stage = new Stage(viewport);

        backgroundTexture = new Texture(Gdx.files.internal("LobbyBackground.png"));
        createButtonTexture = new Texture(Gdx.files.internal("button12.png"));
        joinButtonTexture = new Texture(Gdx.files.internal("button03.png"));

        stage.addActor(new Image(backgroundTexture));


        setupLobbyAreas(worldWidth, worldHeight);

        Gdx.input.setInputProcessor(stage);
    }


    private void setupLobbyAreas(float screenWidth, float screenHeight) {
        // Individual margins for more precise control
        float marginLeftLeft = 30; // Margin on the left side for the left column of lobbies
        float marginRightLeft = 380; // Margin on the right side for the left column of lobbies (middle margin)
        float marginLeftRight = 380; // Margin on the left side for the right column of lobbies (middle margin)
        float marginRightRight = 30; // Margin on the right side for the right column of lobbies
        float marginTop = 20; // Margin on the top of the screen
        float marginBottom = 20; // Margin on the bottom of the screen
        float marginBetweenLobbies = 20; // Vertical margin between lobbies

        // Calculate the widths of the columns accounting for their respective margins
        float columnWidthLeft = (screenWidth - marginLeftLeft - marginRightLeft - marginLeftRight - marginRightRight) / 2;
        float columnWidthRight = columnWidthLeft; // Symmetrical columns in terms of width
        float lobbyHeight = (screenHeight - marginTop - marginBottom - 2 * marginBetweenLobbies) / 3;

        // Create a pixmap for background with semi-transparent white color
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 0.5f);
        pixmap.fill();
        TextureRegionDrawable background = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        // Create separate tables for each lobby to act as boxes
        Table[] leftLobbies = new Table[3];
        Table[] rightLobbies = new Table[3];

        for (int i = 0; i < 3; i++) {
            leftLobbies[i] = new Table();
            leftLobbies[i].setName("GameLobby" + (i + 1));
            leftLobbies[i].setBackground(background);
            leftLobbies[i].setSize(columnWidthLeft, lobbyHeight);
            leftLobbies[i].setPosition(marginLeftLeft, screenHeight - marginTop - (i + 1) * (lobbyHeight + marginBetweenLobbies) + marginBetweenLobbies);

            rightLobbies[i] = new Table();
            rightLobbies[i].setName("GameLobby" + (i + 4));
            rightLobbies[i].setBackground(background);
            rightLobbies[i].setSize(columnWidthRight, lobbyHeight);
            rightLobbies[i].setPosition(screenWidth - columnWidthRight - marginRightRight, screenHeight - marginTop - (i + 1) * (lobbyHeight + marginBetweenLobbies) + marginBetweenLobbies);

            Label leftLobbyLabel = new Label("Game " + (i + 1), new Label.LabelStyle(FontManager.getInstance().getBoldFont20(), Color.WHITE));
            leftLobbies[i].add(leftLobbyLabel).expand().top().left().pad(10);

            Label rightLobbyLabel = new Label("Mäng " + (i + 4), new Label.LabelStyle(FontManager.getInstance().getBoldFont20(), Color.WHITE));
            rightLobbies[i].add(rightLobbyLabel).expand().top().left().pad(10);
            stage.addActor(leftLobbies[i]);
            stage.addActor(rightLobbies[i]);

            addCreateGameButton(leftLobbies[i], "Game" + (i + 1));
            addJoinLobbyButton(leftLobbies[i], "Game" + (i + 1));
            addCreateGameButton(rightLobbies[i], "Game" + (i + 4));
            addJoinLobbyButton(rightLobbies[i], "Game" + (i + 4));
        }
    }


    private void addCreateGameButton(Table lobbyTable, String lobbyName) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Texture createGameButtonTexture = new Texture(Gdx.files.internal("Button09.png"));
                Image createGameButton = new Image(createGameButtonTexture);
                createGameButton.setScaling(Scaling.fit);
                Label createGameButtonText = new Label("Loo mäng", new Label.LabelStyle(FontManager.getInstance().getButtonFont24(), Color.WHITE));
                Stack buttonStack = new Stack();
                buttonStack.setName("createButtonStack"); // Set name to the stack
                createGameButton.setName("createGameButton"); // Set name to the button itself
                buttonStack.add(createGameButton);
                buttonStack.add(createGameButtonText);
                createGameButtonText.setAlignment(Align.center);

                buttonStack.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        showCreateLobbyDialog(lobbyName);
                    }
                });


                lobbyTable.row();
                lobbyTable.add(buttonStack).size(createGameButtonTexture.getWidth(), createGameButtonTexture.getHeight()).expand().center().pad(10);
            }
        });
    }


    private void showLobbyDialog(String lobbyName, List<String> playerNames) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog lobbyDialog = new Dialog(lobbyName, skin) {
            // Override the result method if needed or handle the button within the dialog
            protected void result(Object object) {
                System.out.println("Player exits lobby: " + lobbyName);
                // Add logic to handle leaving the lobby
            }
        };

        // Set the dialog as modal to block input to other UI elements
        lobbyDialog.setModal(true);
        lobbyDialog.setMovable(false);  // Optional: make the dialog non-movable

        // Add player names to the dialog
        for (String player : playerNames) {
            lobbyDialog.getContentTable().add(new Label(player, skin)).expand().fill().center().pad(10);
            lobbyDialog.getContentTable().row();
        }

        // Add the exit button
        TextButton exitButton = new TextButton("Lahku", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                lobbyDialog.hide();
                // Additional logic to actually handle lobby exit
            }
        });
        lobbyDialog.button(exitButton);

        // Define the size of the dialog explicitly if you want it larger
        lobbyDialog.setSize(400, 300); // Adjust size as needed
        lobbyDialog.setPosition((float) Gdx.graphics.getWidth() / 2 - 200, (float) Gdx.graphics.getHeight() / 2 - 150); // Center the dialog

        // Show the dialog on the stage
        lobbyDialog.show(stage);
    }

    private void updateLobbyDialog(Dialog lobbyDialog, List<String> updatedPlayerNames, Skin skin) {
        lobbyDialog.getContentTable().clear();
        for (String player : updatedPlayerNames) {
            lobbyDialog.getContentTable().add(new Label(player, skin)).expand().fill().center().pad(10);
            lobbyDialog.getContentTable().row();
        }
        // You might need to refresh or redraw the dialog, depending on how you implement it
        lobbyDialog.invalidateHierarchy();  // Redraw the dialog content
    }


    private void displayPlayers(Table lobbyTable, List<String> playerNames) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (lobbyTable.getChildren().size > 1) {
                    lobbyTable.clearChildren(true);
                    lobbyTable.add(lobbyTable.getChildren().first()).row();
                }
                for (String name : playerNames) {
                    Label playerNameLabel = new Label(name, new Label.LabelStyle(FontManager.getInstance().getBoldFont20(), Color.WHITE));
                    lobbyTable.add(playerNameLabel).expand().fill().top().left().pad(10).row();
                }
            }
        });
    }

    private void addJoinLobbyButton(Table lobbyTable, String lobbyName) {


        Texture joinGameButtonTexture = new Texture(Gdx.files.internal("Button09.png"));
        Image joinGameButton = new Image(joinGameButtonTexture);
        joinGameButton.setScaling(Scaling.fit);  // Ensure the image scales correctly

        // Create the label for the button
        Label joinGameButtonText = new Label("Liitu", new Label.LabelStyle(FontManager.getInstance().getButtonFont24(), Color.WHITE));

        // Stack to hold the image and the label
        Stack joinButtonStack = new Stack();
        joinButtonStack.setName("joinButtonStack");
        joinGameButton.setName("joinGameButton");

        joinButtonStack.add(joinGameButton);  // Add the image first
        joinButtonStack.add(joinGameButtonText);  // Add the label on top of the image

        // Align the label to be centered within the stack
        joinGameButtonText.setAlignment(Align.center);


        joinButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Join button for " + lobbyName + " clicked");
                sendJoinLobbyRequest(lobbyName);

                // Optional: Code to handle joining the lobby
            }
        });

        // Assuming we want this button after the player names
        lobbyTable.row();
        lobbyTable.add(joinButtonStack).expand().center().pad(0);
    }




    private void sendJoinLobbyRequest(String lobbyName) {
        PacketJoinLobbyRequest packet = new PacketJoinLobbyRequest();
        packet.lobbyName = lobbyName;
        GameClient.getInstance().sendTCP(packet);  // Assuming 'gameClient' is your client network connection
    }




    private void temporaryRefreshLobbyContent(Table lobbyTable, String lobbyName) {
        // Clear all existing content to start fresh
        lobbyTable.clearChildren();
        addJoinLobbyButton(lobbyTable, lobbyName);

        // Add a label for the lobby name for clarity
        lobbyTable.add(new Label(lobbyName, new Label.LabelStyle(FontManager.getInstance().getBoldFont20(), Color.WHITE))).colspan(2).center().pad(10);
        lobbyTable.row();  // Start a new row for buttons

        addCreateGameButton(lobbyTable, lobbyName);
        lobbyTable.add().width(10);  // Add some horizontal spacing between buttons



        // Adjust the layout further if needed
        lobbyTable.row();  // Ensure any additional content starts on a new row
        lobbyTable.pack();  // Pack the table to its contents
    }







    private void showCreateLobbyDialog(String lobbyName) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Create a new dialog and set it up with a title and skin
        final Dialog dialog = new Dialog("Loo mäng: " + lobbyName, skin) {
            @Override
            protected void result(Object object) {
                boolean createLobby = (Boolean) object;
                if (createLobby) {
                    CheckBox aiPlayerCheckbox = (CheckBox) findActor("aiPlayer");
                    CheckBox aiDeciderCheckbox = (CheckBox) findActor("aiDecider");

                    boolean isAiPlayer = aiPlayerCheckbox.isChecked();
                    boolean isAiDecider = aiDeciderCheckbox.isChecked();

                    notifyLobbyCreation(lobbyName, isAiPlayer, isAiDecider); // Notify the server of the new lobby creation
                } else {
                    System.out.println("Lobby creation cancelled.");
                }
            }
        };

        // Dialog content setup
        String dialogText = "Loo mäng, millega saavad loomise järel liituda teised mängijad. Igas mängus on kolm osalejat. " +
                "Kui soovid, saad ühe nendest asendada AI mängijaga, selleks vajuta enne LOO MÄNG nupule vajutamist AI mängija. " +
                "Kui mänguga on liitunud vajalik arv mängijaid, mäng algab.";
        Label descriptionLabel = new Label(dialogText, skin);
        descriptionLabel.setWrap(true);
        dialog.getContentTable().add(descriptionLabel).width(400).pad(10);
        dialog.getContentTable().row();

        // Checkbox for AI player option, with a name for easy retrieval
        CheckBox aiPlayerCheckbox = new CheckBox(" Lisa mängu AI mängija", skin);
        aiPlayerCheckbox.setName("aiPlayer");
        dialog.getContentTable().add(aiPlayerCheckbox).left().padTop(10).row();

        // Checkbox for AI decision maker option, with a name for easy retrieval
        CheckBox aiDeciderCheckbox = new CheckBox(" Lisa mängu AI otsustaja", skin);
        aiDeciderCheckbox.setName("aiDecider");
        dialog.getContentTable().add(aiDeciderCheckbox).left().padTop(10).row();

        // Adding buttons for actions
        dialog.button("Create Lobby", true);  // Object passed to result method indicates creating the lobby
        dialog.button("Cancel", false);  // Object passed to result method indicates cancelling the lobby creation

        // Show the dialog on the stage
        dialog.show(stage);


    }

    private void notifyLobbyCreation(String lobbyName, boolean isAiPlayer, boolean isAiDecider) {
        System.out.println("Creating lobby: " + lobbyName + " with AI player: " + isAiPlayer + ", AI decider: " + isAiDecider);
        PacketCreateLobbyRequest packet = new PacketCreateLobbyRequest(lobbyName, isAiPlayer, isAiDecider);
        GameClient.getInstance().sendTCP(packet);
    }


    @Override
    public void show() {
        LobbyManager.getInstance().addListener(this);
        // Initialization code...
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        LobbyManager.getInstance().removeListener(this);
    }


    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        createButtonTexture.dispose();
        joinButtonTexture.dispose();
    }

    private void addLobbyContent(Table lobbyTable, String lobbyName) {
        // Adding lobby specific UI elements
        if (LobbyManager.getInstance().lobbyExists(lobbyName)) {
            List<String> playerNames = LobbyManager.getInstance().getPlayersInLobby(lobbyName);
            displayPlayers(lobbyTable, playerNames);
            addJoinLobbyButton(lobbyTable, lobbyName);
        } else {
            addCreateGameButton(lobbyTable, lobbyName);
        }
    }
    @Override
    public void onLobbyUpdate() {
        // Implementation code goes here.
        // For example, refresh the lobby list or handle lobby state changes.
        System.out.println("Lobby has been updated.");
        // Possibly re-fetch lobby data or update the UI to reflect new state
    }

    @Override
    public void onGameReady() {
        System.out.println("Mäng algab.");
        // Transition to the game board screen
        Gdx.app.postRunnable(() -> game.setScreen(new GameBoardScreen(game, this.playerId, this.playerRole)));
    }

    @Override
    public void onPlayerIdReceived(int playerId, String playerRole, String playerName) {
        this.playerId = playerId;
        this.playerRole = playerRole;
        this.playerName = playerName;
        // Additional handling if needed
    }
}
