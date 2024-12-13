package ee.taltech.okgame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryonet.Client;
import com.badlogic.gdx.Game;
import ee.taltech.okgame.screens.MenuScreen;

import java.io.IOException;

public class OKGame extends Game {
	SpriteBatch batch;
	Texture img;

	private Client client;

	GameStateManager gameStateManager = GameStateManager.getInstance();

	/**
	 * New GameClient is created.
	 * One client, one player.
	 */
	@Override
	public void create() {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		this.setScreen(new MenuScreen(this));
	}

	/**
	 * Rendering is done in GameStateManager, not here.
	 */
	@Override
	public void render () {
		super.render();
	}

	/**
	 * Connection ended.
	 */
	@Override
	public void dispose () {
		client.close();
		try {
			client.dispose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		batch.dispose();
		img.dispose();
	}
}