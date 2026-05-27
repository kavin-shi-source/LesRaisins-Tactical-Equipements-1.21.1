package me.xjqsh.lrtactical.client.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xjqsh.lrtactical.EquipmentMod;
import me.xjqsh.lrtactical.client.resource.display.ConsumableDisplayInstance;
import me.xjqsh.lrtactical.client.resource.display.MeleeDisplayInstance;
import me.xjqsh.lrtactical.client.resource.display.ThrowableDisplayInstance;
import me.xjqsh.lrtactical.client.resource.manager.ConsumableDisplayManager;
import me.xjqsh.lrtactical.client.resource.manager.MeleeDisplayManager;
import me.xjqsh.lrtactical.client.resource.manager.ThrowableDisplayManager;
import me.xjqsh.lrtactical.init.ModItems;
import me.xjqsh.lrtactical.resource.serializer.ResourceLocationSerializer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = EquipmentMod.MOD_ID)
public enum LrClientAssetsManager {
    INSTANCE;
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationSerializer())
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
            .create();

    private ThrowableDisplayManager throwableDisplay;
    private MeleeDisplayManager meleeDisplay;
    private ConsumableDisplayManager consumableDisplay;

    public void reloadAndRegister(Consumer<PreparableReloadListener> register) {
        throwableDisplay = new ThrowableDisplayManager(GSON);
        meleeDisplay = new MeleeDisplayManager(GSON);
        consumableDisplay = new ConsumableDisplayManager(GSON);

        register.accept(throwableDisplay);
        register.accept(meleeDisplay);
        register.accept(consumableDisplay);
    }

    public ThrowableDisplayInstance getThrowableDisplay(ResourceLocation id) {
        return throwableDisplay.getData(id);
    }

    public MeleeDisplayInstance getMeleeDisplay(ResourceLocation id) {
        return meleeDisplay.getData(id);
    }

    public ConsumableDisplayInstance getConsumableDisplay(ResourceLocation id) {
        return consumableDisplay.getData(id);
    }

    // 要排在tacz后，因为我们要用到tacz的资源
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClientResourceReload(RegisterClientReloadListenersEvent event) {
        LrClientAssetsManager.INSTANCE.reloadAndRegister(event::registerReloadListener);
        event.registerReloadListener(IClientItemExtensions.of(ModItems.FLASH_SHIELD.get()).getCustomRenderer());
    }
}
