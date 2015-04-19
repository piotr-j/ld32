package io.piotrjastrzebski.ld32.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import io.piotrjastrzebski.ld32.LD32;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public class SplashScreen extends BaseScreen {
	private final static String TAG = SplashScreen.class.getSimpleName();

	private static final float MIN_SPLASH_TIME = 0.5f;
	private float splashTime;
	private final Texture img;

	public SplashScreen (LD32 game) {
		super(game);
		img = new Texture("loading.png");
	}

	@Override public void update (float delta) {
		splashTime += delta;
		if (assets.update() && splashTime > MIN_SPLASH_TIME) {
			base.setScreen(new GameScreen(base));
		}
	}

	@Override public void draw () {
		Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 20, 20);
		batch.end();
	}
}
