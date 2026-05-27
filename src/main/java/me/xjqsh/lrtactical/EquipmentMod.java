package me.xjqsh.lrtactical;

import me.xjqsh.lrtactical.config.ClientConfig;
import me.xjqsh.lrtactical.config.CommonConfig;
import me.xjqsh.lrtactical.config.ServerConfig;
import net.neoforged.fml.config.ModConfig;
import me.xjqsh.lrtactical.init.*;
import me.xjqsh.lrtactical.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EquipmentMod.MOD_ID)
public class EquipmentMod {
    public static final String MOD_ID = "lrtactical";
    public static final Logger LOGGER = LogManager.getLogger();

    public EquipmentMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ModCapabilities.ATTACHMENT_TYPES.register(modEventBus);
        ModCustomTypes.THROWABLE_TYPES.register(modEventBus);
        ModCustomTypes.MELEE_WEAPON_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.init());
    }

}
