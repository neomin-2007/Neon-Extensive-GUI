package neomin.uimod.gameplay.ui;

import neomin.uimod.api.data.OptionalData;
import neomin.uimod.api.enums.OptionCategoryType;
import neomin.uimod.api.enums.OptionType;
import neomin.uimod.gameplay.services.OptionsService;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ConfigUI extends GuiScreen {

    private final OptionsService optionsService;

    private OptionCategoryType selectedCategory = OptionCategoryType.GENERAL;

    private int panelX, panelY, panelW, panelH;
    private int sidebarW;
    private final int PADDING = 8;
    private final int ROW_H   = 24;
    private final int FIELD_H = 13;

    private int scrollOffset = 0;
    private static final int SCROLL_SPEED = 3;

    private final Map<OptionType, GuiTextField> stringFields = new EnumMap<>(OptionType.class);

    private GuiTextField intField    = null;
    private OptionType   editingInt  = null;

    private GuiTextField floatField   = null;
    private OptionType   editingFloat = null;

    private GuiTextField charField   = null;
    private OptionType   editingChar = null;

    private OptionType   openDropdown     = null;
    private int          dropdownX, dropdownY, dropdownW;
    private static final int DROPDOWN_ITEM_H = 14;

    public ConfigUI(OptionsService optionsService) {
        this.optionsService = optionsService;
    }

    @Override
    public void initGui() {
        recalcLayout();
        rebuildFields();
    }

    @Override
    public void onGuiClosed() {
        flushAll();
        optionsService.save();
    }

    private void recalcLayout() {
        panelW   = Math.min(420, (int) (width  * 0.78));
        panelH   = Math.min(280, (int) (height * 0.72));
        panelX   = (width  - panelW) / 2;
        panelY   = (height - panelH) / 2;
        sidebarW = Math.max(90, panelW / 4);
    }

    private void rebuildFields() {
        stringFields.clear();
        intField     = null; editingInt   = null;
        floatField   = null; editingFloat = null;
        charField    = null; editingChar  = null;
        openDropdown = null;
        scrollOffset = 0;

        List<OptionType> visible = getOptionsByCategory(selectedCategory);
        int contentX = panelX + sidebarW + PADDING;
        int contentW = panelW - sidebarW - PADDING * 2;

        for (int i = 0; i < visible.size(); i++) {
            OptionType type = visible.get(i);
            if (type.getType() != String.class) continue;

            int fieldY = panelY + PADDING + i * ROW_H + (ROW_H - FIELD_H) / 2;
            int labelW = fontRendererObj.getStringWidth(type.getDisplay()) + PADDING;
            int fieldX = contentX + labelW;
            int fieldW = contentW - labelW;

            GuiTextField f = new GuiTextField(
                    type.ordinal(), fontRendererObj,
                    fieldX, fieldY, fieldW, FIELD_H
            );
            f.setMaxStringLength(256);
            f.setEnableBackgroundDrawing(false);
            String cur = optionsService.get(type);
            f.setText(cur != null ? cur : "");
            stringFields.put(type, f);
        }
    }

    private void flushAll() {
        for (Map.Entry<OptionType, GuiTextField> e : stringFields.entrySet())
            optionsService.set(e.getKey(), e.getValue().getText());
        if (editingInt   != null) commitInt();
        if (editingFloat != null) commitFloat();
        if (editingChar  != null) commitChar();
    }

    private void commitInt() {
        if (intField == null || editingInt == null) return;
        try { optionsService.set(editingInt, Integer.parseInt(intField.getText().trim())); }
        catch (NumberFormatException ignored) {}
        intField = null; editingInt = null;
    }

    private void commitFloat() {
        if (floatField == null || editingFloat == null) return;
        try { optionsService.set(editingFloat, Float.parseFloat(floatField.getText().trim())); }
        catch (NumberFormatException ignored) {}
        floatField = null; editingFloat = null;
    }

    private void commitChar() {
        if (charField == null || editingChar == null) return;
        String text = charField.getText();
        optionsService.set(editingChar, text.isEmpty() ? null : text.charAt(0));
        charField = null; editingChar = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(panelX, panelY, panelX + panelW, panelY + panelH,
                new Color(18, 18, 18, 215).getRGB());

        drawSidebar(mouseX, mouseY);
        drawContent(mouseX, mouseY);
        drawScrollbar();

        if (openDropdown != null) drawDropdown(mouseX, mouseY);

        drawCenteredString(fontRendererObj, "§bUI Mod Config",
                panelX + panelW / 2, panelY - 12, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSidebar(int mouseX, int mouseY) {
        drawRect(panelX, panelY, panelX + sidebarW, panelY + panelH,
                new Color(12, 12, 12, 230).getRGB());
        drawRect(panelX + sidebarW - 1, panelY,
                panelX + sidebarW, panelY + panelH,
                new Color(0, 200, 200, 90).getRGB());

        int itemH   = getSidebarItemHeight();
        int offsetY = panelY + PADDING;

        for (OptionCategoryType cat : OptionCategoryType.values()) {
            boolean selected = cat == selectedCategory;
            boolean hovered  = !selected && isInside(mouseX, mouseY,
                    panelX, offsetY, panelX + sidebarW - 1, offsetY + itemH - 2);

            if (selected) {
                drawRect(panelX, offsetY, panelX + sidebarW - 1, offsetY + itemH - 2,
                        new Color(0, 160, 160, 70).getRGB());
                drawRect(panelX, offsetY, panelX + 3, offsetY + itemH - 2,
                        new Color(0, 220, 220, 220).getRGB());
            } else if (hovered) {
                drawRect(panelX, offsetY, panelX + sidebarW - 1, offsetY + itemH - 2,
                        new Color(255, 255, 255, 18).getRGB());
            }

            int textColor = selected ? 0x00FFFF : (hovered ? 0xCCCCCC : 0x777777);
            int textY     = offsetY + (itemH - 2 - fontRendererObj.FONT_HEIGHT) / 2;
            drawString(fontRendererObj, cat.getDisplay(), panelX + PADDING + 4, textY, textColor);
            offsetY += itemH;
        }
    }

    private void drawContent(int mouseX, int mouseY) {
        int contentX = panelX + sidebarW;
        int contentW = panelW - sidebarW;

        List<OptionType> filtered = getOptionsByCategory(selectedCategory);
        int offsetY = panelY + PADDING - scrollOffset;

        for (OptionType type : filtered) {
            int rowTop = offsetY;
            int rowBot = offsetY + ROW_H;

            if (rowBot > panelY && rowTop < panelY + panelH) {
                int midY = offsetY + (ROW_H - fontRendererObj.FONT_HEIGHT) / 2;
                drawString(fontRendererObj, type.getDisplay(),
                        contentX + PADDING, midY, 0xDDDDDD);

                Object obj = optionsService.get(type);

                if (obj instanceof OptionalData<?>) {
                    OptionalData<?> data = (OptionalData<?>) obj;
                    if (data.size() > 0) {
                        int fw = 90;
                        int fx = contentX + contentW - fw - PADDING;
                        int fy = offsetY + (ROW_H - FIELD_H) / 2;

                        boolean isOpen = type == openDropdown;

                        int border = isOpen
                                ? new Color(0, 200, 200).getRGB()
                                : new Color(70, 70, 70).getRGB();
                        drawRect(fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1, border);
                        drawRect(fx, fy, fx + fw, fy + FIELD_H,
                                new Color(25, 25, 35, 200).getRGB());

                        int index = optionsService.getOptionalIndex(type);
                        if (index < 0 || index >= data.size()) index = 0;

                        String valueStr = String.valueOf(data.get(index));
                        drawString(fontRendererObj, valueStr, fx + 3, fy + 2, 0x00FFFF);

                        String arrow = isOpen ? "§e^" : "§7v";
                        drawString(fontRendererObj, arrow,
                                fx + fw - fontRendererObj.getStringWidth("v") - 3, fy + 2, 0xFFFFFF);
                    }
                    offsetY += ROW_H;
                    continue;
                }

                if (type.getType() == Boolean.class) {
                    boolean on = Boolean.TRUE.equals((Object) optionsService.get(type));
                    int tw = 28, th = 12;
                    int tx = contentX + contentW - tw - PADDING;
                    int ty = offsetY + (ROW_H - th) / 2;

                    drawRect(tx, ty, tx + tw, ty + th,
                            on ? new Color(0, 140, 140, 220).getRGB()
                                    : new Color(45, 45, 45, 200).getRGB());
                    int bx = on ? tx + tw - th + 1 : tx + 1;
                    drawRect(bx, ty + 1, bx + th - 2, ty + th - 1,
                            new Color(220, 220, 220).getRGB());
                    drawString(fontRendererObj, on ? "§aON" : "§cOFF",
                            tx - fontRendererObj.getStringWidth(on ? "ON" : "OFF") - 4,
                            midY, 0xFFFFFF);
                }

                if (type.getType() == Integer.class) {
                    int fw = 52, fx = contentX + contentW - fw - PADDING;
                    int fy = offsetY + (ROW_H - FIELD_H) / 2;
                    boolean editing = type == editingInt;

                    drawRect(fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1,
                            editing ? new Color(0, 200, 200).getRGB() : new Color(70, 70, 70).getRGB());
                    drawRect(fx, fy, fx + fw, fy + FIELD_H, new Color(25, 25, 35, 200).getRGB());

                    if (editing && intField != null) {
                        intField.xPosition = fx + 2; intField.yPosition = fy + 1;
                        intField.width = fw - 4; intField.drawTextBox();
                    } else {
                        Integer cur = optionsService.get(type);
                        drawString(fontRendererObj, cur != null ? cur.toString() : "0",
                                fx + 3, fy + 2, 0xFFFFFF);
                    }
                }

                if (type.getType() == Float.class) {
                    int fw = 52, fx = contentX + contentW - fw - PADDING;
                    int fy = offsetY + (ROW_H - FIELD_H) / 2;
                    boolean editing = type == editingFloat;

                    drawRect(fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1,
                            editing ? new Color(0, 200, 200).getRGB() : new Color(70, 70, 70).getRGB());
                    drawRect(fx, fy, fx + fw, fy + FIELD_H, new Color(25, 25, 35, 200).getRGB());

                    if (editing && floatField != null) {
                        floatField.xPosition = fx + 2; floatField.yPosition = fy + 1;
                        floatField.width = fw - 4; floatField.drawTextBox();
                    } else {
                        Float cur = optionsService.get(type);
                        drawString(fontRendererObj, cur != null ? cur.toString() : "0.0",
                                fx + 3, fy + 2, 0xFFFFFF);
                    }
                }

                if (type.getType() == Character.class) {
                    int fw = 30, fx = contentX + contentW - fw - PADDING;
                    int fy = offsetY + (ROW_H - FIELD_H) / 2;
                    boolean editing = type == editingChar;

                    drawRect(fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1,
                            editing ? new Color(0, 200, 200).getRGB() : new Color(70, 70, 70).getRGB());
                    drawRect(fx, fy, fx + fw, fy + FIELD_H, new Color(25, 25, 35, 200).getRGB());

                    if (editing && charField != null) {
                        charField.xPosition = fx + 2; charField.yPosition = fy + 1;
                        charField.width = fw - 4; charField.drawTextBox();
                    } else {
                        Character cur = optionsService.get(type);
                        drawString(fontRendererObj, cur != null ? String.valueOf(cur) : "_",
                                fx + 3, fy + 2, 0xFFFFFF);
                    }
                }

                if (type.getType() == String.class) {
                    GuiTextField field = stringFields.get(type);
                    int labelW = fontRendererObj.getStringWidth(type.getDisplay()) + PADDING;
                    int fx     = contentX + PADDING + labelW;
                    int fw     = contentW - labelW - PADDING;
                    int fy     = offsetY + (ROW_H - FIELD_H) / 2;

                    boolean focused = field != null && field.isFocused();
                    drawRect(fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1,
                            focused ? new Color(0, 200, 200).getRGB() : new Color(60, 60, 60).getRGB());
                    drawRect(fx, fy, fx + fw, fy + FIELD_H, new Color(20, 20, 20, 200).getRGB());

                    if (field != null) {
                        field.xPosition = fx + 2; field.yPosition = fy + 1;
                        field.width = fw - 4; field.drawTextBox();
                    }
                }
            }

            offsetY += ROW_H;
        }
    }

    private void drawDropdown(int mouseX, int mouseY) {
        OptionalData<?> data = optionsService.get(openDropdown);
        if (data == null || data.size() == 0) return;

        int size       = data.size();
        int totalH     = size * DROPDOWN_ITEM_H + 2;
        int currentIdx = optionsService.getOptionalIndex(openDropdown);

        drawRect(dropdownX + 2, dropdownY + 2,
                dropdownX + dropdownW + 2, dropdownY + totalH + 2,
                new Color(0, 0, 0, 120).getRGB());

        drawRect(dropdownX - 1, dropdownY - 1,
                dropdownX + dropdownW + 1, dropdownY + totalH + 1,
                new Color(0, 180, 180, 200).getRGB());
        drawRect(dropdownX, dropdownY,
                dropdownX + dropdownW, dropdownY + totalH,
                new Color(15, 15, 25, 245).getRGB());

        for (int i = 0; i < size; i++) {
            int itemY   = dropdownY + 1 + i * DROPDOWN_ITEM_H;
            boolean hov = isInside(mouseX, mouseY,
                    dropdownX, itemY, dropdownX + dropdownW, itemY + DROPDOWN_ITEM_H);
            boolean sel = i == currentIdx;

            if (sel) {
                drawRect(dropdownX, itemY, dropdownX + dropdownW, itemY + DROPDOWN_ITEM_H,
                        new Color(0, 100, 100, 160).getRGB());
            } else if (hov) {
                drawRect(dropdownX, itemY, dropdownX + dropdownW, itemY + DROPDOWN_ITEM_H,
                        new Color(255, 255, 255, 20).getRGB());
            }

            String label = String.valueOf(data.get(i));
            int    color = sel ? 0x00FFFF : (hov ? 0xFFFFFF : 0xAAAAAA);
            drawString(fontRendererObj, label, dropdownX + 4,
                    itemY + (DROPDOWN_ITEM_H - fontRendererObj.FONT_HEIGHT) / 2, color);

            if (sel) {
                drawString(fontRendererObj, "§a✔",
                        dropdownX + dropdownW - fontRendererObj.getStringWidth("✔") - 4,
                        itemY + (DROPDOWN_ITEM_H - fontRendererObj.FONT_HEIGHT) / 2, 0x00FF88);
            }
        }
    }

    private void drawScrollbar() {
        List<OptionType> filtered = getOptionsByCategory(selectedCategory);
        int totalH = filtered.size() * ROW_H;
        int viewH  = panelH - PADDING * 2;
        if (totalH <= viewH) return;

        int sbX = panelX + panelW - 4;
        drawRect(sbX, panelY, sbX + 4, panelY + panelH, new Color(30, 30, 30, 180).getRGB());

        float ratio     = (float) viewH / totalH;
        int   thumbH    = Math.max(16, (int) (panelH * ratio));
        int   maxScroll = totalH - viewH;
        int   thumbY    = panelY + (int) ((panelH - thumbH) * ((float) scrollOffset / maxScroll));

        drawRect(sbX + 1, thumbY, sbX + 3, thumbY + thumbH, new Color(0, 180, 180, 180).getRGB());
    }

    @Override
    protected void keyTyped(char ch, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) {
            if (openDropdown != null) { openDropdown = null; return; }
            flushAll(); mc.displayGuiScreen(null); return;
        }

        if (editingChar != null && charField != null) {
            if (Character.isLetterOrDigit(ch) || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE)
                charField.textboxKeyTyped(ch, key);
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) commitChar();
            return;
        }

        if (editingInt != null && intField != null) {
            if (Character.isDigit(ch) || ch == '-' || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE)
                intField.textboxKeyTyped(ch, key);
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) commitInt();
            return;
        }

        if (editingFloat != null && floatField != null) {
            if (Character.isDigit(ch) || ch == '.' || ch == '-'
                    || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE)
                floatField.textboxKeyTyped(ch, key);
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) commitFloat();
            return;
        }

        for (GuiTextField f : stringFields.values())
            if (f.isFocused()) f.textboxKeyTyped(ch, key);

        super.keyTyped(ch, key);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        if (openDropdown != null) {
            handleDropdownClick(mx, my);
            return;
        }

        if (!isInside(mx, my, panelX, panelY, panelX + panelW, panelY + panelH)) {
            commitInt(); commitFloat(); commitChar();
            for (GuiTextField f : stringFields.values()) f.setFocused(false);
            super.mouseClicked(mx, my, btn);
            return;
        }

        handleSidebarClick(mx, my);
        handleContentClick(mx, my);

        for (GuiTextField f : stringFields.values()) f.mouseClicked(mx, my, btn);
        if (intField   != null) intField.mouseClicked(mx, my, btn);
        if (floatField != null) floatField.mouseClicked(mx, my, btn);
        if (charField  != null) charField.mouseClicked(mx, my, btn);

        super.mouseClicked(mx, my, btn);
    }

    /**
     * Processa clique dentro do dropdown aberto.
     * Clique fora fecha sem selecionar nada.
     */
    private void handleDropdownClick(int mx, int my) {
        OptionalData<?> data = optionsService.get(openDropdown);
        if (data == null) { openDropdown = null; return; }

        int size   = data.size();
        int totalH = size * DROPDOWN_ITEM_H + 2;

        if (!isInside(mx, my, dropdownX, dropdownY, dropdownX + dropdownW, dropdownY + totalH)) {
            openDropdown = null;
            return;
        }

        for (int i = 0; i < size; i++) {
            int itemY = dropdownY + 1 + i * DROPDOWN_ITEM_H;
            if (isInside(mx, my, dropdownX, itemY, dropdownX + dropdownW, itemY + DROPDOWN_ITEM_H)) {
                optionsService.setOptionalIndex(openDropdown, i);
                optionsService.save();
                openDropdown = null;
                return;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (openDropdown != null) return;

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        List<OptionType> filtered = getOptionsByCategory(selectedCategory);
        int totalH    = filtered.size() * ROW_H;
        int viewH     = panelH - PADDING * 2;
        int maxScroll = Math.max(0, totalH - viewH);

        scrollOffset -= Integer.signum(wheel) * ROW_H * SCROLL_SPEED;
        scrollOffset  = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private void handleSidebarClick(int mx, int my) {
        int itemH   = getSidebarItemHeight();
        int offsetY = panelY + PADDING;

        for (OptionCategoryType cat : OptionCategoryType.values()) {
            if (isInside(mx, my, panelX, offsetY, panelX + sidebarW - 1, offsetY + itemH - 2)) {
                if (selectedCategory != cat) {
                    flushAll(); selectedCategory = cat; rebuildFields();
                }
                return;
            }
            offsetY += itemH;
        }
    }

    private void handleContentClick(int mx, int my) {
        int contentX = panelX + sidebarW;
        int contentW = panelW - sidebarW;

        if (!isInside(mx, my, contentX, panelY, panelX + panelW, panelY + panelH)) {
            commitInt(); commitFloat(); commitChar();
            return;
        }

        List<OptionType> filtered = getOptionsByCategory(selectedCategory);
        int offsetY = panelY + PADDING - scrollOffset;

        for (OptionType type : filtered) {
            int rowTop = offsetY;
            int rowBot = offsetY + ROW_H;
            boolean visible = rowBot >= panelY && rowTop <= panelY + panelH;

            if (!visible) { offsetY += ROW_H; continue; }

            Object obj = optionsService.get(type);

            if (obj instanceof OptionalData<?>) {
                OptionalData<?> data = (OptionalData<?>) obj;

                if (data.size() > 0) {
                    int fw = 90;
                    int fx = contentX + contentW - fw - PADDING;
                    int fy = offsetY + (ROW_H - FIELD_H) / 2;

                    if (isInside(mx, my, fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1)) {
                        if (openDropdown == type) {
                            openDropdown = null;
                        } else {
                            openDropdown = type;

                            int ddH = data.size() * DROPDOWN_ITEM_H + 2;
                            dropdownW = fw;
                            dropdownX = fx;

                            if (fy + FIELD_H + ddH + 2 <= panelY + panelH) {
                                dropdownY = fy + FIELD_H + 2;
                            } else {
                                dropdownY = fy - ddH - 2;
                            }
                        }
                        return;
                    }
                }

                offsetY += ROW_H;
                continue;
            }

            if (type.getType() == Boolean.class) {
                int tw = 28, th = 12;
                int tx = contentX + contentW - tw - PADDING;
                int ty = offsetY + (ROW_H - th) / 2;

                if (isInside(mx, my, tx, ty, tx + tw, ty + th)) {
                    boolean cur = Boolean.TRUE.equals(optionsService.get(type));
                    optionsService.set(type, !cur);
                    optionsService.save();
                    return;
                }
            }

            if (type.getType() == Integer.class) {
                int fw = 52, fx = contentX + contentW - fw - PADDING;
                int fy = offsetY + (ROW_H - FIELD_H) / 2;

                if (isInside(mx, my, fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1)) {
                    if (editingInt != type) {
                        commitInt();
                        editingInt = type;
                        intField = new GuiTextField(type.ordinal(), fontRendererObj,
                                fx + 2, fy + 1, fw - 4, FIELD_H - 2);
                        intField.setMaxStringLength(12);
                        intField.setEnableBackgroundDrawing(false);
                        intField.setFocused(true);
                        Integer cur = optionsService.get(type);
                        intField.setText(cur != null ? cur.toString() : "0");
                        intField.setCursorPositionEnd();
                    }
                    return;
                } else if (editingInt == type) {
                    commitInt();
                }
            }

            if (type.getType() == Float.class) {
                int fw = 52, fx = contentX + contentW - fw - PADDING;
                int fy = offsetY + (ROW_H - FIELD_H) / 2;

                if (isInside(mx, my, fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1)) {
                    if (editingFloat != type) {
                        commitFloat();
                        editingFloat = type;
                        floatField = new GuiTextField(type.ordinal(), fontRendererObj,
                                fx + 2, fy + 1, fw - 4, FIELD_H);
                        floatField.setMaxStringLength(16);
                        floatField.setEnableBackgroundDrawing(false);
                        floatField.setFocused(true);
                        Float cur = optionsService.get(type);
                        floatField.setText(cur != null ? cur.toString() : "0.0");
                        floatField.setCursorPositionEnd();
                    }
                    return;
                } else if (editingFloat == type) {
                    commitFloat();
                }
            }

            if (type.getType() == Character.class) {
                int fw = 30, fx = contentX + contentW - fw - PADDING;
                int fy = offsetY + (ROW_H - FIELD_H) / 2;

                if (isInside(mx, my, fx - 1, fy - 1, fx + fw + 1, fy + FIELD_H + 1)) {
                    if (editingChar != type) {
                        commitChar();
                        editingChar = type;
                        charField = new GuiTextField(type.ordinal(), fontRendererObj,
                                fx + 2, fy + 1, fw - 4, FIELD_H);
                        charField.setMaxStringLength(1);
                        charField.setEnableBackgroundDrawing(false);
                        charField.setFocused(true);
                        Character cur = optionsService.get(type);
                        charField.setText(cur != null ? String.valueOf(cur) : "");
                    }
                    return;
                } else if (editingChar == type) {
                    commitChar();
                }
            }

            offsetY += ROW_H;
        }
    }

    @Override
    public void updateScreen() {
        for (GuiTextField f : stringFields.values()) f.updateCursorCounter();
        if (intField   != null) intField.updateCursorCounter();
        if (floatField != null) floatField.updateCursorCounter();
        if (charField  != null) charField.updateCursorCounter();
    }

    private List<OptionType> getOptionsByCategory(OptionCategoryType cat) {
        List<OptionType> list = new ArrayList<>();
        for (OptionType t : OptionType.values())
            if (t.getCategoryType() == cat) list.add(t);
        return list;
    }

    private int getSidebarItemHeight() {
        return Math.min(22, Math.max(16, ROW_H));
    }

    private boolean isInside(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
