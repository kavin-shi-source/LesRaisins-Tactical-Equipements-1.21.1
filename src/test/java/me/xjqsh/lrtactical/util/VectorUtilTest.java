package me.xjqsh.lrtactical.util;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VectorUtilTest {

    private static final double DELTA = 1e-6;

    @Test
    @DisplayName("Angle between parallel vectors should be 0")
    void angleBetween_parallel() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(2, 0, 0);
        assertEquals(0, VectorUtil.angleBetween(v1, v2), DELTA);
    }

    @Test
    @DisplayName("Angle between opposite vectors should be 180")
    void angleBetween_opposite() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(-1, 0, 0);
        assertEquals(180, VectorUtil.angleBetween(v1, v2), DELTA);
    }

    @Test
    @DisplayName("Angle between perpendicular vectors should be 90")
    void angleBetween_perpendicular() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(0, 0, 1);
        assertEquals(90, VectorUtil.angleBetween(v1, v2), DELTA);
    }

    @Test
    @DisplayName("Angle between 45 degree vectors")
    void angleBetween_45Degrees() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(1, 0, 1);
        assertEquals(45, VectorUtil.angleBetween(v1, v2), DELTA);
    }

    @Test
    @DisplayName("Angle between 60 degree vectors")
    void angleBetween_60Degrees() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(0.5, 0, Math.sqrt(3) / 2);
        assertEquals(60, VectorUtil.angleBetween(v1, v2), DELTA);
    }

    @Test
    @DisplayName("Angle between identical vectors should be 0")
    void angleBetween_identical() {
        Vec3 v = new Vec3(3, 4, 5);
        assertEquals(0, VectorUtil.angleBetween(v, v), DELTA);
    }

    @Test
    @DisplayName("Angle should be commutative")
    void angleBetween_commutative() {
        Vec3 v1 = new Vec3(1, 2, 3);
        Vec3 v2 = new Vec3(-4, 5, -6);
        double a = VectorUtil.angleBetween(v1, v2);
        double b = VectorUtil.angleBetween(v2, v1);
        assertEquals(a, b, DELTA);
    }

    @Test
    @DisplayName("Angle between vectors with y-component should work")
    void angleBetween_withYComponent() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(1, 1, 0);
        assertEquals(45, VectorUtil.angleBetween(v1, v2), DELTA);
    }

    @Test
    @DisplayName("Floating point edge case should not produce NaN")
    void angleBetween_noNaN() {
        Vec3 v1 = new Vec3(1, 0, 0);
        Vec3 v2 = new Vec3(1, 0, 0);
        double result = VectorUtil.angleBetween(v1, v2);
        assertFalse(Double.isNaN(result));
    }
}
