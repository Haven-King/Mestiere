package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class Skill {
    public final Identifier id;
    public final String name;
    public final Formatting format;
    public final SoundEvent sound;

    public Skill(Identifier id, Formatting format, SoundEvent sound) {
        this.id = id;
        this.name = id.getPath().substring(0, 1).toUpperCase() + id.getPath().substring(1);
        this.format = format;
        this.sound = sound;
    }

    public Skill(Identifier id, Formatting format) {
        this(id, format, null);
    }
}
