package io.piotrjastrzebski.ld32.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.ld32.LD32;
import io.piotrjastrzebski.ld32.assets.Assets;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public abstract class BaseScreen implements Screen, Telegraph {
	public final static int VP_WIDTH = 1280;
	public final static int VP_HEIGHT = 720;
	protected final LD32 base;
	protected final Assets assets;
	protected final SpriteBatch batch;
	protected final ExtendViewport viewport;
	protected final OrthographicCamera camera;
	protected final MessageDispatcher dispatcher;

	public BaseScreen (LD32 base) {
		dispatcher = MessageManager.getInstance();

		this.base = base;
		assets = this.base.getAssets();
		batch = this.base.getBatch();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT);
		camera = (OrthographicCamera)viewport.getCamera();
	}

	protected void playBtnSound () {
		assets.playSound(Assets.S_BUTTON);
	}

	public abstract void update (float delta);

	public abstract void draw ();

	@Override public void render (float delta) {
		batch.setProjectionMatrix(camera.combined);
		update(delta);
		draw();
	}

	@Override public void resize (int width, int height) {
		viewport.update(width, height, true);
	}

	@Override public void show () {

	}

	@Override public void pause () {

	}

	@Override public void resume () {

	}

	@Override public void hide () {

	}

	@Override public void dispose () {

	}

	@Override public boolean handleMessage (Telegram msg) {
		return false;
	}
}
