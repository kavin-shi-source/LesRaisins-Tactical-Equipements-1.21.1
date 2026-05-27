package me.xjqsh.lrtactical.init;


import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import me.xjqsh.lrtactical.api.item.IMeleeWeapon;
import me.xjqsh.lrtactical.api.item.IThrowable;
import me.xjqsh.lrtactical.item.ConsumableItem;
import me.xjqsh.lrtactical.item.DetonatorItem;
import me.xjqsh.lrtactical.item.FlashShieldItem;
import me.xjqsh.lrtactical.item.MeleeItem;
import me.xjqsh.lrtactical.item.ThrowableItem;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EquipmentMod.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THROWABLE_TAB = TABS.register("throwable",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.throwable"))
                    .icon(ModItems::getThrowableIcon)
                    .displayItems(ModItems::fillThrowables)
                    .build()
    );
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MELEE_TAB = TABS.register("melee",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.melee"))
                    .icon(ModItems::getMeleeIcon)
                    .displayItems(ModItems::fillMeleeWeapons)
                    .withTabsBefore(THROWABLE_TAB.getId())
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONSUMABLE_TAB = TABS.register("consumable",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.lrtactical.consumable"))
                    .icon(ModItems::getConsumableIcon)
                    .displayItems(ModItems::fillConsumables)
                    .withTabsBefore(MELEE_TAB.getId())
                    .build()
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, EquipmentMod.MOD_ID);
    public static DeferredHolder<Item, ThrowableItem> THROWABLE = ITEMS.register("throwable", ThrowableItem::new);
    public static DeferredHolder<Item, MeleeItem> MELEE = ITEMS.register("melee", MeleeItem::new);
    public static DeferredHolder<Item, FlashShieldItem> FLASH_SHIELD = ITEMS.register("flash_shield", FlashShieldItem::new);
    public static DeferredHolder<Item, DetonatorItem> DETONATOR = ITEMS.register("detonator", DetonatorItem::new);
    public static DeferredHolder<Item, ConsumableItem> CONSUMABLE = ITEMS.register("consumable", ConsumableItem::new);

    public static ItemStack getThrowableIcon() {
        ItemStack stack = new ItemStack(THROWABLE.get());
        IThrowable iThrowable = IThrowable.of(stack);
        if (iThrowable != null) {
            iThrowable.setId(stack, ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "m67"));
        }
        return stack;
    }

    public static ItemStack getMeleeIcon() {
        ItemStack stack = new ItemStack(MELEE.get());
        IMeleeWeapon iMeleeWeapon = IMeleeWeapon.of(stack);
        if (iMeleeWeapon != null) {
            iMeleeWeapon.setId(stack, ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "karambit"));
        }
        return LrTacticalAPI.getMeleeIndex(stack).map(MeleeWeaponIndex::createItemStack).orElse(stack);
    }

    public static ItemStack getConsumableIcon() {
        ItemStack stack = new ItemStack(CONSUMABLE.get());
        IConsumable iConsumable = IConsumable.of(stack);
        if (iConsumable != null) {
            iConsumable.setId(stack, ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "blood_pack"));
        }
        return LrTacticalAPI.getConsumableIndex(stack).map(ConsumableIndex::createItemStack).orElse(stack);
    }


    public static void fillThrowables(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        for (ThrowableIndex<?, ?> index : LrTacticalAPI.getThrowableIndexes()) {
            ItemStack stack = index.createItemStack();
            pOutput.accept(stack);
        }
        pOutput.accept(new ItemStack(DETONATOR.get()));
    }

    public static void fillMeleeWeapons(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        for (MeleeWeaponIndex<?> index : LrTacticalAPI.getMeleeIndexes()) {
            ItemStack stack = index.createItemStack();
            pOutput.accept(stack);
        }
        pOutput.accept(new ItemStack(FLASH_SHIELD.get()));
    }

    public static void fillConsumables(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        for (ConsumableIndex index : LrTacticalAPI.getConsumableIndexes()) {
            ItemStack stack = index.createItemStack();
            pOutput.accept(stack);
        }
    }
}
