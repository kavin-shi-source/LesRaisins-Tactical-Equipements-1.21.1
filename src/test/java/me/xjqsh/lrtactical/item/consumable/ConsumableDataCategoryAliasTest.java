package me.xjqsh.lrtactical.item.consumable;

import me.xjqsh.lrtactical.item.consumable.ConsumableData.RemoveEffectSelector.CategoryAlias;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ConsumableDataCategoryAliasTest {

    @ParameterizedTest
    @CsvSource({
        "@beneficial, BENEFICIAL",
        "@harmful, HARMFUL",
        "@neutral, NEUTRAL"
    })
    @DisplayName("byId should return correct CategoryAlias for valid inputs")
    void byId_validInputs(String id, String expected) {
        assertEquals(CategoryAlias.valueOf(expected), CategoryAlias.byId(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "", "@unknown", "beneficial", "@@"})
    @DisplayName("byId should return null for invalid inputs")
    void byId_invalidInputs(String id) {
        assertNull(CategoryAlias.byId(id));
    }

    @Test
    @DisplayName("byId should return null for null input")
    void byId_nullInput() {
        assertNull(CategoryAlias.byId(null));
    }

    @Test
    @DisplayName("CategoryAlias enum values should be exactly 3")
    void categoryAlias_count() {
        assertEquals(3, CategoryAlias.values().length);
    }

    @ParameterizedTest
    @CsvSource({
        "BENEFICIAL, @beneficial",
        "HARMFUL, @harmful",
        "NEUTRAL, @neutral"
    })
    @DisplayName("byId roundtrip should work for all values")
    void byId_roundtrip(CategoryAlias alias, String id) {
        assertEquals(alias, CategoryAlias.byId(id));
    }
}
