package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public abstract class SkillRecipe implements Recipe<BasicInventory>, Comparable {
    private final Identifier id;
    private final Skill skill;
    private final Skill.Perk perk;
    private final int value;

    private PlayerEntity player;


    public SkillRecipe(Identifier id, Skill skill, Skill.Perk perk, int value) {
        this.id = id;
        this.skill = skill;
        this.perk = perk;
        this.value = value;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public SkillRecipe(SkillRecipe recipe) {
        this.id = recipe.id;
        this.skill = recipe.skill;
        this.perk = recipe.perk;
        this.value = recipe.value;
    }

    public SkillRecipe(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.skill = Mestiere.SKILLS.get(buf.readIdentifier());
        this.perk = Skill.Perk.get(buf.readIdentifier());
        this.value = buf.readInt();
    }

    @Override
    public boolean matches(BasicInventory inv, World world) {
        return matches(inv);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    public int compareTo(Object o) {
        int result = 0;

        if (o instanceof SkillRecipe) {
            result = -Boolean.compare(canCraft(getPlayer()), ((SkillRecipe) o).canCraft(getPlayer()));
            result = result == 0 ? Integer.compare(getValue(), ((SkillRecipe)o).getValue()) : result;
            result = result == 0 ? getId().compareTo(((SkillRecipe) o).getId()) : result;
        }
        return result;
    }

    public abstract boolean matches(BasicInventory inventory);

    public abstract int numberOfInputs();
    public abstract ItemStack getOutput(BasicInventory inv);

    public int getValue() {return this.value;}
    public Skill.Perk getPerk() {return this.perk;}
    public Skill getSkill() {return this.skill;}

    public SkillRecipe withPlayer(PlayerEntity player) {
        this.player = player;
        return this;
    }

    public PlayerEntity getPlayer() {return this.player;}

    public boolean canCraft(PlayerEntity playerEntity) {
        return Mestiere.COMPONENT.get(player).hasPerk(getPerk()) || !Mestiere.CONFIG.hardcoreProgression || !getPerk().isHardcore();
    }

    void write(PacketByteBuf buf) {
        buf.writeIdentifier(Registry.RECIPE_TYPE.getId(getType()));
        buf.writeIdentifier(getSkill().id);
        buf.writeIdentifier(getPerk().id);
        buf.writeItemStack(getOutput());
        buf.writeInt(getValue());
    }

    @Environment(EnvType.CLIENT)
    public abstract ItemStack getItem(int i, float deltaTick);

    public abstract void fillInputSlots(PlayerInventory playerInventory, Inventory blockInventory);

    public abstract static class Component {
        abstract IntList getRawIds();
        abstract int count();
        abstract boolean matches(ItemStack stack);
        abstract void write(PacketByteBuf buf);
    }
}
