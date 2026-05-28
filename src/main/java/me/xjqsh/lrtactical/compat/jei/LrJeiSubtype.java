package me.xjqsh.lrtactical.compat.jei;

import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LrJeiSubtype {

    private record SubtypeId(ResourceLocation id) {}

    private static final ISubtypeInterpreter<ItemStack> MELEE_SUBTYPE = new ISubtypeInterpreter<>() {
        @Override
        @Nullable
        public Object getSubtypeData(ItemStack itemStack, UidContext context) {
            IMeleeWeapon melee = IMeleeWeapon.of(itemStack);
            if (melee != null) {
                ResourceLocation id = melee.getId(itemStack);
                if (id != null) {
                    return new SubtypeId(id);
                }
            }
            return null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
            IMeleeWeapon melee = IMeleeWeapon.of(itemStack);
            if (melee != null) {
                ResourceLocation id = melee.getId(itemStack);
                if (id != null) {
                    return id.toString();
                }
            }
            return "";
        }
    };

    private static final ISubtypeInterpreter<ItemStack> THROWABLE_SUBTYPE = new ISubtypeInterpreter<>() {
        @Override
        @Nullable
        public Object getSubtypeData(ItemStack itemStack, UidContext context) {
            IThrowable throwable = IThrowable.of(itemStack);
            if (throwable != null) {
                ResourceLocation id = throwable.getId(itemStack);
                if (id != null) {
                    return new SubtypeId(id);
                }
            }
            return null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext context) {
            IThrowable throwable = IThrowable.of(itemStack);
            if (throwable != null) {
                ResourceLocation id = throwable.getId(itemStack);
                if (id != null) {
                    return id.toString();
                }
            }
            return "";
        }
    };

    public static ISubtypeInterpreter<ItemStack> getMeleeSubtype() {
        return MELEE_SUBTYPE;
    }

    public static ISubtypeInterpreter<ItemStack> getThrowableSubtype() {
        return THROWABLE_SUBTYPE;
    }
}
