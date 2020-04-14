package dev.hephaestus.mestiere.skills;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import static net.minecraft.util.Util.createTranslationKey;

public class MiningPerk extends SkillPerk {
    private final Block block;
    private final Text stext;

    public MiningPerk(int level, Block block) {
        super(Mestiere.newID("mining.ore." + Registry.BLOCK.getId(block).getPath()),
                Skills.MINING,
                level,
                true,
                new ItemStack(block.asItem()));

        this.block = block;
        stext = new TranslatableText(block.getTranslationKey()).setStyle(new Style().setColor(MestiereConfig.messageFormatting.getOrDefault(Registry.BLOCK.getId(block), Formatting.WHITE)));
    }

    @Override
    public void gained(ServerPlayerEntity player) {
        FibLib.Blocks.update(player.getServerWorld(), block);
    }

    @Override
    public String getOrCreateTranslationKey(Mestiere.KEY_TYPE type) {
        if (messages.get(type) == null) {
            if (type == Mestiere.KEY_TYPE.NAME)
                messages.put(type, createTranslationKey("perk", Mestiere.newID("mining." + Registry.BLOCK.getId(block).getPath() + "." + type.toString().toLowerCase())));
            else
                messages.put(type, createTranslationKey("perk", Mestiere.newID("mining.ore." + type.toString().toLowerCase())));
        }

        return messages.get(type);
    }

    @Override
    public TranslatableText getText(Mestiere.KEY_TYPE type) {
        return new TranslatableText(getOrCreateTranslationKey(type), stext);
    }
}
