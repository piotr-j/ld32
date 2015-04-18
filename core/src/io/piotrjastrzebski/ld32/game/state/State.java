package io.piotrjastrzebski.ld32.game.state;

import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.ld32.game.Building;
import io.piotrjastrzebski.ld32.game.Resource;

import java.math.BigDecimal;

/**
 * State of the game
 * Created by EvilEntity on 18/04/2015.
 */
public class State {
	/**
	 * TimeStamp can't be long, as GWT reflection doesn't support it.
	 */
	public BigDecimal ts;

	private Array<Resource> resources;
	private Array<Building> buildings;

	transient private boolean isFresh = false;

	public State () {
		ts = currentTS();
		resources = new Array<>();
		buildings = new Array<>();
	}

	public State (boolean fresh) {
		this();
		isFresh = true;
	}

	public void clear() {
		resources.clear();
		buildings.clear();
	}

	public void updateTS () {
		ts = currentTS();
	}

	public BigDecimal currentTS () {
		return BigDecimal.valueOf(System.currentTimeMillis());
	}

	public BigDecimal getTS () {
		return ts;
	}

	@Override public String toString () {
		return "State";
	}

	public void addResource(Resource resource) {
		if (getResource(resource.name) == null) {
			resources.add(resource);
		}
	}

	public Resource getResource(String name) {
		for (Resource resource: resources) {
			if (resource.name.equals(name)) {
				return resource;
			}
		}
		return null;
	}

	public void addBuilding(Building building) {
		if (getBuilding(building.name) == null) {
			buildings.add(building);
		}
	}

	public Building getBuilding (String name) {
		for (Building building: buildings) {
			if (building.name.equals(name)) {
				return building;
			}
		}
		return null;
	}

	public Array<Resource> getResources () {
		return resources;
	}

	public Array<Building> getBuildings () {
		return buildings;
	}

	public boolean isFresh () {
		return isFresh;
	}
}
