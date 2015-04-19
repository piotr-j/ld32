package io.piotrjastrzebski.ld32.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.ld32.assets.Assets;

import java.math.BigDecimal;

/**
 * Created by EvilEntity on 19/04/2015.
 */
public class Ufo extends Entity {
	private float radius = 5;
	private BigDecimal health;

	public Ufo (Assets assets) {
		super(assets);
		health = BigDecimal.TEN;
	}

	@Override public Entity setAsset (String name) {
		super.setAsset(name);
		sprite.setScale(0.25f);

		return this;
	}

	public float getRadius () {
		return radius;
	}

	public Ufo setRadius (float radius) {
		this.radius = radius;
		return this;
	}

	public Ufo setHealth (long health) {
		this.health = BigDecimal.valueOf(health);
		return this;
	}

	public void damage(long damage) {
		health = health.subtract(BigDecimal.valueOf(damage));
	}
	public void damage(BigDecimal damage) {
		health = health.subtract(damage);
	}

	public boolean isDead() {
		return health.compareTo(BigDecimal.ZERO) <= 0;
	}

	boolean justSpawned = true;
	float spawnTimer = 0;
	boolean needsRemoval = false;
	float deathTimer = 0;
	float idleTimer = 0;

	boolean deadSound = false;

	@Override public void update (float delta) {
		if (justSpawned) {
			spawnTimer+=delta;
			sprite.setColor(1, 1, 1, 0.25f+spawnTimer);
			sprite.setScale(0.25f+spawnTimer);
			if (spawnTimer >= .75f) {
				justSpawned = false;
				sprite.setColor(1,1,1,1);
				sprite.setScale(1);
			}
		}

		idleTimer += delta;
		float offset = MathUtils.sin(idleTimer * MathUtils.PI)*0.01f;
		sprite.setPosition(sprite.getX(), sprite.getY()-offset);

		if (isDead()) {
			if (!deadSound) {
				deadSound = true;
				assets.playSound(Assets.S_UFO_DEATH);
			}
			deathTimer += delta;
			float clamp = MathUtils.clamp(1 - deathTimer/2, 0, 1);
			sprite.setColor(1, 1, 1, clamp);
			sprite.setScale(clamp);
			sprite.setPosition(sprite.getX(), sprite.getY()-0.1f);
			if (deathTimer >= 5) {
				needsRemoval = true;
			}
		}
	}

	public boolean isAttackable () {
		return !justSpawned;
	}
	public boolean needsRemoval () {
		return needsRemoval;
	}
}
