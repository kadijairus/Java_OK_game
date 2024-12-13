package ee.taltech.okgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ee.taltech.okgame.packet.*;
import ee.taltech.okgame.GameClient;
import ee.taltech.okgame.players.Player;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import ee.taltech.okgame.packet.PacketEndTurnPressed;
import ee.taltech.okgame.packet.PacketPlayerExitMessage;

import java.text.DecimalFormat;


public class Hud implements GameClient.ExitAcknowledgementListener, RoundNumberChangeListener, GameClient.LeftWinListener, GameClient.RightWinListener, GameClient.PlayerExitListener {
    private final float worldWidth;
    private final float worldHeight;
    public Stage stage;
    private Viewport viewport;

    private Image exitButton;
    private Image endTurnButton;
    private Label exitButtonText;
    private Image leftPlayerButton;
    private Image rightPlayerButton;
    private Label endTurnButtonText;
    private Texture exitButtonTexture;
    private Texture endTurnButtonTexture;
    private Texture exitButtonTextTexture;
    private Texture endTurnButtonTextTexture;
    private Texture leftPlayerButtonTexture;
    private Texture rightPlayerButtonTexture;
    private GameClient gameClient;
    private int currentPlayerId;
    private Player currentPlayer;
    private String playerRole;
    private Label exitLabel;
    private Label rightPlayerLabel;
    private Label leftPlayerLabel;
    private FontManager fontManagery;
    private GameStateManager gameStateManager;
    private Dialog rulesDialog;
    private static final String RIGHT_PLAYER = "RightPlayer";
    private static final String LEFT_PLAYER = "LeftPlayer";
    private Stack exitButtonStack;
    private Stack endTurnButtonStack;
    private Stack leftPlayerButtonStack;
    private Stack rightPlayerButtonStack;



    // Constructor
    public Hud(int currentPlayerId, String playerRole) {
        gameStateManager = GameStateManager.getInstance();
        this.currentPlayer = PlayerManager.getInstance().getSelf();
        Vector2 screenSize = gameStateManager.getCurrentScreenSize();
        this.worldWidth = screenSize.x;
        this.worldHeight = screenSize.y;
        viewport = new FitViewport(worldWidth, worldHeight);
        stage = new Stage(viewport);
        gameClient = GameClient.getInstance();
        setupHudForRole();
        GameStateManager.getInstance().addRoundChangeListener(this);

        gameClient.setExitAcknowledgementListener(this);
        gameClient.setLeftWinListener(this);
        gameClient.setRightWinListener(this);
        gameClient.setPlayerExitListener(this);

    }



    private void setupHudForRole() {
        String role = currentPlayer.getPlayerRole();
        Gdx.app.log("HUD Setup", "Player role: " + role);
        int currentRound = gameStateManager.getCurrentRound();
        Gdx.app.log("HUD Setup", "Player role: " + role); // Add this log to check role recognition
        switch (role) {
            case LEFT_PLAYER:
            case RIGHT_PLAYER:
                setupStandardButtons();
                showRulesDialogStandard(currentRound);
                break;
            case "Decider":
                setupDeciderBackground();
                setupDeciderButtons();
                showRulesDialogDecider(currentRound);
                break;
            default:
                Gdx.app.error("HUD Setup", "Unknown player role: " + role);
                setupStandardButtons(); // Maybe default to a safe configuration
                break;
        }
    }

    private void setupStandardButtons() {
        loadTexturesStandard();
        createStandardButtons();
        setupButtonPositionsStandard();
        addListenersToStandardButtons();
    }

    private void setupDeciderButtons() {
        // Setup buttons and other elements specifically for Decider
        loadTexturesDecider();
        createDeciderButtons();
        setupButtonPositionsDecider();
        addListenersToDeciderButtons();

    }


    private void loadTexturesStandard() {
        exitButtonTexture = new Texture(Gdx.files.internal("button12.png"));
        endTurnButtonTexture = new Texture(Gdx.files.internal("button13.png"));
        exitButtonTextTexture = new Texture(Gdx.files.internal("TxtQuit.png"));
        endTurnButtonTextTexture = new Texture(Gdx.files.internal("TxtPlay.png"));
    }

    private void loadTexturesDecider() {
        exitButtonTexture = new Texture(Gdx.files.internal("button12.png"));
        exitButtonTextTexture = new Texture(Gdx.files.internal("TxtQuit.png"));
        leftPlayerButtonTexture = new Texture(Gdx.files.internal("button03.png"));
        rightPlayerButtonTexture = new Texture(Gdx.files.internal("button07.png"));

    }


    private void createStandardButtons() {
        FontManager fontManager = FontManager.getInstance();

        exitButtonStack = new Stack();  // Ensure this line executes without any conditions
        exitButton = new Image(exitButtonTexture);
        exitButtonText = new Label("Lahku", new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));
        exitButtonStack.add(exitButton);
        exitButtonStack.add(exitButtonText);
        exitButtonText.setAlignment(Align.center);
        exitButtonStack.setSize(exitButton.getWidth(), exitButton.getHeight());


        // Similar for endTurnButtonStack...
        endTurnButtonStack = new Stack();
        endTurnButton = new Image(endTurnButtonTexture);
        endTurnButtonText = new Label("Valmis", new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));
        endTurnButtonStack.add(endTurnButton);
        endTurnButtonStack.add(endTurnButtonText);
        endTurnButtonText.setAlignment(Align.center);
        endTurnButtonStack.setSize(endTurnButton.getWidth(), endTurnButton.getHeight());

        stage.addActor(exitButtonStack);
        stage.addActor(endTurnButtonStack);
    }


    private void createDeciderButtons() {
        FontManager fontManager = FontManager.getInstance();

        // Ensure these are not being redeclared locally
        exitButtonStack = new Stack();
        exitButton = new Image(exitButtonTexture);
        exitLabel = new Label("Lahku", new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));
        exitButtonStack.add(exitButton);
        exitButtonStack.add(exitLabel);
        exitLabel.setAlignment(Align.center);
        exitButtonStack.setSize(exitButton.getWidth(), exitButton.getHeight());

        leftPlayerButtonStack = new Stack();
        leftPlayerButton = new Image(leftPlayerButtonTexture);
        leftPlayerLabel = new Label(PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER).orElse("Vasak"), new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));
        leftPlayerButtonStack.add(leftPlayerButton);
        leftPlayerButtonStack.add(leftPlayerLabel);
        leftPlayerLabel.setAlignment(Align.center);
        leftPlayerButtonStack.setSize(exitButton.getWidth(), exitButton.getHeight());

        rightPlayerButtonStack = new Stack();
        rightPlayerButton = new Image(rightPlayerButtonTexture);
        rightPlayerLabel = new Label(PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER).orElse("Parem"), new Label.LabelStyle(fontManager.getButtonFont24(), Color.WHITE));
        rightPlayerButtonStack.add(rightPlayerButton);
        rightPlayerButtonStack.add(rightPlayerLabel);
        rightPlayerLabel.setAlignment(Align.center);
        rightPlayerButtonStack.setSize(exitButton.getWidth(), exitButton.getHeight());

        // Adding stacks to the stage
        stage.addActor(exitButtonStack);
        stage.addActor(leftPlayerButtonStack);
        stage.addActor(rightPlayerButtonStack);
    }



    private void setupButtonPositionsStandard() {
        float margin = 20;
        float space = 10;

        // Set positions for the entire stack
        exitButtonStack.setPosition(worldWidth - exitButtonStack.getWidth() - margin, margin);
        endTurnButtonStack.setPosition(worldWidth - endTurnButtonStack.getWidth() - margin,
                exitButtonStack.getY() + exitButtonStack.getHeight() + space);
    }

    private void setupButtonPositionsDecider() {
        float margin = 20;
        float space = 10;

        exitButtonStack.setPosition(viewport.getWorldWidth() - exitButtonStack.getWidth() - margin, margin);
        rightPlayerButtonStack.setPosition(exitButtonStack.getX() - rightPlayerButtonStack.getWidth() - space, margin);
        leftPlayerButtonStack.setPosition(rightPlayerButtonStack.getX() - leftPlayerButtonStack.getWidth() - space, margin);
    }

    private void centerLabelOnButton(Label label, Image button) {
        float x = button.getX() + (button.getWidth() - label.getWidth()) / 2;
        float y = button.getY() + (button.getHeight() - label.getHeight()) / 2;
        label.setPosition(x, y);
    }


    private void setupDeciderBackground() {
        // Load the background texture
        Texture backgroundTexture = new Texture(Gdx.files.internal("tablev2.png"));
        Image background = new Image(backgroundTexture);

        // Calculate the position and size to cover the lower third of the screen
        float backgroundHeight = viewport.getWorldHeight() / 3;  // One-third height
        float backgroundY = 0;  // Start at the bottom of the screen

        // Set the size to cover the entire width of the viewport and one-third of the height
        background.setSize(viewport.getWorldWidth(), backgroundHeight);
        background.setPosition(0, backgroundY);

        // Add the background to the stage before adding buttons
        stage.addActor(background);

        // Ensure buttons are added on top of the background
        setupDeciderButtons(); // This will also handle adding the buttons to the stage
    }

    private void addListenersToStandardButtons() {
        endTurnButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PacketEndTurnPressed packet = new PacketEndTurnPressed();
                packet.endTurnPressed = true;
                gameClient.sendTCP(packet);
            }
        });

        exitButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showConfirmationDialog();
            }
        });
    }

    private void addListenersToDeciderButtons() {
        exitButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showConfirmationDialog();
            }
        });
        leftPlayerButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameStateManager.getInstance().getCurrentRound() >= 6) {
                    showConfirmationForLeftWinner();
                }
            }
        });
        rightPlayerButtonStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameStateManager.getInstance().getCurrentRound() == 6) {
                    showConfirmationForRightWinner();
                }
            }
        });
    }

    private void showRulesDialogStandard(int round) {

        int currentRound = gameStateManager.getCurrentRound();
        // This method now depends on the current round

        // Dismiss the previous dialog if it exists
        if (rulesDialog != null && rulesDialog.isVisible()) {
            rulesDialog.hide();
        }

        // Initialize new dialog with round-specific settings
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        FontManager fontManager = FontManager.getInstance();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        Color semiTransparentColor = new Color(0, 0, 0, 0.4f);
        pixmap.setColor(semiTransparentColor);
        pixmap.fill();

        Texture pixmapTexture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(pixmapTexture));

        rulesDialog = new Dialog("", skin);
        rulesDialog.getStyle().titleFont = fontManager.getFontOpenSansRegular12();
        rulesDialog.getStyle().titleFontColor = Color.WHITE;
        rulesDialog.setBackground(backgroundDrawable);

        // Set dialog text based on current round
        String rulesText = getRulesTextForRound(currentRound);
        rulesDialog.text(rulesText, new Label.LabelStyle(fontManager.getFontOpenSansRegular16(), Color.WHITE));

        rulesDialog.button("OK", true);
        rulesDialog.setMovable(false);
        rulesDialog.getContentTable().pad(10);
        rulesDialog.setSize(400, 300);
        rulesDialog.setModal(false);
        rulesDialog.show(stage);
        rulesDialog.setPosition(20, 40);
    }

    private String getRulesTextForRound(int round) {
        switch (round) {
            case 1:
                return  "Esimene voor\n" +
                        "Pead enda tiimi valima kolmest valikust\n" +
                        "parima tiimiliikme. Valiku tegemiseks\n" +
                        "lohista valitud kaart enda mängualale,\n" +
                        "kaart liigub ise õigesse kohta.\n" +
                        "Kui oled valiku teinud, vajuta Valmis.";
            case 2:
                return  "Teine voor\n" +
                        "Pead enda tiimi valima kolmest valikust\n" +
                        "parima tiimiliikme. Valiku tegemiseks\n" +
                        "lohista valitud kaart enda mängualale,\n" +
                        "kaart liigub ise õigesse kohta.\n" +
                        "Kui oled valiku teinud, vajuta Valmis.";
            case 3:
                return  "Kolmas voor\n" +
                        "Pead valima vastasmängija tiimi liikme.\n" +
                        "Kes on kõige nõmedam? Valimiseks\n" +
                        "lohista valitud kaart vastase mängu-\n" +
                        "alale, kaart liigub ise õigesse kohta.\n" +
                        "Kui oled valiku teinud, vajuta Valmis.";
            case 4:
                return "Neljas voor\n" +
                        "Tee enda tiimiliikme paremaks\n" +
                        "või vastase mängija halvemaks.\n" +
                        "Liiguta kaart selle tiimiliikme \n" +
                        "kõrvale, keda´tahad muuta. Siin\n" +
                        "pead ala täpselt tabama.\n" +
                        "Kui valik tehtud, vajuta Valmis.";
            case 5:
                return "Viies voor\n" +
                        "Tee enda tiimiliikme paremaks\n" +
                        "või vastase mängija halvemaks.\n" +
                        "Liiguta kaart selle tiimiliikme \n" +
                        "kõrvale, keda´tahad muuta. Siin\n" +
                        "pead ala täpselt tabama.\n" +
                        "Kui valik tehtud, vajuta Valmis.";
            case 6:
                return "Tiimid on kokku pandud\n" +
                        "Nüüd tuleb oodata otsustaja otsust.\n" +
                        "Hoian sulle pöialt!\n";
            default:
                return "Unknown round";
        }
    }
    private void showRulesDialogDecider(int round) {

        int currentRound = gameStateManager.getCurrentRound();
        // This method now depends on the current round

        // Dismiss the previous dialog if it exists
        if (rulesDialog != null && rulesDialog.isVisible()) {
            rulesDialog.hide();
        }

        // Initialize new dialog with round-specific settings
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        FontManager fontManager = FontManager.getInstance();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        Color semiTransparentColor = new Color(0, 0, 0, 0.4f);
        pixmap.setColor(semiTransparentColor);
        pixmap.fill();

        Texture pixmapTexture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable backgroundDrawable = new TextureRegionDrawable(new TextureRegion(pixmapTexture));

        rulesDialog = new Dialog("", skin);
        rulesDialog.getStyle().titleFont = fontManager.getFontOpenSansRegular12();
        rulesDialog.getStyle().titleFontColor = Color.WHITE;
        rulesDialog.setBackground(backgroundDrawable);

        // Set dialog text based on current round
        String rulesText = getRulesTextForRoundDecider(currentRound);
        rulesDialog.text(rulesText, new Label.LabelStyle(fontManager.getFontOpenSansRegular16(), Color.WHITE));

        rulesDialog.button("OK", true);
        rulesDialog.setMovable(false);
        rulesDialog.getContentTable().pad(10);
        rulesDialog.setSize(600, 300);
        rulesDialog.setModal(false);
        rulesDialog.show(stage);
        rulesDialog.setPosition(20, 120);
    }

    private String getRulesTextForRoundDecider(int round) {
        switch (round) {
            case 1:
                return "Käimas on esimene voor, mängijad valivad enda tiimi kolme valiku seast parima liikme. Sina veel otsust teha ei saa, oota rahulikult. Valiku saad teha pärast viienda vooru lõppu.";
            case 2:
                return "Käimas on teine voor. Mängijad valivad enda tiimi kolme valiku seast parima liikme. Sina veel otsust teha ei saa, oota rahulikult.";
            case 3:
                return "Käimas on kolmas voor. Mängijad valivad vastasmängija tiimi liikme. Sina veel otsust teha ei saa, oota rahulikult.";
            case 4:
                return "Käimas on neljas voor. Mängijad saavad enda tiimiliikmeid paremaks või vastasmängija tiimiliikmeid halvemaks muuta. Sina veel valikut teha ei saa, aga enam ei pea kaua ootama.";
            case 5:
                return "Käimas on viies voor. Mängijad saavad enda tiimiliikmeid paremaks või vastasmängija tiimiliikmeid halvemaks muuta. Kui mõlemad mängijad on valiku teinud, on sinu ülesanne valida välja parem tiim. ";
            case 6:
                return "Sinu etteaste! Võitja valimiseks vajuta selle mängija nimega nupule, kes sinu arvates parem on.";
            default:
                return "Unknown round";
        }
    }

    private void showConfirmationDialog() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialog = new Dialog("Confirm Exit", skin) {
            @Override
            protected void result(Object object) {
                boolean exit = (Boolean) object;
                if (exit) {
                    // The Player exits the app.
                    sendExitMessage();
                } else {
                    // As for now, the dialogue box is simply closed, in the future, might be something else.
                }
            }
        };

        dialog.text("Kas oled kindel, et soovid väljuda?");
        dialog.button("Jah", true); // sends true as the result
        dialog.button("Loobu", false);  // sends false as the result
        dialog.show(stage);
    }
    private void showRightWinnerDialog() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialog = new Dialog("PIDU!!", skin, "dialog") {
            @Override
            protected void result(Object object) {
                boolean exit = (Boolean) object;
                if (exit) {
                    sendExitMessage();
                } else {
                    // As for now, the dialogue box is simply closed, in the future, might be something else.
                }
            }
        };
        String formattedValue = String.format("%.2f", gameClient.playerAverage);
        String formattedValueLoser = String.format("%.2f", gameClient.loserAverage);
        dialog.text("Mängija " + PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER)
                .orElse("paremal") + " on parim!" + "\n" + "\n" + "Mängija "
                + PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER).orElse("vasakul")
                + " keskmine hinne on " + Double.parseDouble(formattedValueLoser) + "\n" + "Mängija "
                + PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER).orElse("paremal")
                + " keskmine hinne on " + Double.parseDouble(formattedValue));
        dialog.button("OK", true);
        dialog.show(stage);
    }

    private void showLeftWinnerDialog() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialog = new Dialog("PIDU!!", skin, "dialog") {
            @Override
            protected void result(Object object) {
                boolean exit = (Boolean) object;
                if (exit) {
                    sendExitMessage();
                } else {
                    // As for now, the dialogue box is simply closed, in the future, might be something else.
                }
            }
        };
        String formattedValue = String.format("%.2f", gameClient.playerAverage);
        String formattedValueLoser = String.format("%.2f", gameClient.loserAverage);
        dialog.text("Mängija " + PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER)
                .orElse("vasakul") + " on parim!" + "\n" + "\n" + "Mängija "
                + PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER).orElse("paremal")
                + " keskmine hinne on " + Double.parseDouble(formattedValueLoser) + "\n" + "Mängija "
                + PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER).orElse("vasakul")
                + " keskmine hinne on " + Double.parseDouble(formattedValue));
        dialog.button("OK", true);
        dialog.show(stage);
    }

    private void showConfirmationForRightWinner() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialog = new Dialog("Confirm Winner", skin) {
            @Override
            protected void result(Object object) {
                boolean confirmed = (Boolean) object;
                if (confirmed) {
                    PacketRightPlayerWins packet = new PacketRightPlayerWins();
                    packet.rightPlayerWins = true;
                    packet.playerName = PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER)
                            .orElse("Parempoolne mängija");
                    packet.loserName = PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER)
                            .orElse("Vasakpoolne mängija");
                    gameClient.sendTCP(packet);
                } else {
                    // As for now, the dialogue box is simply closed, in the future, might be something else.
                }
            }
        };

        dialog.text("Kas kinnitad mängija "
                + PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER).orElse("paremal")
                + " paremaks?");
        dialog.button("Jah", true); // sends true as the result
        dialog.button("Loobu", false);  // sends false as the result
        dialog.show(stage);
    }
    private void showConfirmationForLeftWinner() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialog = new Dialog("Kinnita võitja", skin) {
            @Override
            protected void result(Object object) {
                boolean confirmed = (Boolean) object;
                if (confirmed) {
                    PacketLeftPlayerWins packet = new PacketLeftPlayerWins();
                    packet.leftPlayerWins = true;
                    packet.playerName = PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER)
                            .orElse("Vasakpoolne mängija");
                    packet.loserName = PlayerManager.getInstance().getPlayerNameByRole(RIGHT_PLAYER)
                            .orElse("Parempoolne mängija");
                    gameClient.sendTCP(packet);
                } else {
                    // As for now, the dialogue box is simply closed, in the future, might be something else.
                }
            }
        };

        dialog.text("Kas kinnitad mängija "
                + PlayerManager.getInstance().getPlayerNameByRole(LEFT_PLAYER).orElse("vasakul")
                + " paremaks?");
        dialog.button("Jah", true); // sends true as the result
        dialog.button("Loobu", false);  // sends false as the result
        dialog.show(stage);
    }

    private void showPlayerLeftGameDialog() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialogExit = new Dialog("Mängija lahkus mängust.", skin) {
            @Override
            protected void result(Object object) {
                boolean exit = (Boolean) object;
                if (exit) {
                    Gdx.app.exit(); // Exit the app if "OK" is pressed
                } else {
                    // Handle the case for "Cancel" or other buttons if you add them later
                }
            }
        };

        dialogExit.text("Üks mängijatest lahkus mängust. Mängu ei saa jätkata. Mängust väljumiseks vajuta OK");
        dialogExit.button("OK", true); // Assign "true" as the result when "OK" is clicked

        dialogExit.show(stage); // Show the dialog on the stage
    }


    public void displayWinner(String winner) {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        Dialog dialog = new Dialog("The Victorious", skin);
        dialog.text(winner + " has won the game!");
        dialog.button("Ok", true);
        dialog.show(stage);
    }


    public void sendExitMessage() {
        PacketPlayerExitMessage message = new PacketPlayerExitMessage(currentPlayerId);
        gameClient.sendTCP(message);
    }
    public void onLeftWin() {
        // Safe to exit the game now
        showLeftWinnerDialog();
    }

    public void onRightWin() {
        // Safe to exit the game now
        showRightWinnerDialog();
    }

    public void onExitAcknowledged() {
        // Safe to exit the game now
        Gdx.app.exit();
    }

    public void onExitNotified() {
        // Safe to exit the game now
        showPlayerLeftGameDialog();
    }

    // Method to dispose of resources
    public void dispose() {
        if (exitButtonTexture != null) exitButtonTexture.dispose();
        if (endTurnButtonTexture != null) endTurnButtonTexture.dispose();
        if (leftPlayerButtonTexture != null) leftPlayerButtonTexture.dispose();
        if (rightPlayerButtonTexture != null) rightPlayerButtonTexture.dispose();
        GameStateManager.getInstance().removeRoundChangeListener(this);
        stage.dispose();
    }

    @Override
    public void onRoundChange(int newRound) {
        switch (currentPlayer.getPlayerRole()) {
            case "Decider":
                showRulesDialogDecider(newRound);  // This method will handle dialog for the Decider role
                break;
            case LEFT_PLAYER:
                showRulesDialogStandard(newRound);
                break;
            case RIGHT_PLAYER:
                showRulesDialogStandard(newRound);  // This method will handle dialog for standard players
                break;
            default:
                break;
        }
    }
}

