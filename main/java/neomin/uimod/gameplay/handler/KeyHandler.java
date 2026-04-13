package neomin.uimod.gameplay.handler;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    public static KeyBinding OPEN_GUI;

    public static void init() {
        OPEN_GUI = new KeyBinding("Abrir Menu de Configurações", Keyboard.KEY_RSHIFT, "NeonExtensiveGUI");
        ClientRegistry.registerKeyBinding(OPEN_GUI);
    }
}
