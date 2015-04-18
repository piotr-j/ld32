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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.strongjoshua.console.Console;
import io.piotrjastrzebski.ld32.LD32;
import io.piotrjastrzebski.ld32.game.Game;
import io.piotrjastrzebski.ld32.game.ILogger;
import io.piotrjastrzebski.ld32.game.state.NumberFormatter;
import io.piotrjastrzebski.ld32.game.state.State;
import io.piotrjastrzebski.ld32.game.state.StateSerializer;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public class GameScreen extends BaseScreen implements ILogger {
	private final static String TAG = GameScreen.class.getSimpleName();
	private final static String PREFS = "LD32";
	private final static String PREFS_STATE = "state";
	private final static String PREFS_SAVE_TIMER = "save-timer";
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

		console = new Console(skin, false);
		console.setKeyID(Input.Keys.F2);
//		console.setCommandExecutor(new GameCE(this));
		console.setSizePercent(100, 40);
		console.setPositionPercent(0, 60);
		multiplexer.addProcessor(console.getInputProcessor());

		Gdx.input.setInputProcessor(multiplexer);

		serializer = new StateSerializer(this);
		game = new Game(this);
		initState();
		VisUI.load();
		createGUI();
		updateGUI();
	}

	private void initState () {
		SAVE_TIMER = prefs.getInteger(PREFS_SAVE_TIMER, SAVE_TIMER);

		String stateData = prefs.getString(PREFS_STATE, null);
		if (stateData == null) {
			game.init(new State());
			log(TAG, "New state!");
			return;
		}

		State state = serializer.fromJson(stateData);
		if (state == null) {
			game.init(new State());
			log(TAG, "State loading failed, new state!");
			return;
		}
		game.init(state);
		log(TAG, "State loaded!");
	}

	private void saveState () {
		State state = game.getState();
		String stateData = serializer.toJson(state);
		prefs.putString(PREFS_STATE, stateData);
		prefs.flush();
		log(TAG, "State saved!");
	}

	VisLabel sbValue;

	private void createGUI () {
		VisTable root = new VisTable(true);
		root.setFillParent(true);
		stage.addActor(root);
		VisTextButton testButton = new VisTextButton("test yo!");
		testButton.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				Gdx.app.log("", "clicked!");
			}
		});
		VisLabel sbLabel = new VisLabel("SpaceBux");
		sbValue = new VisLabel("");
		VisTable sbTable = new VisTable(true);
		sbTable.add(sbLabel);
		sbTable.add(sbValue);
		root.add(sbTable);
		root.row();
		root.add(testButton);
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
		String sbVal = NumberFormatter.formatEngineer(state.spaceBux);
		sbValue.setText(sbVal);
	}

	@Override public void draw () {
		Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		batch.end();

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
