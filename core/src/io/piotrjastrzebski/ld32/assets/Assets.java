package io.piotrjastrzebski.ld32.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public class Assets {
	public final static String SKIN = "ui/uiskin.json";
	public final static String ATLAS = "pack/packed.atlas";
	private AssetManager manager;
	private boolean isDone;
	private Skin skin;
	private TextureAtlas atlas;

	public Assets () {
		manager = new AssetManager();
		manager.load(SKIN, Skin.class);
		manager.load(ATLAS, TextureAtlas.class);
	}

	public boolean update () {
		isDone = manager.update();
		if (isDone) {
			finalizeLoading();
		}
		return isDone;
	}

	private void finalizeLoading () {
		skin = manager.get(SKIN, Skin.class);
		atlas = manager.get(ATLAS, TextureAtlas.class);

	}

	public TextureAtlas.AtlasRegion getRegion (String name) {
		// TODO cache
		return atlas.findRegion(name);
	}

	public boolean isFinalized () {
		return isDone;
	}

	public Skin getSkin () {
		return skin;
	}

	public void dispose () {
		manager.dispose();
	}
}
