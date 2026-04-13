package neomin.uimod;

import neomin.uimod.api.enums.OptionType;
import neomin.uimod.gameplay.handler.KeyHandler;
import neomin.uimod.gameplay.services.OptionsService;
import neomin.uimod.gameplay.ui.ConfigUI;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main
{
    public static final String MODID = "uimod";
    public static final String VERSION = "1.0";

    private OptionsService optionsService;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        optionsService = new OptionsService();

        File configFile = new File(event.getModConfigurationDirectory(), "uimod.cfg");
        optionsService.init(configFile);

        KeyHandler.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (KeyHandler.OPEN_GUI.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new ConfigUI(optionsService));
        }
    }
}
