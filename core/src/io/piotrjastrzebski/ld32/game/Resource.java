package io.piotrjastrzebski.ld32.game;

import io.piotrjastrzebski.ld32.game.state.NumberFormatter;

import java.math.BigDecimal;

/**
 * Created by EvilEntity on 18/04/2015.
 */
public class Resource {
	// internal name
	public String name;
	// current amount of this resourceType
	private BigDecimal amount;
	// multiplier for this resources when adding values
	private BigDecimal multiplier;

	public Resource () {

	}

	public Resource (String name) {
		this.name = name;
		amount = BigDecimal.ZERO;
		multiplier = BigDecimal.ONE;
	}

	public BigDecimal getMultiplier () {
		return multiplier;
	}

	public void setMultiplier (BigDecimal multiplier) {
		this.multiplier = multiplier;
	}

	/**
	 * add to current amount, will be multiplied by #getMultiplier()
	 */
	public void add (BigDecimal value) {
		amount = amount.add(value.multiply(multiplier));
	}

	/**
	 * Subtract from current amount, clamped [0, inf)
	 */
	public void subtract (BigDecimal value) {
		amount = amount.subtract(value);
		if (amount.signum() == -1) {
			amount = BigDecimal.ZERO;
		}
	}

	public BigDecimal getAmount () {
		return amount;
	}

	public String getAmountAsString () {
		return NumberFormatter.formatEngineer(amount);
	}

	@Override public String toString () {
		return NumberFormatter.formatEngineer(amount);
	}
}
