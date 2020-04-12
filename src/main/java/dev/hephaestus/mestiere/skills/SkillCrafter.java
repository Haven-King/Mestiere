package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.village.SimpleTrader;

public class SkillCrafter extends SimpleTrader {
    PlayerEntity player;
    SoundEvent craftSound;
    Skill skill;
    Block block;

    public SkillCrafter(PlayerEntity player, Skill skill, Block block, SoundEvent craftSound) {
        super(player);
        this.craftSound = craftSound;
        this.player = player;
        this.skill = skill;
        this.block = block;
    }

    public void addRecipe(SkillRecipe recipe) {
        assert recipe != null;
        ItemStack item1 = recipe.getAdjustedFirstBuyItem();
        ItemStack item2 = recipe.getSecondBuyItem();
        Mestiere.debug("%d %d", Mestiere.COMPONENT.get(player).getLevel(this.skill), recipe.perkRequired.level);
        if (this.player.inventory.countInInv(item1.getItem()) >= item1.getCount()
            && player.inventory.countInInv(item2.getItem()) >= item2.getCount()
            && (Mestiere.COMPONENT.get(player).getLevel(this.skill) >= recipe.perkRequired.level ||
                (!Mestiere.CONFIG.hardcoreProgression && recipe.perkRequired.hardcore)
               ) ){

            this.getOffers().add(new SkillRecipe(this.player, recipe.withSound(this.craftSound)));
        }
    }

    public void show() {
        this.sendOffers(this.player, new TranslatableText(block.getTranslationKey()), 0);
    }

    public boolean isLevelledTrader() {
        return false;
    }
}