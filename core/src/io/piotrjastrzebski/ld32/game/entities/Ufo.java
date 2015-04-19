package io.piotrjastrzebski.ld32.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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

	public boolean isDead() {
		return health.compareTo(BigDecimal.ZERO) <= 0;
	}

	@Override public void update (float delta) {
		if (isDead()) {
			sprite.setColor(Color.RED);
		}
	}
}
