package io.piotrjastrzebski.ld32.game.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.game.Msg;

/**
 * Created by EvilEntity on 19/04/2015.
 */
public class Turret extends Entity {
	private Ufo target;

	public Turret (Assets assets) {
		super(assets);
	}

	public void target(Ufo target) {
		if (this.target == null)
			this.target = target;
	}

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
				target.getX()+xOffset+MathUtils.random(-radius, radius),
				target.getY()+yOffset+MathUtils.random(-radius, radius));
			dispatcher.dispatchMessage(this, Msg.FIRE_MILK_MISSILE, targetPos);
		}
	}

	@Override public void reset() {
		target = null;
		fireCD = 0;
	}
}
