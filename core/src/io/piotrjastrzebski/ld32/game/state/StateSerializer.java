package io.piotrjastrzebski.ld32.game.state;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;
import io.piotrjastrzebski.ld32.game.ILogger;

import java.math.BigDecimal;

/**
 * Used to load and save game state
 * Created by EvilEntity on 18/04/2015.
 */
public class StateSerializer {
	private final static String TAG = StateSerializer.class.getSimpleName();
	private final static String KEY = "goon";
	protected ILogger logger;
	protected Json json;
	protected StringBuilder sb;
	private boolean useXor = false;

	public StateSerializer (ILogger logger) {
		this.logger = logger;
		json = new Json(JsonWriter.OutputType.minimal);
		// save all the data so we get defaults in the json
		json.setUsePrototypes(false);
		json.setSerializer(BigDecimal.class, new Json.Serializer<BigDecimal>() {
			@Override public void write (Json json, BigDecimal bigDecimal, Class knownType) {
				json.writeValue(bigDecimal.toString());
			}

			@Override public BigDecimal read (Json json, JsonValue jsonData, Class type) {
				return new BigDecimal(jsonData.asString());
			}
		});
		sb = new StringBuilder();
	}

	public String toJson (State state) {
		// this should never fail in production
		String jsonState = json.toJson(state);
		byte[] xor = xor(jsonState).getBytes();
		return Base64.encodeBytes(xor);
	}

	public State fromJson (String data) {
		byte[] decoded;
		try {
			// can fail if data contains invalid characters
			decoded = Base64.decode(data.getBytes());
		} catch (IllegalArgumentException e) {
			logger.log(TAG, "Loading from data failed");
			return null;
		}

		String b64d = xor(new String(decoded));

		State gameState;
		try {
			gameState = json.fromJson(State.class, b64d);
		} catch (SerializationException e) {
			logger.log(TAG, "Loading from data failed");
			return null;
		}
		return gameState;
	}

	private String xor (String input) {
		if (!useXor)
			return input;
		sb.setLength(0);
		for (int i = 0; i < input.length(); i++) {
			sb.append((char)(input.charAt(i) ^ KEY.charAt(i % KEY.length())));
		}
		return sb.toString();
	}

	public void setUseXor (boolean useXor) {
		this.useXor = useXor;
	}
}
