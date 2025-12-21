package ru.otus.hw.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonUtils {

    /**
     * Shuffles the entries of the given map randomly while preserving the key-value associations.
     * The returned map maintains the shuffled order of entries.
     *
     * @param map the map whose entries are to be shuffled; may be null or empty
     * @return a new LinkedHashMap containing the same entries as the input map,
     *         but in a randomly shuffled order. Returns an empty map if the input is null or empty.
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     */
    public static <K, V> Map<K, V> shuffleMap(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return new LinkedHashMap<>();
        }

        List<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        Collections.shuffle(entries);

        Map<K, V> result = new LinkedHashMap<>(map.size());
        entries.forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    public static List<String> sortingStringList(List<String> list) {
        return CollectionUtils.isEmpty(list) ? List.of() : list.stream().sorted(String::compareToIgnoreCase).toList();
    }

    public static void checkStringIsPresent(String string, String name) {
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException("⛔ %s name cannot be null or empty".formatted(name));
        }
    }


}
