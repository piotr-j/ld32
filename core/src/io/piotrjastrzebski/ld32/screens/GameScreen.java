package io.piotrjastrzebski.ld32.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.DialogUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
//import com.strongjoshua.console.Console;
import io.piotrjastrzebski.ld32.Constants;
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
//	private Console console;
	private final InputMultiplexer multiplexer;
	private TextureAtlas.AtlasRegion background;

	private StateSerializer serializer;

	public GameScreen (LD32 base) {
		super(base);
		background = assets.getRegion("bg");
		prefs = Gdx.app.getPreferences(PREFS);
		multiplexer = new InputMultiplexer();
		stage = new Stage(new ScreenViewport(), batch);
		multiplexer.addProcessor(stage);

		VisUI.load();
		// enabled markup so we can color text
		BitmapFont defFont = VisUI.getSkin().get("default-font", BitmapFont.class);
		defFont.setMarkupEnabled(true);
//		defFont.getData().markupEnabled = true;
		BitmapFont smallFont = VisUI.getSkin().get("small-font", BitmapFont.class);
		smallFont.setMarkupEnabled(true);
//		smallFont.getData().markupEnabled = true;

//		console = new Console(VisUI.getSkin(), false);
//		console.setKeyID(Input.Keys.F2);
////		console.setCommandExecutor(new GameCE(this));
//		console.setSizePercent(100, 40);
//		console.setPositionPercent(0, 60);
//		multiplexer.addProcessor(console.getInputProcessor());

		Gdx.input.setInputProcessor(multiplexer);

		serializer = new StateSerializer(this);
		game = new Game(this, assets);
		initState();
		createGUI();
		updateGUI();
		DialogUtils.showOKDialog(stage, "Welcome!",
			"Click on empty space to get some resources!\nBuy stuff to defend from aliens!");
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

	public int buyAmount = BUY_1;
	public final static String TAB_PRODUCTION = "production";
	public final static String TAB_DEFENSE = "defense";
	public final static String TAB_TECH = "tech";

	private ObjectMap<String, VisTable> tabs;
	private VisTable tabSelectors;
	private VisTable tabContainer;
	private ButtonGroup<VisTextButton> tabButtonGroup;
	VisLabel tapLabel;
	private void createGUI () {
		stage.clear();

		tapLabel = new VisLabel();
		stage.addActor(tapLabel);
		tapLabel.setColor(1,1,1,0);

		final VisTable root = new VisTable(true);
		root.setFillParent(true);
		stage.addActor(root);
		root.setTouchable(Touchable.enabled);
		root.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				if (root == event.getTarget()) {
					tapped(x, y);
				}
			}
		});
		final VisTextButton soundToggle = new VisTextButton("Sound OFF", "toggle");
		soundToggle.setChecked(false);
		soundToggle.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				boolean sound = !assets.isSoundEnabled();
				assets.setSoundEnabled(sound);
				if (sound) {
					soundToggle.setText("Sound ON");
				} else {
					soundToggle.setText("Sound OFF");
				}
				soundToggle.setChecked(sound);
				playBtnSound();
			}
		});
		VisTable topContainer = new VisTable(true);
		topContainer.add(soundToggle).pad(10);
		topContainer.add(createResourceGUI()).expandX().fillX();
		root.add(topContainer).expandX().fillX();
		root.row();

		tabButtonGroup = new ButtonGroup<>();
		tabButtonGroup.setMinCheckCount(1);
		tabButtonGroup.setMaxCheckCount(1);
		tabSelectors = new VisTable(true);
		VisTextButton selectProd = new VisTextButton(TAB_PRODUCTION, "toggle");
		selectProd.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				playBtnSound();
				selectTab(TAB_PRODUCTION);
			}
		});
		tabButtonGroup.add(selectProd);
		tabSelectors.add(selectProd);

		VisTextButton selectTech = new VisTextButton(TAB_TECH, "toggle");
		selectTech.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				playBtnSound();
				selectTab(TAB_TECH);
			}
		});
		tabButtonGroup.add(selectTech);
		tabSelectors.add(selectTech);

		VisTextButton selectDef = new VisTextButton(TAB_DEFENSE, "toggle");
		selectDef.addListener(new ClickListener(){
			@Override public void clicked (InputEvent event, float x, float y) {
				playBtnSound();
				selectTab(TAB_DEFENSE);
			}
		});
		tabButtonGroup.add(selectDef);
		tabSelectors.add(selectDef);
		root.add(tabSelectors).fillX().expandX();
		root.row();

		tabs = new ObjectMap<>();
		tabContainer = new VisTable(true);
		tabs.put(TAB_PRODUCTION, createProductionTab());
		tabs.put(TAB_DEFENSE, createDefenseTab());
		tabs.put(TAB_TECH, createTechTab());
		selectTab(TAB_PRODUCTION);
		root.add(tabContainer).fill().expand();
	}
	private void tapped (float x, float y) {
		if (game.isVisible()){
			// todo get correct coordinates at fire there or something
			game.tap();
		} else {
			// TODO amount from tech or something
			State state = game.getState();
			Building iceB = state.getBuilding(Constants.Building.ICE_HARVESTER);
			Building sbB = state.getBuilding(Constants.Building.SPACE_BANK);
			Resource spaceBux = state.getResource(Constants.Resources.SPACE_BUX);
			long sbAmount = MathUtils.clamp((long)(sbB.getAmount() * 0.1f), 1L, Long.MAX_VALUE);
			long iceAmount = MathUtils.clamp((long)(iceB.getAmount()*0.1f), 1L, Long.MAX_VALUE);
			spaceBux.add(BigDecimal.valueOf(sbAmount));
			Resource ice = state.getResource(Constants.Resources.ICE);
			ice.add(BigDecimal.valueOf(iceAmount));
			final VisLabel label = new VisLabel();
			stage.addActor(label);
			label.setText("+"+sbAmount+" SB +"+iceAmount+" ICE");
			label.setPosition(x + label.getWidth() / 2, y + label.getHeight() / 2);
			label.clearActions();
			label.setColor(0, 0.7f, 0, 1);
			label.addAction(Actions.sequence(Actions.fadeOut(1), Actions.removeActor()));
			updateResources();
		}
	}

	private String currentTab = "";
	private void selectTab(String name) {
		if (currentTab.equals(name)) return;
		currentTab = name;
		tabContainer.clear();
		tabContainer.add(tabs.get(name)).expand().fill();
		switch (name) {
		case TAB_DEFENSE:
			log(TAG, "Selected " + TAB_DEFENSE);
			game.visible();
			break;

		case TAB_TECH:
			log(TAG, "Selected " + TAB_TECH);
			game.hidden();
			break;

		case TAB_PRODUCTION:
			log(TAG, "Selected " + TAB_PRODUCTION);
			game.hidden();
			break;
		}
	}

	private VisTable createProductionTab() {
		VisTable tab = new VisTable(true);

		final VisTextButton buyAmountButton = new VisTextButton("Buy " + buyAmount);
		buyAmountButton.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				playBtnSound();
				switch (buyAmount) {
				// TODO support buy all
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
					buyAmountButton.setText("Buy 1");
					break;
//				case BUY_ALL:
//					buyAmount = BUY_1;
//					buyAmountButton.setText("Buy 1");
//					break;
				}
				updateBuyButtons();
			}
		});
		tab.add(buyAmountButton);
		tab.row();

		tab.add(createBuyGUI()).row();
		tab.row();

		VisTextButton resetButton = new VisTextButton("Reset state!");
		resetButton.addListener(new ClickListener() {
			@Override public void clicked (InputEvent event, float x, float y) {
				game.init(new State(true));
				saveState();
				createGUI();
				updateGUI();
			}
		});
		tab.add(resetButton);

		return tab;
	}

	private VisTable createDefenseTab() {
		VisTable tab = new VisTable(true);

		return tab;
	}

	private VisTable createTechTab() {
		VisTable tab = new VisTable(true);
		tab.add(new VisLabel("NOT YET IMPLEMENTED"));

		return tab;
	}

	ObjectMap<String, VisLabel> resourceLabels;

	private VisTable createResourceGUI () {
		State state = game.getState();

		resourceLabels = new ObjectMap<>();

		VisTable container = new VisTable(true);
		for (Resource resource : state.getResources()) {
			VisTable resTable = new VisTable(true);

			// TODO get i18n string for name
			VisLabel resLabel = new VisLabel(resource.name);
			resTable.add(resLabel);
			VisLabel resValue = new VisLabel(resource.getAmountAsString());
			resTable.add(resValue);

			resourceLabels.put(resource.name, resValue);
			container.add(resTable);
		}
		return container;
	}

	ObjectMap<String, VisTextButton> buyButtons;
	ObjectMap<String, VisLabel> buyCostLabels;

	private VisTable createBuyGUI () {
		State state = game.getState();

		buyButtons = new ObjectMap<>();
		buyCostLabels = new ObjectMap<>();

		VisTable buyTable = new VisTable(true);
		int id = 0;
		for (final Building building : state.getBuildings()) {
			VisTable buildTable = new VisTable(true);
			// TODO get i18n string for name
			final VisTextButton buyBtn = new VisTextButton("");
			buyBtn.row();
			final VisLabel costLabel = new VisLabel("");
			buyBtn.add(costLabel);
			if (!building.hasNext()) {
				buyBtn.setDisabled(true);
			}
			buildTable.add(buyBtn);
			buyBtn.addListener(new ClickListener() {
				@Override public void clicked (InputEvent event, float x, float y) {
					buyBuilding(building);
					if (!building.hasNext()) {
						buyBtn.setDisabled(true);
					}
					playBtnSound();
				}
			});

			buyButtons.put(building.name, buyBtn);
			buyCostLabels.put(building.name, costLabel);
			buyTable.add(buildTable);
			if (id > 0 && id % 2 != 0) {
				buyTable.row();
			}
			id++;
		}
		return buyTable;
	}

	private void buyBuilding(Building building) {
		if (building.buy(game.getState(), buyAmount)) {
			updateResources();
			updateBuyButton(building);
		}
	}

	private void updateBuyButton(Building building) {
		VisTextButton button = buyButtons.get(building.name);
		VisLabel label = buyCostLabels.get(building.name);
		button.setText(building.name + " " + building.getAmount());

		State state = game.getState();

		ObjectMap<String, BigDecimal> costs = building.calculateCost(buyAmount);
		String costsText = "";
		for (ObjectMap.Entry<String, BigDecimal> entry : costs.entries()) {
			// TODO better colors [#xxxxxx]
			String tint = "[GREEN]";
			if (state.getResource(entry.key).getAmount().compareTo(entry.value) < 0) {
				tint = "[RED]";
			}
			costsText += entry.key + " x " + tint + NumberFormatter.formatEngineer(entry.value)+"[] ";
		}
		label.setText("Buy " + buyAmount + "  " + costsText);
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
		updateResources();
		updateBuyButtons();
	}

	private void updateResources() {
		State state = game.getState();
		for (Resource resource : state.getResources()) {
			resourceLabels.get(resource.name).setText(resource.getAmountAsString());
		}
	}

	private void updateBuyButtons() {
		State state = game.getState();
		for (Building building : state.getBuildings()) {
			updateBuyButton(building);
		}
	}

	@Override public void draw () {
		Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(stage.getCamera().combined);
		batch.begin();
		batch.disableBlending();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

		batch.enableBlending();
		game.draw(batch);
		stage.draw();
//		console.draw();
	}

	@Override public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
//		console.refresh();
		game.resize(width, height);
	}

	@Override public void pause () {
		saveState();
	}

	@Override public void resume () {

	}

	@Override public void dispose () {
		super.dispose();
//		console.dispose();
		VisUI.dispose();
	}

	@Override public void log (String tag, String msg) {
//		console.log(tag + " : " + msg);
		Gdx.app.log(tag, msg);
	}

	@Override public boolean handleMessage (Telegram msg) {
		return false;
	}
}
