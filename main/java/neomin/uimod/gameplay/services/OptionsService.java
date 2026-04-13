package neomin.uimod.gameplay.services;

import neomin.uimod.api.data.Option;
import neomin.uimod.api.data.OptionalData;
import neomin.uimod.api.enums.OptionType;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class OptionsService {

    private Configuration config;
    private final Map<OptionType, Option<?>> options = new EnumMap<>(OptionType.class);
    private final Map<OptionType, Integer> optionalIndexes = new EnumMap<>(OptionType.class);

    public void init(File file) {
        config = new Configuration(file);
        load();
    }

    public void load() {
        config.load();

        for (OptionType type : OptionType.values()) {

            String category = type.getCategoryType().name().toLowerCase();
            String key = type.name().toLowerCase();

            if (type.getType() == Boolean.class) {
                boolean value = config.getBoolean(key, category, (Boolean) type.getDefaultValue(), "");
                options.put(type, new Option<Boolean>(type, value));
            }

            if (type.getType() == Integer.class) {
                int value = config.getInt(key, category, (Integer) type.getDefaultValue(),
                        Integer.MIN_VALUE, Integer.MAX_VALUE, "");
                options.put(type, new Option<Integer>(type, value));
            }

            if (type.getType() == Float.class) {
                float value = config.getFloat(key, category, (Float) type.getDefaultValue(),
                        Float.MIN_VALUE, Float.MAX_VALUE, "");
                options.put(type, new Option<Float>(type, value));
            }

            if (type.getType() == String.class) {
                String value = config.getString(key, category, (String) type.getDefaultValue(), "");
                options.put(type, new Option<String>(type, value));
            }

            if (type.getType() == Character.class) {
                String value = config.getString(
                        key,
                        category,
                        String.valueOf(type.getDefaultValue()),
                        ""
                );

                char c = value.isEmpty() ? '\0' : value.charAt(0);
                options.put(type, new Option<Character>(type, c));
            }

            if (type.getDefaultValue() instanceof OptionalData) {
                OptionalData<?> def = (OptionalData<?>) type.getDefaultValue();
                options.put(type, new Option<OptionalData<?>>(type, def));

                // Lê o índice salvo separadamente
                int savedIndex = config.getInt(
                        key + "_index", category, 0, 0, def.size() - 1, ""
                );
                optionalIndexes.put(type, savedIndex);
            }
        }

        if (config.hasChanged()) config.save();
    }

    public void save() {
        for (Map.Entry<OptionType, Option<?>> entry : options.entrySet()) {

            OptionType type = entry.getKey();
            Option<?> option = entry.getValue();

            String category = type.getCategoryType().name().toLowerCase();
            String key = type.name().toLowerCase();

            if (type.getType() == Boolean.class) {
                config.get(category, key, true).set((Boolean) option.get());
            }

            if (type.getType() == Integer.class) {
                config.get(category, key, 0).set((Integer) option.get());
            }

            if (type.getType() == Float.class) {
                config.get(category, key, 0f).set((Float) option.get());
            }

            if (type.getType() == String.class) {
                config.get(category, key, "").set((String) option.get());
            }

            if (type.getType() == Character.class) {
                config.get(category, key, "").set(String.valueOf(option.get()));
            }

            if (option.get() instanceof OptionalData) {
                // Salva o índice selecionado, não o OptionalData em si
                int idx = optionalIndexes.getOrDefault(type, 0);
                config.get(category, key + "_index", 0).set(idx);
            }
        }

        config.save();
    }

    public int getOptionalIndex(OptionType type) {
        return optionalIndexes.getOrDefault(type, 0);
    }

    public void setOptionalIndex(OptionType type, int index) {
        optionalIndexes.put(type, index);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(OptionType type) {
        Option<?> option = options.get(type);
        return option != null ? (T) option.get() : null;
    }

    @SuppressWarnings("unchecked")
    public <T> void set(OptionType type, T value) {
        Option<?> option = options.get(type);
        if (option != null) {
            ((Option<T>) option).set(value);
        }
    }
}