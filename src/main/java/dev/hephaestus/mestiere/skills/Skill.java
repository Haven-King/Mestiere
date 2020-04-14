package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;

import static net.minecraft.util.Util.createTranslationKey;

public class Skill {
    public final Identifier id;
    public final Formatting format;
    public final SoundEvent sound;
    public final ItemStack icon;

    public Skill(Identifier id, Formatting format, SoundEvent sound, ItemStack icon) {
        this.id = id;
        this.format = format;
        this.sound = sound;
        this.icon = icon;
    }

    public Skill(Identifier id, Formatting format, ItemStack icon) {
        this(id, format, null, icon);
    }

    protected HashMap<Mestiere.KEY_TYPE, String> messages = new HashMap<>();

    public String getOrCreateTranslationKey(Mestiere.KEY_TYPE type) {
        if (messages.get(type) == null) {
            messages.put(type, createTranslationKey("skill", Mestiere.newID(this.id.getPath() + "." + type.toString().toLowerCase())));
        }

        return messages.get(type);
    }

    public TranslatableText getText(Mestiere.KEY_TYPE type) {
        return (TranslatableText) new TranslatableText(getOrCreateTranslationKey(type)).formatted(format);
    }
}
