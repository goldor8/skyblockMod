package fr.modcraftmc.skyblock;

import fr.modcraftmc.skyblock.commands.Command;
import fr.modcraftmc.skyblock.database.Connector;
import fr.modcraftmc.skyblock.listeners.BlockListeners;
import fr.modcraftmc.skyblock.listeners.PlayerListener;
import fr.modcraftmc.skyblock.network.demands.GuiCommand;
import fr.modcraftmc.skyblock.network.PacketHandler;
import fr.modcraftmc.skyblock.network.PacketOpenGUI;
import fr.modcraftmc.skyblock.network.demands.Request;
import fr.modcraftmc.skyblock.util.config.ConfigHandler;
import fr.modcraftmc.skyblock.world.SkyblockChunkGenerator;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;
import java.io.File;

import static fr.modcraftmc.skyblock.util.Constants.DISPLAY_NAME;

@Mod(SkyBlock.MOD_ID)
@Mod.EventBusSubscriber(modid = SkyBlock.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkyBlock
{
    public static SkyBlock instance;

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "modcraftsb";
    public static final File CONFIG_DIR = new File(FMLPaths.CONFIGDIR.get().toFile().toString()+"/"+MOD_ID);
//    public static final File GAME_DIR = new File(FMLPaths.GAMEDIR.get().toFile().toString());
    public static final ResourceLocation SKYBLOCK_DIM_TYPE = new ResourceLocation(MOD_ID, "skyblock_dim");
    public static Connector config;

    public static final ResourceLocation DIM_ID = new ResourceLocation(MOD_ID, "skyblockdim");
    public static final RegistryKey<DimensionType> DIMENSION_TYPE_KEY = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY, DIM_ID);

    public static MinecraftServer DEDICATED_SERVER;

    private KeyBinding skyblock;

    public SkyBlock() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
//        modEventBus.addListener(this::onServerStarting);
//        modEventBus.addListener(this::changedDimension);
//        modEventBus.addListener(this::onPlayerLogin);
//        modEventBus.addListener(this::registerDimensions);
//        modEventBus.addListener(this::onKeyInput);
//        modEventBus.addListener(this::onRespawnPlayer);

//        DimensionInit.MOD_DIMENSION.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new BlockListeners());
        MinecraftForge.EVENT_BUS.register(new PlayerListener());
//        if (FMLEnvironment.dist == Dist.CLIENT)
//            MinecraftForge.EVENT_BUS.register(new ClientListener());

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.server_config);
        ConfigHandler.loadConfig(ConfigHandler.server_config, FMLPaths.CONFIGDIR.get().resolve("modcraftsb-server.toml").toString());
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "chunk_generator"), SkyblockChunkGenerator.CODEC);

        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER){
            config = new Connector();
        }

        instance = this;
    }

//    @SubscribeEvent
//    public void onBlockBreak(BlockEvent.BreakEvent event){
//        RegistryKey<World> dim = event.getPlayer().getCommandSenderWorld().getDimensionKey();
//        LOGGER.info("dim = " + dim);
//        event.getPlayer().sendMessage(new StringTextComponent("tu as cassé ce block: "+event.getState().getBlock().getTranslationKey()), event.getPlayer().getUniqueID());
//    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event){
        if (skyblock.consumeClick()){
            PacketHandler.INSTANCE.sendToServer(new PacketOpenGUI(Request.MAIN, null, GuiCommand.EMPTY));
        }
    }

//    @SubscribeEvent
//    public void changedDimension(PlayerEvent.PlayerChangedDimensionEvent e){
//        e.getPlayer().sendMessage(new StringTextComponent("Tu as atteris dans une dimension: "+e.getEntity().getCommandSenderWorld().getDimensionKey()), e.getPlayer().getUniqueID());
//    }

    private void clientSetup(final FMLClientSetupEvent event){
        skyblock = new KeyBinding("key.modcraftsb.openmenu", KeyEvent.VK_G, "key.modcraftsb.category");
        ClientRegistry.registerKeyBinding(skyblock);
    }

    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.registerMessages();
    }

    //@SubscribeEvent
    public void registerDimensions(final RegistryEvent event) {
//        if (Dimension.func_236062_a_(SkyBlock.SKYBLOCK_DIM_TYPE))
//        if (DimensionType.byName(SkyBlock.SKYBLOCK_DIM_TYPE) == null)
//            DimensionManager.registerDimension(SkyBlock.SKYBLOCK_DIM_TYPE, DimensionInit.SKYBLOCK_DIM.get(), null, true);
//        SkyBlock.LOGGER.info("Dimensions Registered!");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        DEDICATED_SERVER = event.getServer();
        Command.register(event.getServer().getFunctions().getDispatcher());
    }
}
