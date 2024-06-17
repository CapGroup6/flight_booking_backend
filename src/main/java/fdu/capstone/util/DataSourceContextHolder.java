package fdu.capstone.util;

/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */
public class DataSourceContextHolder {



    private static final ThreadLocal<String> CACHE = new ThreadLocal<>();

    public static void setDataSourceType(String dataSourceType) {
        CACHE.set(dataSourceType);
    }

    public static String getDataSourceType() {
        return CACHE.get();
    }

    public static void clear() {
        CACHE.remove();
    }
}
