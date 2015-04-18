package io.piotrjastrzebski.ld32.game;

import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.ld32.Constants;
import io.piotrjastrzebski.ld32.assets.Assets;
import io.piotrjastrzebski.ld32.game.state.State;

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

	TextureAtlas.AtlasSprite ufo;

	public Game (ILogger logger, Assets assets) {
		this.logger = logger;
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, camera);

		ufo = new TextureAtlas.AtlasSprite(assets.getRegion("ufo1"));
		ufo.setPosition(0, 0);
		ufo.setSize(ufo.getWidth() * SCALE, ufo.getHeight() * SCALE);

		dispatcher = MessageManager.getInstance();

		// TODO load buildings and resources from json
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

	public void tap () {

	}

	private float tickAcc = 0;

	public boolean update (float delta) {
		if (!isInit)
			return false;
		tickAcc += delta;
		if (tickAcc >= S_PER_TICK) {
			tickAcc -= S_PER_TICK;
			tick(1);
			return true;
		}
		return false;
	}

	public void draw (Batch batch) {
		if (!isInit || !isVisible)
			return;
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		ufo.draw(batch);

		batch.end();
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

	@Override public boolean handleMessage (Telegram msg) {
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
