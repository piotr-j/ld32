package io.piotrjastrzebski.ld32.game.entities;

import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.game.Game;

/**
 * Created by EvilEntity on 19/04/2015.
 */
public abstract class Entity implements Pool.Poolable, Telegraph{
	protected final MessageDispatcher dispatcher;
	protected TextureAtlas.AtlasSprite sprite;
	protected Assets assets;
	protected Vector2 pos;

	public Entity(Assets assets) {
		this.assets = assets;
		dispatcher = MessageManager.getInstance();
		pos = new Vector2();
	}

	public Entity setAsset(String name) {
		sprite = new TextureAtlas.AtlasSprite(assets.getRegion(name));
		sprite.setSize(sprite.getWidth() * Game.SCALE, sprite.getHeight() * Game.SCALE);
		sprite.setOriginCenter();
		return this;
	}

	public Entity setPosition(float x, float y) {
		sprite.setPosition(x, y);
		pos.set(x, y);
		return this;
	}

	public float getX() {
		return sprite.getX();
	}

	public float getY() {
		return sprite.getY();
	}

	public float getWidth() {
		return sprite.getWidth();
	}

	public float getHeight() {
		return sprite.getHeight();
	}

	public void draw(Batch batch) {
		if (sprite == null) return;
		sprite.draw(batch);
	}

	public void update(float delta) {

	}

	@Override public void reset () {
		sprite = null;
	}

	@Override public boolean handleMessage (Telegram msg) {
		return false;
	}

	public void drawBounds (ShapeRenderer shapeRenderer) {
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(
			sprite.getX(), sprite.getY(),
			sprite.getOriginX(), sprite.getOriginY(),
			sprite.getWidth(), sprite.getHeight(),
			sprite.getScaleX(), sprite.getScaleY(),
			sprite.getRotation());
	}

	public Vector2 getPos () {
		return pos;
	}
}
