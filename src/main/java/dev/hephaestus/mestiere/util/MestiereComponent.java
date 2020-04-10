package dev.hephaestus.mestiere.util;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

interface XpComponent extends Component {
    int getLevel(Skill skill);
    int getXp(Skill skill);
    void setXp(Skill skill, int xp);
    void addXp(Skill skill, int xp);
}

public class MestiereComponent implements XpComponent {
    private final ServerPlayerEntity player;

    private HashMap<Skill, Integer> skills = new HashMap<>();

    public MestiereComponent(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        CompoundTag skillTag = tag.getCompound(Mestiere.MOD_ID);

        for (Skill s : Mestiere.SKILLS) {
            this.skills.put(s, skillTag.getInt(s.id.toString()));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        CompoundTag skillTag = new CompoundTag();

        for (Skill s : Mestiere.SKILLS) {
            skillTag.putInt(s.id.toString(), skills.getOrDefault(s, 0));
        }

        tag.put(Mestiere.MOD_ID, skillTag);

        return tag;
    }

    @Override
    public int getLevel(Skill skill) {
        return (int) MathHelper.sqrt((float)getXp(skill)/2 + 1);
    }

    @Override
    public int getXp(Skill skill) {
        return this.skills.getOrDefault(skill, 0);
    }

    @Override
    public void setXp(Skill skill, int xp) {
        this.skills.put(skill, xp);
    }

    @Override
    public void addXp(Skill skill, int xp) {
        this.player.addExperience(xp);

        int level = getLevel(skill);
        this.skills.put(skill, this.skills.getOrDefault(skill, 0) + xp);
        int newLevel = getLevel(skill);
        if (newLevel > level) {
            LiteralText sText = new LiteralText(skill.name);
            sText.setStyle(new Style().setColor(skill.format).setBold(true));

            LiteralText lText = new LiteralText(Integer.toString(newLevel));
            lText.setStyle(new Style().setColor(Formatting.GREEN));

            player.sendChatMessage(
                new LiteralText("Congratulations! You have reached level ")
                        .append(lText)
                        .append(" in ")
                        .append(sText).append("!"),
                MessageType.CHAT);

            for (Map.Entry<Block, Integer> e: Mestiere.CONFIG.levelRequireToDetect.entrySet()) {
                if (e.getValue() <= newLevel && e.getValue() > level)
                    FibLib.update(this.player.getServerWorld(), Mestiere.CONFIG.levelRequireToDetect.keySet());
            }
        }
        Mestiere.debug(String.format("%s has %dXP in %s. They are level %d", this.player.getName().asString(), this.skills.getOrDefault(skill, 0), skill.name, getLevel(skill)));
    }
}