package io.piotrjastrzebski.ld32.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import io.piotrjastrzebski.ld32.LD32;

public class HtmlLauncher extends GwtApplication {

	@Override public GwtApplicationConfiguration getConfig () {
		// 900x600 is max to embed on ld page
		// we want same aspect ratio as 1280/720
		return new GwtApplicationConfiguration(900, 506);
	}

	@Override public ApplicationListener getApplicationListener () {
		return new LD32();
	}
}
