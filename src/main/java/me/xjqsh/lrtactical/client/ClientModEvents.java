package me.xjqsh.lrtactical.client;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.renderer.item.ConsumableItemRenderer;
import me.xjqsh.lrtactical.client.renderer.item.FlashShieldItemRenderer;
import me.xjqsh.lrtactical.client.renderer.item.MeleeItemRenderer;
import me.xjqsh.lrtactical.client.renderer.item.ThrowableItemRendererWrapper;
import me.xjqsh.lrtactical.init.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = EquipmentMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private ThrowableItemRendererWrapper renderer = null;

            @Override
            public ThrowableItemRendererWrapper getCustomRenderer() {
                if (this.renderer == null) {
                    renderer = new ThrowableItemRendererWrapper();
                }
                return renderer;
            }
        }, ModItems.THROWABLE.get());

        event.registerItem(new IClientItemExtensions() {
            private MeleeItemRenderer renderer = null;

            @Override
            public MeleeItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    renderer = new MeleeItemRenderer();
                }
                return renderer;
            }
        }, ModItems.MELEE.get());

        event.registerItem(new IClientItemExtensions() {
            private final FlashShieldItemRenderer renderer = new FlashShieldItemRenderer();

            @Override
            public FlashShieldItemRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (hand == InteractionHand.OFF_HAND) {
                    return HumanoidModel.ArmPose.EMPTY;
                }
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        }, ModItems.FLASH_SHIELD.get());

        event.registerItem(new IClientItemExtensions() {
            private ConsumableItemRenderer renderer = null;

            @Override
            public ConsumableItemRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ConsumableItemRenderer();
                }
                return renderer;
            }
        }, ModItems.CONSUMABLE.get());
    }
}
