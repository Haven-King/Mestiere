package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TraderInventory;
import net.minecraft.world.World;

public class SkillRecipe extends TradeOffer implements Recipe<TraderInventory> {
    public static final TraderInventory DUMMY_INVENTORY = new TraderInventory(null);

    private SoundEvent sound;
    private PlayerEntity player;
    public final Skill skill;
    public final int value;
    public final SkillPerk perkRequired;
    public final Identifier id;

    public SkillRecipe(PlayerEntity player, int value, Skill skill, ItemStack firstIngredient, ItemStack secondIngredient, ItemStack outputItem, SkillPerk perkRequired, Identifier id) {
        super(firstIngredient, secondIngredient, outputItem, Integer.MAX_VALUE, 0, 1.0f);
        this.player = player;
        this.skill = skill;
        this.value = value;
        this.perkRequired = perkRequired;
        this.id = id;
    }

    public SkillRecipe(int value, Skill skill, ItemStack firstIngredient, ItemStack secondIngredient, ItemStack outputItem, Identifier id, SkillPerk perkRequired) {
        this(null, value, skill, firstIngredient, secondIngredient, outputItem, perkRequired, id);
    }

    public SkillRecipe(int value, Skill skill, ItemStack firstIngredient, ItemStack secondIngredient, ItemStack outputItem, Identifier id) {
        this(null, value, skill, firstIngredient, secondIngredient, outputItem, SkillPerk.NONE, id);
    }

    public SkillRecipe(PlayerEntity player, SkillRecipe recipe) {
        super(recipe.getOriginalFirstBuyItem(), recipe.getSecondBuyItem(), recipe.getSellItem(), Integer.MAX_VALUE, 0, 1.0f);
        this.skill = recipe.skill;
        this.value = recipe.value;
        this.id = recipe.id;
        this.sound = recipe.sound;
        this.player = player;
        this.perkRequired = recipe.perkRequired;
    }

    public SkillRecipe withSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public void use() {
        assert this.player != null;
        super.use();
        Mestiere.COMPONENT.get(this.player).addXp(this.skill, this.value);

        if (this.sound != null) {
            this.player.world.getServer().getPlayerManager().getPlayerList().forEach((player) ->
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(this.sound, SoundCategory.PLAYERS, this.player.getPos().getX(), this.player.getPos().getY(), this.player.getPos().getZ(), 1.0f, 1.0f))
            );
        }
    }

    @Override
    public boolean matches(TraderInventory inv, World world) {
        return true;
    }

    @Override
    public ItemStack craft(TraderInventory inv) {
        return this.getOutput();
    }

    @Override
    public boolean fits(int width, int height) {
        // These recipes won't be used in a crafting table; this function doesn't matter.
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return this.getSellItem();
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<SkillRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();

        public static final Identifier ID = new Identifier(Mestiere.MOD_ID, "skill_recipe");

        public String toString() {
            return ID.toString();
        }
    }
}
