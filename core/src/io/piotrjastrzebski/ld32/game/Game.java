package io.piotrjastrzebski.ld32.game;

import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.g2d.Batch;
import io.piotrjastrzebski.ld32.game.state.State;

/**
 * Main entry point for game
 * Created by EvilEntity on 18/04/2015.
 */
public class Game implements Telegraph {
	// number of ticks that occur each seconds
	public final static int TICKS_PER_S = 1;
	// fraction of second per each tick
	public final static float S_PER_TICK = 1.f / TICKS_PER_S;
	// milliseconds per each tick
	public final static long MS_PER_TICK = 1000 / TICKS_PER_S;

	private final static String TAG = Game.class.getSimpleName();

	private State state;
	private ILogger logger;
	private boolean isInit;

	protected final MessageDispatcher dispatcher;

	public Game (ILogger logger) {
		this.logger = logger;
		dispatcher = MessageManager.getInstance();
	}

	public void init (State state) {
		if (state == null)
			return;
		isInit = true;
		this.state = state;
		long diff = (state.currentTS().subtract(state.getTS()).longValue()) / MS_PER_TICK;
		if (diff > 0) {
			// add diff ticks to the game state
			log(TAG, "State diff: " + diff);
			tick(diff);
		}
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
		if (!isInit)
			return;

	}

	private void tick (long times) {
		for (long tickID = 0; tickID < times; tickID++) {
			tick();
		}
		state.updateTS();
		log(TAG, "Ticked " + times + " times!");
	}

	private void tick () {
		state.addSpaceBux(1L);
	}

	public void setState (State state) {

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
}
