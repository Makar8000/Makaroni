package news;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AionCache<T> implements Serializable {
    private static final long serialVersionUID = 1041148093988831306L;
    private static final int size = 40;
    private final Map<String, T> map;
    private final String[] keys;
    private int nextLoc = 0;

    public AionCache() {
        this.map = new HashMap<>();
        this.keys = new String[size];
    }

    public void add(String key, T value) {
        if (keys[nextLoc] != null)
            map.remove(keys[nextLoc]);

        map.put(key, value);
        keys[nextLoc] = key;
        nextLoc = (nextLoc + 1) % keys.length;

        System.out.println();
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }
}