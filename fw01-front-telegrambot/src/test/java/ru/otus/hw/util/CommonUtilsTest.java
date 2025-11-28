package ru.otus.hw.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommonUtilsTest methods should: ")
@Slf4j
class CommonUtilsTest {

    private final Map<String, String> map = Map.of(
            "Key1", "Value1",
            "Key2", "Value2",
            "Key3", "Value3");

    @Test
    @DisplayName("shuffle given map")
    void shuffleMap() {
        Map<String, String> shuffled = CommonUtils.shuffleMap(map);
        shuffled.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
        assertThat(shuffled)
                .isNotEmpty()
                .hasSize(3)
                .containsAllEntriesOf(map);
    }

    @Test
    @DisplayName("should return a new sorted list")
    void sortingStringList() {
        List<String> actualList = CommonUtils.sortingStringList(List.of("Техника", "Образование", "Поликлиника"));
        assertThat(actualList)
                .isNotEmpty()
                .hasSize(3)
                .isSorted()
                .containsExactly("Образование", "Поликлиника", "Техника");
    }
}
