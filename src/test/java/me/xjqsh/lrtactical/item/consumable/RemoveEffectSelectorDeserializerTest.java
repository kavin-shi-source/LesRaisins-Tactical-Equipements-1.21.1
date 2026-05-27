package me.xjqsh.lrtactical.item.consumable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import me.xjqsh.lrtactical.item.consumable.ConsumableData.RemoveEffectSelector;
import me.xjqsh.lrtactical.item.consumable.ConsumableData.RemoveEffectSelector.Deserializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoveEffectSelectorDeserializerTest {

    private Deserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new Deserializer();
    }

    @Test
    @DisplayName("Should parse @harmful as harmful category selector")
    void deserialize_harmfulCategory() {
        JsonElement json = new JsonPrimitive("@harmful");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertTrue(result.isCategory());
        assertEquals(MobEffectCategory.HARMFUL, result.getCategory());
        assertNull(result.getEffect());
    }

    @Test
    @DisplayName("Should parse @beneficial as beneficial category selector")
    void deserialize_beneficialCategory() {
        JsonElement json = new JsonPrimitive("@beneficial");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertTrue(result.isCategory());
        assertEquals(MobEffectCategory.BENEFICIAL, result.getCategory());
        assertNull(result.getEffect());
    }

    @Test
    @DisplayName("Should parse @neutral as neutral category selector")
    void deserialize_neutralCategory() {
        JsonElement json = new JsonPrimitive("@neutral");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertTrue(result.isCategory());
        assertEquals(MobEffectCategory.NEUTRAL, result.getCategory());
        assertNull(result.getEffect());
    }

    @Test
    @DisplayName("Should parse valid effect ID as effect selector")
    void deserialize_validEffectId() {
        JsonElement json = new JsonPrimitive("minecraft:poison");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertFalse(result.isCategory());
        assertEquals(ResourceLocation.tryParse("minecraft:poison"), result.getEffect());
        assertNull(result.getCategory());
    }

    @Test
    @DisplayName("Should parse effect ID with custom namespace")
    void deserialize_customNamespace() {
        JsonElement json = new JsonPrimitive("mymod:custom_effect");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertFalse(result.isCategory());
        assertEquals(ResourceLocation.tryParse("mymod:custom_effect"), result.getEffect());
    }

    @Test
    @DisplayName("Should throw JsonParseException for non-string input")
    void deserialize_nonStringInput() {
        JsonElement json = new JsonPrimitive(42);
        assertThrows(JsonParseException.class, () -> deserializer.deserialize(json, null, null));
    }

    @Test
    @DisplayName("Should parse empty string as effect selector (ResourceLocation.tryParse accepts it)")
    void deserialize_emptyString() {
        JsonElement json = new JsonPrimitive("");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertNotNull(result);
        assertFalse(result.isCategory());
    }

    @Test
    @DisplayName("Should throw JsonParseException for invalid input")
    void deserialize_invalidString() {
        JsonElement json = new JsonPrimitive("not@valid");
        assertThrows(JsonParseException.class, () -> deserializer.deserialize(json, null, null));
    }

    @Test
    @DisplayName("Should parse effect ID without namespace as minecraft: prefixed effect selector")
    void deserialize_idWithoutNamespace() {
        JsonElement json = new JsonPrimitive("poison");
        RemoveEffectSelector result = deserializer.deserialize(json, null, null);
        assertNotNull(result);
        assertFalse(result.isCategory());
        assertEquals(ResourceLocation.withDefaultNamespace("poison"), result.getEffect());
    }
}
