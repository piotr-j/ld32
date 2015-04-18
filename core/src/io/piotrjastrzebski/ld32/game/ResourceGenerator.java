package io.piotrjastrzebski.ld32.game;

import io.piotrjastrzebski.ld32.game.state.State;

import java.math.BigDecimal;

/**
 * Generates certain amount of resource each tick
 * Created by EvilEntity on 18/04/2015.
 */
public class ResourceGenerator {
	// internal name
	public String resourceType;
	// amount to add per tick
	private BigDecimal amount;
	// multiplier when adding values
	private BigDecimal multiplier;

	public ResourceGenerator () {
	}

	public ResourceGenerator (String resource) {
		this.resourceType = resource;
		amount = BigDecimal.ZERO;
		multiplier = BigDecimal.ONE;
	}

	public void setMultiplier (long multiplier) {
		this.multiplier = BigDecimal.valueOf(multiplier);
	}

	public void setMultiplier (BigDecimal multiplier) {
		this.multiplier = multiplier;
	}

	public BigDecimal getMultiplier () {
		return multiplier;
	}

	public void setAmount (long amount) {
		this.amount = BigDecimal.valueOf(amount);
	}

	public void setAmount (BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getAmount () {
		return amount;
	}

	public void tick (State state, long mult) {
		Resource resource = state.getResource(resourceType);
		if (mult == 0)
			return;
		if (amount.equals(BigDecimal.ZERO))
			return;
		if (multiplier.equals(BigDecimal.ZERO))
			return;
		resource.add(amount.multiply(multiplier).multiply(BigDecimal.valueOf(mult)));
	}
}
