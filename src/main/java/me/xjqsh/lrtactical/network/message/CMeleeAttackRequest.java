package me.xjqsh.lrtactical.network.message;

import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.melee.MeleeAction;
import me.xjqsh.lrtactical.capability.CombatProperties;
import me.xjqsh.lrtactical.init.ModCapabilities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record CMeleeAttackRequest(
        MeleeAction action,
        int actionCount,
        List<Integer> entityIds
) implements CustomPacketPayload {

    public static final Type<CMeleeAttackRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EquipmentMod.MOD_ID, "melee_attack_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CMeleeAttackRequest> STREAM_CODEC = StreamCodec.of(
            CMeleeAttackRequest::encode,
            CMeleeAttackRequest::decode
    );

    public CMeleeAttackRequest(MeleeAction action, List<Entity> entities) {
        this(action, 0, entities.stream().map(Entity::getId).toList());
    }

    public static void encode(RegistryFriendlyByteBuf buf, CMeleeAttackRequest message) {
        buf.writeEnum(message.action);
        buf.writeVarInt(message.actionCount);
        buf.writeVarInt(message.entityIds.size());
        for (int id : message.entityIds) {
            buf.writeVarInt(id);
        }
    }

    public static CMeleeAttackRequest decode(RegistryFriendlyByteBuf buf) {
        MeleeAction action = buf.readEnum(MeleeAction.class);
        int actionCount = buf.readVarInt();
        int size = buf.readVarInt();
        List<Integer> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readVarInt());
        }
        return new CMeleeAttackRequest(action, actionCount, ids);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CMeleeAttackRequest message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Level level = player.level();
            List<Entity> entities = new ArrayList<>();
            for (int id : message.entityIds) {
                Entity entity = level.getEntity(id);
                if (entity instanceof LivingEntity) {
                    entities.add(entity);
                }
            }
            CombatProperties cap = player.getData(ModCapabilities.COMBAT_PROPERTIES);
            cap.setActionCount(message.action, message.actionCount);
            cap.postAttack(message.action, entities);
        });
    }
}
