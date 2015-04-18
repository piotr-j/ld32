package io.piotrjastrzebski.ld32.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.strongjoshua.console.Console;
import io.piotrjastrzebski.ld32.LD32;
import io.piotrjastrzebski.ld32.game.Building;
import io.piotrjastrzebski.ld32.game.Game;
import io.piotrjastrzebski.ld32.game.ILogger;
import io.piotrjastrzebski.ld32.game.Resource;
import io.piotrjastrzebski.ld32.game.state.NumberFormatter;
import io.piotrjastrzebski.ld32.game.state.State;
import io.piotrjastrzebski.ld32.game.state.StateSerializer;

import java.math.BigDecimal;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public class GameScreen extends BaseScreen implements ILogger {
	private final static String TAG = GameScreen.class.getSimpleName();
	private final static String PREFS = "LD32";
	private final static String PREFS_STATE = "state";
	private final static String PREFS_SAVE_TIMER = "save-timer";
	public final static int BUY_ALL = -1;
	public final static int BUY_1 = 1;
	public final static int BUY_10 = 10;
	public final static int BUY_100 = 100;
	private int SAVE_TIMER = 10;

	private final Preferences prefs;
	private final Game game;
	private final Stage stage;
	private Console console;
	private final InputMultiplexer multiplexer;
	private final Skin skin;

	private StateSerializer serializer;

	public GameScreen (LD32 base) {
		super(base);
		prefs = Gdx.app.getPreferences(PREFS);
		skin = assets.getSkin();
		multiplexer = new InputMultiplexer();
		stage = new Stage(new ScreenViewport(), batch);
		multiplexer.addProcessor(stage);

		VisUI.load();
		console = new Console(VisUI.getSkin(), false);
		console.setKeyID(Input.Keys.F2);
//		console.setCommandExecutor(new GameCE(this));
		console.setSizePercent(100, 40);
		console.setPositionPercent(0, 60);
		multiplexer.addProcessor(console.getInputProcessor());

		Gdx.input.setInputProcessor(multiplexer);

		serializer = new StateSerializer(this);
		game = new Game(this);
		initState();
		createGUI();
		updateGUI();
	}

	private void initState () {
		SAVE_TIMER = prefs.getInteger(PREFS_SAVE_TIMER, SAVE_TIMER);

		String stateData = prefs.getString(PREFS_STATE, null);
		if (stateData == null) {
			game.init(new State(true));
			log(TAG, "New state!");
			return;
		}

		State state = serializer.fromJson(stateData);
		if (state == null) {
			game.init(new State(true));
			log(TAG, "State loading failed, new state!");
			return;
		}
		game.init(state);
		log(TAG, "State loaded!");
	}

	private void saveState () {
		State state = game.getState();
		String stateData = serializer.toJson(state);
//		log(TAG, stateData);
		prefs.putString(PREFS_STATE, stateData);
		prefs.flush();
		log(TAG, "State saved!");
	}

	ObjectMap<String, VisLabel> resourceLabels;
	ObjectMap<String, VisTextButton> buyButtons;
	public static int buyAmount = BUY_1;
	private void createGUI () {
		stage.clear();
		VisTable root = new VisTable(true);
		root.setFillParent(true);
		stage.addActor(root);

		final VisTextButton buyAmountButton = new VisTextButton("Buy "+buyAmount);
		buyAmountButton.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				switch (buyAmount) {
				case BUY_1:
					buyAmount = BUY_10;
					buyAmountButton.setText("Buy 10");
					break;
				case BUY_10:
					buyAmount = BUY_100;
					buyAmountButton.setText("Buy 100");
					break;
				case BUY_100:
//					buyAmount = BUY_ALL;
					buyAmount = BUY_1;
					buyAmountButton.setText("Buy ALL");
					break;
//				case BUY_ALL:
//					buyAmount = BUY_1;
//					buyAmountButton.setText("Buy 1");
//					break;
				}
			}
		});
		root.add(buyAmountButton);
		root.row();

		State state = game.getState();
		VisTable ressTable = new VisTable(true);
		resourceLabels = new ObjectMap<>();
		for (Resource resource:state.getResources()) {
			VisTable resTable = new VisTable(true);
			// TODO get i18n string for name
			VisLabel resLabel = new VisLabel(resource.name);
			resTable.add(resLabel);
			VisLabel resValue = new VisLabel(resource.toString());
			resTable.add(resValue);
			resourceLabels.put(resource.name, resValue);
			ressTable.add(resTable).row();
		}
		root.add(ressTable).row();

		VisTable buyTable = new VisTable(true);
		buyButtons = new ObjectMap<>();
		final int buyAmount = 1;
		for (final Building building:state.getBuildings()) {
			VisTable buildTable = new VisTable(true);
			// TODO get i18n string for name
			final VisTextButton buyBtn = new VisTextButton(building.name + " x" + building.getAmount());

			buyBtn.row();
			ObjectMap<String, BigDecimal> costs = building.calculateCost(1);
			String costsText = "";
			for (ObjectMap.Entry<String, BigDecimal> entry:costs.entries()) {
				costsText += entry.key + " x"+ NumberFormatter.formatEngineer(entry.value);
			}
			final VisLabel costLabel = new VisLabel("Buy " + buyAmount + " for " + costsText);

			buyBtn.add(costLabel);
			buildTable.add(buyBtn);
			buyBtn.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					if(building.buy(game.getState(), buyAmount)) {
						buyBtn.setText(building.name + " x" + building.getAmount());

						ObjectMap<String, BigDecimal> costs = building.calculateCost(buyAmount);
						String costsText = "";
						for (ObjectMap.Entry<String, BigDecimal> entry:costs.entries()) {
							costsText += entry.key + " x"+ NumberFormatter.formatEngineer(entry.value);
						}
						costLabel.setText("Buy " + buyAmount + " for " + costsText);
					}
				}
			});
			buyButtons.put(building.name, buyBtn);
			buyTable.add(buildTable).row();
		}
		root.add(buyTable).row();

		root.row();
		VisTextButton resetButton = new VisTextButton("Reset state!");
		resetButton.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				game.init(new State(true));
				saveState();
				createGUI();
			}
		});
		root.add(resetButton);
	}

	private float saveTimer = 0;

	@Override public void update (float delta) {
		if (game.update(delta)) {
			updateGUI();
		}
		saveTimer += delta;
		if (saveTimer >= SAVE_TIMER) {
			saveTimer -= SAVE_TIMER;
			saveState();
		}
		stage.act(delta);
	}

	private void updateGUI () {
		State state = game.getState();
		for (Resource resource:state.getResources()) {
			resourceLabels.get(resource.name).setText(resource.getAmountAsString());
		}

		for (Building building:state.getBuildings()) {

		}
	}

	@Override public void draw () {
		Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		batch.begin();
//
//		batch.end();

		game.draw(batch);
		stage.draw();
		console.draw();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
		console.refresh();
	}

	@Override public void pause () {
		saveState();
	}

	@Override public void resume () {

	}

	@Override public void dispose () {
		super.dispose();
		console.dispose();
		VisUI.dispose();
	}

	@Override public void log (String tag, String msg) {
		console.log(tag + " : " + msg);
		Gdx.app.log(tag, msg);
	}

	@Override public boolean handleMessage (Telegram msg) {
		return false;
	}
}
