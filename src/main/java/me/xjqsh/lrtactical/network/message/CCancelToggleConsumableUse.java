package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.api.LrTacticalAPI;
import me.xjqsh.lrtactical.api.item.IConsumable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CCancelToggleConsumableUse() implements CustomPacketPayload {
    public static final Type<CCancelToggleConsumableUse> TYPE = new Type<>(ResourceLocation.parse("lrtactical:cancel_toggle_consumable_use"));
    public static final StreamCodec<FriendlyByteBuf, CCancelToggleConsumableUse> STREAM_CODEC = StreamCodec.unit(new CCancelToggleConsumableUse());

    @Override
    public Type<CCancelToggleConsumableUse> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack useItem = player.getUseItem();
                if (useItem.getItem() instanceof IConsumable && LrTacticalAPI.getConsumableIndex(useItem)
                        .map(index -> index.getData().isToggleUse())
                        .orElse(false)) {
                    player.stopUsingItem();
                }
            }
        });
    }
}
