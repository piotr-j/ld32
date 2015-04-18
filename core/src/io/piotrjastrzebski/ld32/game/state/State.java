package io.piotrjastrzebski.ld32.game.state;

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
	public BigDecimal spaceBux;

	public State () {
		ts = currentTS();
		spaceBux = BigDecimal.valueOf(0L);
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
		return super.toString();
	}

	public void addSpaceBux (long toAdd) {
		spaceBux = spaceBux.add(getBC(toAdd));
	}

	private BigDecimal getBC (long val) {
		return BigDecimal.valueOf(val);
	}
}
