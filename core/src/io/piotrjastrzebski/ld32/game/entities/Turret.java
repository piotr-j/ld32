package io.piotrjastrzebski.ld32.game.entities;

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
		this.target = target;
	}

	Vector2 targetPos = new Vector2();
	// cooldown in seconds
	float FIRE_COOLDOWN = 1;
	float fireCD = 0;
	@Override public void update (float delta) {
		if (target == null) {
			return;
		}
		if (target.isDead()) {
			target = null;
			return;
		}
		// fire if off cool down
		fireCD+=delta;
		if (fireCD >= FIRE_COOLDOWN) {
			fireCD -= FIRE_COOLDOWN;
			// TODO use radius to plot random position
			targetPos.set(target.getX(), target.getY());
			dispatcher.dispatchMessage(this, Msg.FIRE_MILK_MISSILE, targetPos);
		}
	}

	@Override public void reset() {
		target = null;
		fireCD = 0;
	}
}
