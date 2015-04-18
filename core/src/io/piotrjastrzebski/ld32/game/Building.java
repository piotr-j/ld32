package io.piotrjastrzebski.ld32.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import io.piotrjastrzebski.ld32.game.state.State;
import io.piotrjastrzebski.ld32.screens.GameScreen;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Single node that generates some resources per tick and can be build more of
 * Created by EvilEntity on 18/04/2015.
 */
public class Building {
	// internal name
	public String name;
	// 2 billion should be enough for now
	int amount = 0;

	// how much does it cost to build next one
	ObjectMap<String, BigDecimal> initialCosts;

	Array<ResourceGenerator> resourceGens;

	public Building () {
	}

	public Building (String name) {
		this.name = name;
		resourceGens = new Array<>();
		initialCosts = new ObjectMap<>();
	}

	public void addAmount (int amount) {
		this.amount += amount;
	}

	public void subtractAmount (int amount) {
		this.amount -= amount;
	}

	public void addGenerator (ResourceGenerator generator) {
		resourceGens.add(generator);
	}

	public void removeGenerator (String typeToRemove) {
		Iterator<ResourceGenerator> iterator = resourceGens.iterator();
		while (iterator.hasNext()) {
			ResourceGenerator next = iterator.next();
			if (next.resourceType.equals(name)) {
				iterator.remove();
			}
		}
	}

	public void addInitialCost (String type, long amount) {
		initialCosts.put(type, BigDecimal.valueOf(amount));
	}

	public void addInitialCost (String type, BigDecimal amount) {
		initialCosts.put(type, amount);
	}

	private transient ObjectMap<String, BigDecimal> costToBuy = new ObjectMap<>();

	public ObjectMap<String, BigDecimal> calculateCost (int toBuy) {
		// TODO handle buy all
		if (toBuy == GameScreen.BUY_ALL) {
			toBuy = 1;
		}
		costToBuy.clear();
		ObjectMap.Entries<String, BigDecimal> costs = initialCosts.entries();
		for (ObjectMap.Entry<String, BigDecimal> costData : costs) {
			BigDecimal cost = costData.value.multiply(BigDecimal.valueOf((amount + toBuy) * toBuy));
			costToBuy.put(costData.key, cost);
		}
		return costToBuy;
	}

	public void tick (State state) {
		for (ResourceGenerator resourceGen : resourceGens) {
			resourceGen.tick(state, amount);
		}
	}

	public boolean buy (State state, int amount) {
		ObjectMap<String, BigDecimal> costs = calculateCost(amount);
		// check if we have required resources
		for (ObjectMap.Entry<String, BigDecimal> costData : costs) {
			Resource resource = state.getResource(costData.key);
			// cant buy if we have less then needed
			if (resource.getAmount().compareTo(costData.value) == -1) {
				return false;
			}
		}
		// we have enough resource, subtract amounts
		for (ObjectMap.Entry<String, BigDecimal> costData : costs) {
			Resource resource = state.getResource(costData.key);
			resource.subtract(costData.value);
		}
		// add requested amount
		addAmount(amount);
		return true;
	}

	public int getAmount () {
		return amount;
	}
}
