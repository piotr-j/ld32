package io.piotrjastrzebski.ld32.game;

import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
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
public class Game implements Telegraph, Building.BuyListener {
	public final static boolean DEBUG_DRAW = false;
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

	Array<Turret> turretArray;
	Array<Projectile> projectiles;
	Array<Ufo> ufos;

	Array<ParticleEffectPool.PooledEffect> effects;
	ParticleEffectPool milkExpEffectPool;

	Pool<Projectile> projectilePool;

	private float[] launcherPositions = {
		8, 1, 0,
		VP_WIDTH - 3, 7, 1,
		1, 10, 0,
		VP_WIDTH - 8, 1, 1,
		3, 7, 0,
		VP_WIDTH - 1, 10, 1,
		3, 3, 0,
		VP_WIDTH - 3, 3, 1
	};

	private final int maxLaunchers = 8;

	public Game (ILogger logger, final Assets assets) {
		this.logger = logger;
		this.assets = assets;
		effects = new Array<>();
		milkExpEffectPool = new ParticleEffectPool(assets.getMilkExpEffect(), 4, 32);


		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);

		dispatcher = MessageManager.getInstance();
		dispatcher.addListener(this, Msg.FIRE_MILK_MISSILE);
		dispatcher.addListener(this, Msg.CREATE_EXP);

		ufos = new Array<>();

		turretArray = new Array<>();
//
//		for (int i = 0; i < 8; i++) {
//			addLauncher();
//		}

		projectiles = new Array<>();
		projectilePool = new Pool<Projectile>() {
			@Override protected Projectile newObject () {
				return new Projectile(assets);
			}
		};

		shapeRenderer = new ShapeRenderer();

	}

	int currentLauncher = 0;
	public void addLauncher() {
		if (currentLauncher >= maxLaunchers) return;
		int id = currentLauncher*3;
		float x = launcherPositions[id];
		float y = launcherPositions[id+1];
		boolean flipX = launcherPositions[id+2]>0.5;

		Turret turret = new Turret(assets, "milk-rocket");
		turret.setAsset("milk-rocket-launcher").setPosition(x - (flipX?turret.getWidth():0), y);
		turret.getSprite().flip(flipX, false);
		turretArray.add(turret);

		currentLauncher++;
	}

	public void init (State state) {
		if (state == null)
			return;
		isInit = true;
		this.state = state;
		turretArray.clear();
		ufos.clear();
		projectiles.clear();
		if (state.isFresh()) {
			initFreshState();
		}
		initListeners();
		Building building = state.getBuilding(Constants.Building.MILK_LAUNCHER);
		for (int i = 0; i < building.amount; i++) {
			addLauncher();
		}
		Building building2 = state.getBuilding(Constants.Building.MILK_LAUNCHER_UPGRADE_ROCKETS);
		for (int i = 0; i < building2.amount; i++) {
			addRockets();
		}
		Building building3 = state.getBuilding(Constants.Building.MILK_LAUNCHER_UPGRADE_DAMAGE);
		for (int i = 0; i < building3.amount; i++) {
			upgradeDamage();
		}
		Building building4 = state.getBuilding(Constants.Building.MILK_LAUNCHER_UPGRADE_RELOAD);
		for (int i = 0; i < building4.amount; i++) {
			upgradeReloadSpeed();
		}
		long diff = (state.currentTS().subtract(state.getTS()).longValue()) / MS_PER_TICK;
		if (diff > 0) {
			// add diff ticks to the game state
			log(TAG, "State diff: " + diff);
			tick(diff);
		}
		spawnUfo(VP_WIDTH / 2, VP_HEIGHT / 2);
	}

	private void initListeners () {
		Array<Building> buildings = state.getBuildings();
		for (Building building:buildings) {
			building.setBuyListener(this);
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
		spaceBank.addInitialCost(spaceBux.name, 5);
		state.addBuilding(spaceBank);

		ResourceGenerator iceGen = new ResourceGenerator(ice.name);
		iceGen.setAmount(2);
		iceGen.setMultiplier(1);
		Building iceHarvester = new Building(Constants.Building.ICE_HARVESTER);
		iceHarvester.addGenerator(iceGen);
		iceHarvester.addInitialCost(spaceBux.name, 10);
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


		Building turret = new Building(Constants.Building.MILK_LAUNCHER);
		turret.addInitialCost(spaceBux.name, 100);
		turret.addInitialCost(ice.name, 200);
		turret.addInitialCost(lifeSupport.name, 200);
		state.addBuilding(turret);

		Building turretUpgradeDamage = new Building(Constants.Building.MILK_LAUNCHER_UPGRADE_DAMAGE);
		turretUpgradeDamage.addInitialCost(spaceBux.name, 300);
		turretUpgradeDamage.addInitialCost(ice.name, 300);
		turretUpgradeDamage.addInitialCost(rocketFuel.name, 400);
		state.addBuilding(turretUpgradeDamage);

		Building turretUpgradeReload = new Building(Constants.Building.MILK_LAUNCHER_UPGRADE_RELOAD);
		turretUpgradeReload.addInitialCost(spaceBux.name, 250);
		turretUpgradeReload.addInitialCost(ice.name, 500);
		turretUpgradeReload.addInitialCost(rocketFuel.name, 600);
		turretUpgradeReload.setMaxAmount(10);
		state.addBuilding(turretUpgradeReload);

		Building turretUpgradeRockets = new Building(Constants.Building.MILK_LAUNCHER_UPGRADE_ROCKETS);
		turretUpgradeRockets.addInitialCost(spaceBux.name, 1000);
		turretUpgradeRockets.addInitialCost(ice.name, 3000);
		turretUpgradeRockets.addInitialCost(rocketFuel.name, 2000);
		turretUpgradeRockets.setMaxAmount(4);
		state.addBuilding(turretUpgradeRockets);

	}

	int rockets = 1;
	private void addRockets() {
		rockets++;
		for (Turret turret:turretArray) {
			turret.setNumRocketsPerShot(rockets);
		}
	}
	float reloadSpeed = 5.5f;
	private void upgradeReloadSpeed() {
		reloadSpeed-=0.5f;
		for (Turret turret:turretArray) {
			turret.setFireCoolDown(reloadSpeed);
		}
	}

	long damage = 1;
	private void upgradeDamage() {
		damage+= 1;
	}

	public void tap () {
		fireMissile();
	}

	int lastTurretId = 0;
	private void fireMissile () {
		if (turretArray.size == 0) return;
		if (lastTurretId >= turretArray.size) {
			lastTurretId = 0;
		}
		Turret turret = turretArray.get(lastTurretId);
		if (turret.getUfo() == null) return;
		if (turret.getUfo().isDead() || !turret.getUfo().isAttackable()) return;
		Projectile projectile = projectilePool.obtain();
		projectile.setAsset(turret.getProjType());
		Vector2 spawn = turret.getProjSpawn();
		projectile.setPosition(spawn.x, spawn.y);
		projectile.setDamage(damage);
		projectile.setTarget(turret.getTarget());
		projectiles.add(projectile);
		lastTurretId++;
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
		Ufo first = null;
		if (ufos.size > 0) {
			first = ufos.get(0);
		}
		for (Turret turret: turretArray) {
			turret.update(delta);
			turret.target(first);
		}

		Iterator<Ufo> ufoIterator = ufos.iterator();
		while (ufoIterator.hasNext()) {
			Ufo ufo = ufoIterator.next();
			ufo.update(delta);
			if (ufo.needsRemoval()) {
				ufoIterator.remove();
				spawnUfo(VP_WIDTH / 2, VP_HEIGHT / 2);
			}
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
				projectilePool.free(projectile);
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
		assets.playSound(Assets.S_EXP_2);
	}

	public void draw (Batch batch) {
		if (!isInit || !isVisible)
			return;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		for(Ufo ufo:ufos) {
			ufo.draw(batch);
		}
		for(Projectile projectile: projectiles) {
			projectile.draw(batch);
		}
		for (ParticleEffectPool.PooledEffect effect:effects) {
			effect.draw(batch);
		}
		for (Turret turret: turretArray) {
			turret.draw(batch);
		}
		batch.end();
		if (DEBUG_DRAW) {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			for (Entity entity : turretArray) {
				entity.drawBounds(shapeRenderer);
			}
			for (Ufo ufo : ufos) {
				ufo.drawBounds(shapeRenderer);
			}
			for (Projectile projectile : projectiles) {
				projectile.drawBounds(shapeRenderer);
			}
			shapeRenderer.end();
		}
	}

	private void tick (long times) {
		for (long tickID = 0; tickID < times; tickID++) {
			tick();
		}
		state.updateTS();
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

	private void fireMissile (Turret turret) {
		Projectile projectile = projectilePool.obtain();
		projectile.setAsset(turret.getProjType());
		Vector2 spawn = turret.getProjSpawn();
		projectile.setPosition(spawn.x, spawn.y);
		projectile.setDamage(damage);
		projectile.setTarget(turret.getTarget());
		projectiles.add(projectile);
	}

	private void spawnUfo(float x, float y) {
		Ufo ufo = new Ufo(assets);
		int ufoLevel = state.getUfoLevel() + 1;
		ufo.setRadius(3).setHealth(10*ufoLevel).setAsset("ufo1")
			.setPosition(x - ufo.getWidth() / 2, y - ufo.getHeight() / 2);
		ufos.add(ufo);
		state.setUfoLevel(ufoLevel);
	}

	@Override public boolean handleMessage (Telegram msg) {
		switch (msg.message) {
		case Msg.FIRE_MILK_MISSILE:
			fireMissile((Turret)msg.extraInfo);
			break;
		case Msg.CREATE_EXP:
			createExplosion((Vector2)msg.extraInfo);
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

	@Override public void buySuccess (Building building) {
		// TODO this is shit
		switch (building.name) {
		case Constants.Building.MILK_LAUNCHER:
			addLauncher();
			break;
		case Constants.Building.MILK_LAUNCHER_UPGRADE_ROCKETS:
			addRockets();
			break;
		case Constants.Building.MILK_LAUNCHER_UPGRADE_DAMAGE:
			upgradeDamage();
			break;
		case Constants.Building.MILK_LAUNCHER_UPGRADE_RELOAD:
			upgradeReloadSpeed();
			break;
		}
	}

	@Override public void buyFailed (Building building) {

	}

	public boolean isVisible () {
		return isVisible;
	}
}
