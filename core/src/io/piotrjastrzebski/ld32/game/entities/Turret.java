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
		projSpawn.set(x+0.5f, y+1.5f);
		return this;
	}

	Vector2 projSpawn = new Vector2();
	Vector2 targetPos = new Vector2();
	// cooldown in seconds
	float fireCoolDown = 5;
	float currentCoolDown = MathUtils.random(0, fireCoolDown);
	int numRockets = 4;

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
		currentCoolDown +=delta;
		if (currentCoolDown >= fireCoolDown) {
			currentCoolDown -= fireCoolDown;
			// TODO use radius to plot random position
			for (int i = 0; i < numRockets; i++) {
				dispatcher.dispatchMessage(this, Msg.FIRE_MILK_MISSILE, this);
			}
		}
	}

	@Override public void reset() {
		target = null;
		currentCoolDown = 0;
	}

	public Vector2 getTarget () {
		float xOffset = target.getWidth()/2;
		float yOffset = target.getHeight()/2;
		float radius = target.getRadius();
		targetPos.set(target.getX() + xOffset + MathUtils.random(-radius / 2, radius / 2),
			target.getY() + yOffset + MathUtils.random(-radius / 2, radius / 2));
		return targetPos;
	}

	Vector2 nextSpawn = new Vector2();
	float ySpawnOffset = 0;
	float ySpawnOffsetPerRocket = 0.3f;
	public Vector2 getProjSpawn() {
		if (ySpawnOffset >= ySpawnOffsetPerRocket *3.5f) {
			ySpawnOffset = 0;
		}
		nextSpawn.set(projSpawn).add(0, ySpawnOffset);
		ySpawnOffset += ySpawnOffsetPerRocket;
		return nextSpawn;
	}

	public String getProjType () {
		return projType;
	}

	public TextureAtlas.AtlasSprite getSprite () {
		return sprite;
	}

	/**
	 * Cooldown between shots
	 */
	public void setFireCoolDown (float cooldown) {
		this.fireCoolDown = cooldown;
	}

	/**
	 * Number of rockets per shot 1-4
	 */
	public void setNumRocketsPerShot(int numRockets) {
		this.numRockets = MathUtils.clamp(numRockets, 1, 4);
	}
}
