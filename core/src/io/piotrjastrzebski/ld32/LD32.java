package io.piotrjastrzebski.ld32;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.screens.SplashScreen;

public class LD32 extends Game {
	private Assets assets;
	private SpriteBatch batch;

	@Override public void create () {
		assets = new Assets();
		batch = new SpriteBatch();
		setScreen(new SplashScreen(this));
	}

	@Override public void dispose () {
		super.dispose();
		screen.dispose();
		assets.dispose();
		batch.dispose();
	}

	public Assets getAssets () {
		return assets;
	}

	public SpriteBatch getBatch () {
		return batch;
	}
}
