package me.xjqsh.lrtactical.compat.jei;

import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

public class LrJeiSubtype {

    private static final ISubtypeInterpreter<ItemStack> MELEE_SUBTYPE = new ISubtypeInterpreter<>() {
        @Override
        public String apply(ItemStack itemStack, UidContext context) {
            IMeleeWeapon melee = IMeleeWeapon.of(itemStack);
            if (melee != null) {
                return melee.getId(itemStack).toString();
            }
            return "";
        }
    };

    private static final ISubtypeInterpreter<ItemStack> THROWABLE_SUBTYPE = new ISubtypeInterpreter<>() {
        @Override
        public String apply(ItemStack itemStack, UidContext context) {
            IThrowable throwable = IThrowable.of(itemStack);
            if (throwable != null) {
                return throwable.getId(itemStack).toString();
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
