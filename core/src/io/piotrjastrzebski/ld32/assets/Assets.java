package io.piotrjastrzebski.ld32.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public class Assets {
	public final static String SKIN = "ui/uiskin.json";
	public final static String ATLAS = "pack/packed.atlas";
	public final static String MILK_PARTICLE_EFFECT = "milk-explosion.p";
	public final static String S_BUTTON = "sounds/button.wav";
	public final static String S_EXP_1 = "sounds/explosion.wav";
	public final static String S_EXP_2 = "sounds/explosion2.wav";
	public final static String S_HIT = "sounds/hit.wav";
	public final static String S_ROCKET = "sounds/rocket.wav";
	public final static String S_UFO_DEATH = "sounds/ufo-death.wav";
	private final static String[] SOUNDS = {
		S_BUTTON, S_EXP_1, S_EXP_2, S_HIT, S_ROCKET, S_UFO_DEATH
	};
	private AssetManager manager;
	private boolean isDone;
	private Skin skin;
	private TextureAtlas atlas;
	private ParticleEffect milkExpEffect;

	private ObjectMap<String, Sound> sounds;

	public Assets () {
		sounds = new ObjectMap<>();

		manager = new AssetManager();
		manager.load(SKIN, Skin.class);
		manager.load(ATLAS, TextureAtlas.class);
		ParticleEffectLoader.ParticleEffectParameter parameter = new ParticleEffectLoader.ParticleEffectParameter();
		parameter.atlasFile = ATLAS;
		manager.load(MILK_PARTICLE_EFFECT, ParticleEffect.class, parameter);

		for (String soundPath:SOUNDS) {
			manager.load(soundPath, Sound.class);
		}
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
		milkExpEffect = manager.get(MILK_PARTICLE_EFFECT, ParticleEffect.class);

		for (String soundPath:SOUNDS) {
			Sound sound = manager.get(soundPath, Sound.class);
			sounds.put(soundPath, sound);
		}
	}

	public TextureAtlas.AtlasRegion getRegion (String name) {
		// TODO cache
		return atlas.findRegion(name);
	}

	public void playSound(String name) {
		if (!soundEnabled) return;
		sounds.get(name).play();
	}

	public boolean isFinalized () {
		return isDone;
	}

	public Skin getSkin () {
		return skin;
	}

	public ParticleEffect getMilkExpEffect () {
		return milkExpEffect;
	}

	public void dispose () {
		manager.dispose();
	}

	boolean soundEnabled = false;
	public void setSoundEnabled (boolean enabled) {
		soundEnabled = enabled;
		if (!soundEnabled) {
			ObjectMap.Values<Sound> values = sounds.values();

			for (Sound sound:values) {
				sound.stop();
			}
		}
	}

	public boolean isSoundEnabled () {
		return soundEnabled;
	}
}
