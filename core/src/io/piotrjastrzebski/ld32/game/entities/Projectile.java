package io.piotrjastrzebski.ld32.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.game.Game;

import java.math.BigDecimal;

/**
 * Created by EvilEntity on 19/04/2015.
 */
public class Projectile extends Entity {
	BigDecimal damage;

	public Projectile (Assets assets) {
		super(assets);
		damage = BigDecimal.ONE;
	}


	Vector2 pos = new Vector2(0, 0);
	Vector2 speed = new Vector2(0, 0);
	Vector2 baseAccel = new Vector2(1, 1);
//	Vector2 accel = new Vector2(0, 0);
	Vector2 target = new Vector2();
	Circle targetCircle = new Circle();
	Rectangle screenRect = new Rectangle(-2, -2, Game.VP_WIDTH + 4, Game.VP_HEIGHT + 4);

	public Projectile setTarget(float x, float y) {
		target.set(x, y);
		pos.set(getX(), getY());
		float angle = target.sub(pos).angle();
		baseAccel.setAngle(angle).scl(0.1f);

//		float cos = (float)Math.cos(angle * MathUtils.degreesToRadians);
//		float sin = (float)Math.sin(angle * MathUtils.degreesToRadians);
//
//		float newX = cos - sin;
//		float newY = sin + cos;
//		baseAccel.set(newX, newY).scl(0.1f);

		targetCircle.set(x, y, 2);
		sprite.rotate(angle);
		target.set(x, y);
		return this;
	}

	public Projectile setDamage(long damage) {
		this.damage = BigDecimal.valueOf(damage);
		return this;
	}

	@Override public void reset() {
		speed.set(0, 0);
		isExploded = false;
	}

	boolean isExploded = false;

	@Override public void update (float delta) {
		if (isExploded || baseAccel.isZero()) return;
		pos.set(getX(), getY());
//		speed.add(accel.set(baseAccel).scl(delta));
		pos.add(baseAccel);//.scl(delta*10);
		sprite.setPosition(pos.x, pos.y);
		if (targetCircle.contains(pos) || !screenRect.contains(pos)) {
			isExploded = true;
			Gdx.app.log("", "exploded at "+pos);
		}
	}

	@Override public void drawBounds (ShapeRenderer shapeRenderer) {
		super.drawBounds(shapeRenderer);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.circle(targetCircle.x, targetCircle.y, targetCircle.radius);
		shapeRenderer.setColor(Color.MAGENTA);
		shapeRenderer.line(pos.x, pos.y, target.x, target.y);
	}

	public Vector2 getTarget () {
		return target;
	}

	public boolean isExploded() {
		return isExploded;
	}

	public BigDecimal getDamage () {
		return damage;
	}

	public Circle getTargetCircle () {
		return targetCircle;
	}
}
