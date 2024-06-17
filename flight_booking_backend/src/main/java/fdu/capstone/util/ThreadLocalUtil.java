package fdu.capstone.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
public class ThreadLocalUtil {

    private static ThreadLocal<Map<String, Object>> local = new InheritableThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>(16);
        }
    };

    public static <T> void set(String key, T value) {
        local.get().put(key, value);
    }

    public static <T> T get(String key) {
        return (T) local.get().get(key);
    }

    public static void remove(String key) {
        local.get().remove(key);
    }

    public static void remove() {
        local.remove();
    }
}
