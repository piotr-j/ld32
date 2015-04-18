package io.piotrjastrzebski.ld32.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.piotrjastrzebski.ld32.LD32;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// 900x600 is max to embed on ld page
		// we want same aspect ratio as 1280/720
		config.width = 900;
		config.height = 506;
		new LwjglApplication(new LD32(), config);
	}
}
