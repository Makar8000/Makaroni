package news;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AionCache<T> implements Serializable {
	private static final long serialVersionUID = 1041148093988831306L;
	private static final int size = 10;
	private final Map<Long, T> map;
	private final Long[] keys;
	private int nextLoc = 0;

	public AionCache() {
		this.map = new HashMap<>();
		this.keys = new Long[size];
	}

	public void add(Long key, T value) {
		if (keys[nextLoc] != null)
			map.remove(keys[nextLoc]);

		map.put(key, value);
		keys[nextLoc] = key;
		nextLoc = (nextLoc + 1) % keys.length;
	}

	public boolean contains(Long key) {
		return map.containsKey(key);
	}

	public T get(Long key) {
		return map.get(key);
	}
}