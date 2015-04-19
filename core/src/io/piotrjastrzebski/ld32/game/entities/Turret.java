package io.piotrjastrzebski.ld32.game.entities;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.game.Msg;

/**
 * Created by EvilEntity on 19/04/2015.
 */
public class Turret extends Entity {
	private Ufo target;
	private String projType;
	public Turret (Assets assets, String projType) {
		super(assets);
		this.projType = projType;
	}

	public void target(Ufo target) {
		if (this.target == null)
			this.target = target;
	}

	@Override public Entity setPosition (float x, float y) {
		super.setPosition(x, y);
		projSpawn.set(x+0.5f, y+2f);
		return this;
	}

	Vector2 projSpawn = new Vector2();
	Vector2 targetPos = new Vector2();
	// cooldown in seconds
	float FIRE_COOLDOWN = 1;
	float fireCD = FIRE_COOLDOWN;
	@Override public void update (float delta) {
		if (target == null) {
			return;
		}
		if (target.isDead()) {
			target = null;
			return;
		}
		if (!target.isAttackable()) {
			return;
		}
		// fire if off cool down
		fireCD+=delta;
		if (fireCD >= FIRE_COOLDOWN) {
			fireCD -= FIRE_COOLDOWN;
			// TODO use radius to plot random position
			float xOffset = target.getWidth()/2;
			float yOffset = target.getHeight()/2;
			float radius = target.getRadius();
			targetPos.set(
				target.getX()+xOffset+MathUtils.random(-radius/2, radius/2),
				target.getY()+yOffset+MathUtils.random(-radius/2, radius/2));
			dispatcher.dispatchMessage(this, Msg.FIRE_MILK_MISSILE, this);
		}
	}

	@Override public void reset() {
		target = null;
		fireCD = 0;
	}

	public Vector2 getTarget () {
		return targetPos;
	}

	public Vector2 getProjSpawn() {
		return projSpawn;
	}

	public String getProjType () {
		return projType;
	}

	public TextureAtlas.AtlasSprite getSprite () {
		return sprite;
	}
}
