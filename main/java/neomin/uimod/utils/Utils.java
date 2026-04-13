package neomin.uimod.utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

public class Utils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void copyToClipboard(String text) {
        clipboard.setContents(new StringSelection(text), null);
    }

    public static Minecraft GetMC() {
        return mc;
    }
}