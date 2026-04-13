package neomin.uimod.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import neomin.uimod.api.data.OptionalData;

@Getter
@AllArgsConstructor
public enum OptionType {
    INTEGER_OPTION("Opção A", OptionCategoryType.GENERAL, 0, Integer.class),
    BOOLEAN_OPTION("Opção B", OptionCategoryType.GENERAL, false, Boolean.class),
    STRING_OPTION("Opção C", OptionCategoryType.GENERAL, "", String.class),

    HOTBAR_VIEW("Hotbar Adaptiva", OptionCategoryType.GRAPHICAL, false, Boolean.class),
    SCOREBOARD_VIEW("Scoreboard Ativo", OptionCategoryType.GRAPHICAL, true, Boolean.class),
    CHAT_VIEW("Mensagens do Chat", OptionCategoryType.GRAPHICAL, true, Boolean.class),
    GRAPHIC_QUALITY("Qualidade Gráfica", OptionCategoryType.GRAPHICAL,
            new OptionalData<>(String.class, "Baixa", "Média", "Alta"), null),

    SLOT_LOCK("Bloquear Slots", OptionCategoryType.KEYBIND, 'B', Character.class),;

    private final String display;
    private final OptionCategoryType categoryType;
    private final Object defaultValue;
    private final Object type;
}
