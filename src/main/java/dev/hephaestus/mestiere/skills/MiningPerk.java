package dev.hephaestus.mestiere.skills;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

public class MiningPerk extends SkillPerk {
    private final Block block;

    public MiningPerk(int level, Block block) {
        super(Mestiere.newID("mining." + Registry.BLOCK.getId(block).getPath()), Skills.MINING, level, new LiteralText("You can now see ")
            .append(new TranslatableText(block.getTranslationKey()).setStyle(new Style().setColor(MestiereConfig.messageFormatting.getOrDefault(Registry.BLOCK.getId(block), Formatting.WHITE)).setBold(true)))
            .append(new LiteralText("!")),
            true, new ItemStack(block.asItem()));

        this.block = block;
    }

    @Override
    public void gained(ServerPlayerEntity player) {
        FibLib.Blocks.update(player.getServerWorld(), block);
    }
}
