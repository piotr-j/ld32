package io.piotrjastrzebski.ld32.game;

import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.ld32.Constants;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.game.entities.Entity;
import io.piotrjastrzebski.ld32.game.entities.Projectile;
import io.piotrjastrzebski.ld32.game.entities.Turret;
import io.piotrjastrzebski.ld32.game.entities.Ufo;
import io.piotrjastrzebski.ld32.game.state.State;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Main entry point for game
 * Created by EvilEntity on 18/04/2015.
 */
public class Game implements Telegraph {
	public final static float VP_WIDTH = 40.0f;
	public final static float VP_HEIGHT = 22.5f;
	public final static float SCALE = 1.f/32.f;

	// number of ticks that occur each seconds
	public final static int TICKS_PER_S = 1;
	// fraction of second per each tick
	public final static float S_PER_TICK = 1.f / TICKS_PER_S;
	// milliseconds per each tick
	public final static long MS_PER_TICK = 1000 / TICKS_PER_S;

	private final static String TAG = Game.class.getSimpleName();

	private State state;
	private ILogger logger;
	private Assets assets;
	private boolean isInit;

	protected final MessageDispatcher dispatcher;
	private boolean isVisible;

	private OrthographicCamera camera;
	private ExtendViewport viewport;

	ShapeRenderer shapeRenderer;

	Array<Entity> entityArray;
	Array<Projectile> projectiles;
	Array<Ufo> ufos;

	Array<ParticleEffectPool.PooledEffect> effects;
	ParticleEffectPool milkExpEffectPool;

	public Game (ILogger logger, Assets assets) {
		this.logger = logger;
		this.assets = assets;
		effects = new Array<>();
		milkExpEffectPool = new ParticleEffectPool(assets.getMilkExpEffect(), 5, 10);


		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);

		dispatcher = MessageManager.getInstance();
		dispatcher.addListener(this, Msg.FIRE_MILK_MISSILE);

		entityArray = new Array<>();
		projectiles = new Array<>();
		ufos = new Array<>();
		Ufo ufo = new Ufo(assets);
		ufo.setRadius(5).setHealth(10).setAsset("ufo1").setPosition(20, 11.25f);
		ufos.add(ufo);

		Turret milkLauncher = new Turret(assets);
		milkLauncher.setAsset("milk-rocket-launcher").setPosition(6, 5);
		milkLauncher.target(ufo);
		entityArray.add(milkLauncher);


		shapeRenderer = new ShapeRenderer();

	}

	public void init (State state) {
		if (state == null)
			return;
		isInit = true;
		this.state = state;
		if (state.isFresh()) {
			initFreshState();
		}
		long diff = (state.currentTS().subtract(state.getTS()).longValue()) / MS_PER_TICK;
		if (diff > 0) {
			// add diff ticks to the game state
			log(TAG, "State diff: " + diff);
			tick(diff);
		}
	}

	private void initFreshState () {
		Resource spaceBux = new Resource(Constants.Resources.SPACE_BUX);
		Resource ice = new Resource(Constants.Resources.ICE);
		Resource lifeSupport = new Resource(Constants.Resources.LIFE_SUPPORT);
		Resource rocketFuel = new Resource(Constants.Resources.ROCKET_FUEL);
		state.addResource(spaceBux);
		state.addResource(ice);
		state.addResource(lifeSupport);
		state.addResource(rocketFuel);
		log(TAG, "Fresh state!");
		// TODO load initial state form json

		ResourceGenerator spaceBuxGen = new ResourceGenerator(spaceBux.name);
		spaceBuxGen.setAmount(1);
		spaceBuxGen.setMultiplier(1);
		Building spaceBank = new Building(Constants.Building.SPACE_BANK);
		spaceBank.addGenerator(spaceBuxGen);
		spaceBank.addInitialCost(spaceBux.name, 1);
		spaceBank.addAmount(1);
		state.addBuilding(spaceBank);

		ResourceGenerator iceGen = new ResourceGenerator(ice.name);
		iceGen.setAmount(2);
		iceGen.setMultiplier(1);
		Building iceHarvester = new Building(Constants.Building.ICE_HARVESTER);
		iceHarvester.addGenerator(iceGen);
		iceHarvester.addInitialCost(spaceBux.name, 5);
		state.addBuilding(iceHarvester);

		ResourceGenerator fuelGen = new ResourceGenerator(rocketFuel.name);
		fuelGen.setAmount(1);
		fuelGen.setMultiplier(1);

		ResourceGenerator lsGen = new ResourceGenerator(lifeSupport.name);
		lsGen.setAmount(2);
		lsGen.setMultiplier(1);

		Building iceRefinery = new Building(Constants.Building.ICE_REFINERY);
		iceRefinery.addGenerator(fuelGen);
		iceRefinery.addGenerator(lsGen);
		iceRefinery.addInitialCost(spaceBux.name, 10);
		iceRefinery.addInitialCost(ice.name, 20);
		state.addBuilding(iceRefinery);

	}

	public void tap (float x, float y) {

	}

	private float tickAcc = 0;

	public boolean update (float delta) {
		if (!isInit)
			return false;
		updateDefense(delta);
		tickAcc += delta;
		if (tickAcc >= S_PER_TICK) {
			tickAcc -= S_PER_TICK;
			tick(1);
			return true;
		}
		return false;
	}

	private void updateDefense(float delta) {
		if (!isVisible) return;

		for (Entity entity: entityArray) {
			entity.update(delta);
		}

		for(Ufo ufo:ufos) {
			ufo.update(delta);
		}

		for (int i = effects.size - 1; i >= 0; i--) {
			ParticleEffectPool.PooledEffect effect = effects.get(i);
			effect.update(delta);
			if (effect.isComplete()) {
				effect.free();
				effects.removeIndex(i);
			}
		}

		Iterator<Projectile> projIter = projectiles.iterator();
		while (projIter.hasNext()) {
			Projectile projectile = projIter.next();
			projectile.update(delta);
			if (projectile.isExploded()) {
				createExplosion(projectile.getTarget());
				damageUfos(projectile.getDamage(), projectile.getTargetCircle());
				// TODO pool
				projIter.remove();
			}
		}
	}

	private void damageUfos (BigDecimal damage, Circle target) {
		// TODO
		for (Ufo ufo:ufos) {
			ufo.damage(damage);
		}
	}

	private void createExplosion (Vector2 target) {
		createExplosion(target.x, target.y);
	}

	private void createExplosion (float x, float y) {
		ParticleEffectPool.PooledEffect effect = milkExpEffectPool.obtain();
		effect.setPosition(x, y);
		effects.add(effect);
	}

	public void draw (Batch batch) {
		if (!isInit || !isVisible)
			return;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for (Entity entity: entityArray) {
			entity.draw(batch);
		}
		for(Ufo ufo:ufos) {
			ufo.draw(batch);
		}
		for (ParticleEffectPool.PooledEffect effect:effects) {
			effect.draw(batch);
		}
		for(Projectile projectile: projectiles) {
			projectile.draw(batch);
		}
		batch.end();
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (Entity entity: entityArray) {
			entity.drawBounds(shapeRenderer);
		}
		for(Ufo ufo:ufos) {
			ufo.drawBounds(shapeRenderer);
		}
		for(Projectile projectile: projectiles) {
			projectile.drawBounds(shapeRenderer);
		}
		shapeRenderer.end();
	}

	private void tick (long times) {
		for (long tickID = 0; tickID < times; tickID++) {
			tick();
		}
		state.updateTS();
		log(TAG, "Ticked " + times + " times!");
	}

	private void tick () {
		for (Building building : state.getBuildings()) {
			building.tick(state);
		}

	}

	public State getState () {
		return state;
	}

	public void log (String tag, String msg) {
		logger.log(tag, msg);
	}

	private void fireMissile (Vector2 target, String type) {
		if (projectiles.size > 0) return;
		Projectile projectile = new Projectile(assets);
		projectile.setAsset("milk-rocket");
		projectile.setPosition(6, 5);
		projectile.setDamage(1L);
		projectile.setTarget(target.x, target.y);
		projectiles.add(projectile);

	}

	@Override public boolean handleMessage (Telegram msg) {
		switch (msg.message) {
		case Msg.FIRE_MILK_MISSILE:
			fireMissile((Vector2)msg.extraInfo, "milk-missile");
			break;

		}
		return false;
	}

	public void visible () {
		isVisible = true;
	}

	public void hidden () {
		isVisible = false;
	}

	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}
}
