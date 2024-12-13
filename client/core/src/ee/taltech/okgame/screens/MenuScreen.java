package ee.taltech.okgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.ScreenAdapter;
import ee.taltech.okgame.FontManager;
import ee.taltech.okgame.GameClient;
import ee.taltech.okgame.OKGame;
import ee.taltech.okgame.PlayerIdReceivedListener;
import ee.taltech.okgame.packet.PacketIsPlayPressed;
import ee.taltech.okgame.packet.PacketMoveToLobbyPressed;

import java.util.logging.Logger;


public class MenuScreen extends ScreenAdapter implements PlayerIdReceivedListener {
    private OKGame game;
    private Stage stage;
    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Texture playButtonTexture;
    private Texture settingsButtonTexture;
    private Texture playButtonTextTexture;
    private Texture settingsButtonTextTexture;
    private GameClient gameClient;
    private Label settingsButtonText;
    private Label playButtonText;
    Logger logger = Logger.getLogger(getClass().getName());

    private int playerId = -1;

    private String playerRole;
    private String playerNamey;

    private PlayerIdReceivedListener playerIdReceivedListener;

    public MenuScreen(OKGame game)  {
        this.game = game;
        this.gameClient = GameClient.getInstance();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        gameClient.setPlayerIdReceivedListener(this);
        FontManager fontManager = FontManager.getInstance();


        backgroundTexture = new Texture("MenuScreenBackground.jpg");
        playButtonTexture = new Texture("button05.png");
        settingsButtonTexture = new Texture("button02.png");

        Image playButton = new Image(playButtonTexture);
        Image settingsButton = new Image(settingsButtonTexture);
        Label playButtonText = new Label("Alusta mängu", new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));
        Label settingsButtonText = new Label("Seaded", new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));

        Stack playButtonStack = new Stack();
        Stack settingsButtonStack = new Stack();

        // Add components to stacks
        playButtonStack.add(playButton);
        playButtonStack.add(playButtonText);
        settingsButtonStack.add(settingsButton);
        settingsButtonStack.add(settingsButtonText);

        // Center the text within the stack
        playButtonText.setAlignment(Align.center);
        settingsButtonText.setAlignment(Align.center);

        // Size the stacks to the largest element (optional, depends on desired layout)
        playButtonStack.setSize(playButton.getWidth(), playButton.getHeight());
        settingsButtonStack.setSize(settingsButton.getWidth(), settingsButton.getHeight());

        float centerY = Gdx.graphics.getHeight() / 2f;
        int margin = 100;
        int screenWidth = Gdx.graphics.getWidth();

        settingsButtonStack.setPosition(screenWidth - settingsButtonStack.getWidth() - margin, centerY - settingsButtonStack.getHeight() - 20);
        playButtonStack.setPosition(screenWidth - playButtonStack.getWidth() - margin, centerY + 20);

        // Add listeners to stacks
        playButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showDialogForNameEntry();
            }
        });

        settingsButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameClient.sendTCP("Settings button pressed");
            }
        });

        // Add stacks to stage
        stage.addActor(playButtonStack);
        stage.addActor(settingsButtonStack);
    }

    private void showDialogForNameEntry() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json")); // Ensure you have this skin
        final Dialog dialog = new Dialog("Enter Name", skin) {
            protected void result(Object object) {
                if ((Boolean) object) {
                    TextField nameField = (TextField) findActor("nameField");
                    String playerName = nameField.getText();
                    moveToLobbyScreen(playerName);  // Move to lobby screen immediately after getting the name
                }
            }
        };

        final TextField nameField = new TextField("", skin);
        nameField.setName("nameField");
        dialog.getContentTable().add("Your name: ");
        dialog.getContentTable().add(nameField).pad(10);
        dialog.getContentTable().row(); // Move to the next row in the dialog

        // Add checkboxes
        final CheckBox aiPlayerCheckbox = new CheckBox(" AI mängija", skin);
        aiPlayerCheckbox.setName("aiPlayer");
        dialog.getContentTable().add(aiPlayerCheckbox).colspan(2).left().padTop(10);

        dialog.getContentTable().row(); // Move to the next row in the dialog

        final CheckBox aiDeciderCheckbox = new CheckBox(" AI otsustaja", skin);
        aiDeciderCheckbox.setName("aiDecider");
        dialog.getContentTable().add(aiDeciderCheckbox).colspan(2).left().padTop(10);

        dialog.button("Mängima", true); // Pass 'true' as the result for the 'Mängima' button
        dialog.button("Cancel", false); // Pass 'false' as the result for the 'Cancel' button
        dialog.show(stage);
    }


    private void moveToLobbyScreen(String playerName) {
        PacketMoveToLobbyPressed packet = new PacketMoveToLobbyPressed (playerName);
        packet.playPressed = true;
        GameClient.getInstance().playBackgroundMusic();
        packet.playerName = playerName;
        gameClient.sendTCP(packet); // Send the packet to the server with the AI data
        game.setScreen(new LobbyScreen(game, playerName));
    }



    @Override
    public void onPlayerIdReceived(int playerId, String playerRole, String playerName) {
        logger.info("\n--- I received playerID");
        this.playerId = playerId;
        this.playerRole = playerRole;
        this.playerNamey = playerName;
        // Use Gdx.app.postRunnable to switch screens on the main thread
        Gdx.app.postRunnable(() -> game.setScreen(new GameBoardScreen(game, playerId, playerRole)));
    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void hide() {
        // Dispose of the stage and batch, which you're already doing
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();

        // Dispose of textures
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (playButtonTexture != null) playButtonTexture.dispose();
        if (settingsButtonTexture != null) settingsButtonTexture.dispose();
        if (playButtonTextTexture != null) playButtonTextTexture.dispose();
        if (settingsButtonTextTexture != null) settingsButtonTextTexture.dispose();
    }

}
