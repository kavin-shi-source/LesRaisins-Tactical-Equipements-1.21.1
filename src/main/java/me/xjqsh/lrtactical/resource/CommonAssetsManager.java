package me.xjqsh.lrtactical.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import java.lang.reflect.Type;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.api.collision.ITargetFilter;
import me.xjqsh.lrtactical.item.consumable.ConsumableData;
import me.xjqsh.lrtactical.item.index.ConsumableIndex;
import me.xjqsh.lrtactical.item.index.MeleeWeaponIndex;
import me.xjqsh.lrtactical.item.index.ThrowableIndex;
import me.xjqsh.lrtactical.item.melee.AttributeData;
import me.xjqsh.lrtactical.item.melee.CombatData;
import me.xjqsh.lrtactical.item.throwable.area.EffectCloudThrowableData;
import me.xjqsh.lrtactical.network.DataType;
import me.xjqsh.lrtactical.network.NetworkHandler;
import me.xjqsh.lrtactical.network.message.SPackSyncMessage;
import me.xjqsh.lrtactical.resource.manager.ConsumableIndexManager;
import me.xjqsh.lrtactical.resource.manager.MeleeIndexManager;
import me.xjqsh.lrtactical.resource.manager.ThrowableIndexManager;
import me.xjqsh.lrtactical.resource.serializer.ParticleOptionsDeserializer;
import me.xjqsh.lrtactical.resource.serializer.ResourceLocationSerializer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = EquipmentMod.MOD_ID)
public class CommonAssetsManager implements ICommonResourceProvider {

    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationSerializer())
            .registerTypeAdapter(CombatData.class, new CombatData.Deserializer())
            .registerTypeAdapter(ITargetFilter.class, new ITargetFilter.Deserializer())
            .registerTypeAdapter(AttributeData.class, new AttributeData.Deserializer())
            .registerTypeAdapter(ParticleOptions.class, new ParticleOptionsDeserializer())
            .registerTypeAdapter(EffectCloudThrowableData.EffectData.class, new EffectCloudThrowableData.EffectDataDeSerializer())
            .registerTypeAdapter(ConsumableData.RemoveEffectSelector.class, new ConsumableData.RemoveEffectSelector.Deserializer())
            .create();

    public static CommonAssetsManager INSTANCE;

    private CommonAssetsManager() {
    }

    public static ICommonResourceProvider get() {
        return INSTANCE == null ? CommonNetworkCache.INSTANCE : INSTANCE;
    }

    public ThrowableIndexManager throwableIndexManager;
    public MeleeIndexManager meleeIndexManager;
    public ConsumableIndexManager consumableIndexManager;

    private void reloadAndRegister(Consumer<PreparableReloadListener> register) {
        throwableIndexManager = new ThrowableIndexManager(GSON);
        meleeIndexManager = new MeleeIndexManager(GSON);
        consumableIndexManager = new ConsumableIndexManager(GSON);
        register.accept(throwableIndexManager);
        register.accept(meleeIndexManager);
        register.accept(consumableIndexManager);
    }

    public Map<DataType, Map<ResourceLocation, String>> toNetwork() {
        ImmutableMap.Builder<DataType, Map<ResourceLocation, String>> builder = ImmutableMap.builder();
        builder.put(DataType.THROWABLE_INDEX, throwableIndexManager.getCache());
        builder.put(DataType.MELEE_INDEX, meleeIndexManager.getCache());
        builder.put(DataType.CONSUMABLE_INDEX, consumableIndexManager.getCache());
        return builder.build();
    }

    @Override
    public ThrowableIndex<?, ?> getThrowableIndex(ResourceLocation id) {
        return throwableIndexManager.getData(id);
    }

    @Override
    public Collection<ThrowableIndex<?, ?>> getThrowableIndexes() {
        return throwableIndexManager.getAllData().values();
    }

    @Override
    public MeleeWeaponIndex<?> getMeleeIndex(ResourceLocation id) {
        return meleeIndexManager.getData(id);
    }

    @Override
    public Collection<MeleeWeaponIndex<?>> getMeleeIndexes() {
        return meleeIndexManager.getAllData().values();
    }

    @Override
    public ConsumableIndex getConsumableIndex(ResourceLocation id) {
        return consumableIndexManager.getData(id);
    }

    @Override
    public Collection<ConsumableIndex> getConsumableIndexes() {
        return consumableIndexManager.getAllData().values();
    }

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        var commonAssetsManager = new CommonAssetsManager();
        commonAssetsManager.reloadAndRegister(event::addListener);
        INSTANCE = commonAssetsManager;
    }

    @SubscribeEvent
    public static void OnDatapackSync(OnDatapackSyncEvent event) {
        if (CommonAssetsManager.INSTANCE == null) {
            return;
        }
        var msg = new SPackSyncMessage(CommonAssetsManager.INSTANCE.toNetwork());
        if (event.getPlayer() != null) {
            NetworkHandler.sendToClientPlayer(msg, event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> NetworkHandler.sendToClientPlayer(msg, player));
        }
    }
}
