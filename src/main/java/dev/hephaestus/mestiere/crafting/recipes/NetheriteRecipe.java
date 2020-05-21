package dev.hephaestus.mestiere.crafting.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class NetheriteRecipe extends Skill.Recipe {
    private final Item inputItem;
    private final Item outputItem;
    public NetheriteRecipe(Identifier id, Skill skill, Skill.Perk perk, Item inputItem, Item outputItem, int value) {
        super(id, skill, perk, value);
        this.inputItem = inputItem;
        this.outputItem = outputItem;
    }

    public NetheriteRecipe(NetheriteRecipe recipe) {
        super(recipe);
        this.inputItem = recipe.inputItem;
        this.outputItem = recipe.outputItem;
    }

    public NetheriteRecipe(Identifier id, PacketByteBuf buf) {
        super(id, buf);
        this.inputItem = buf.readItemStack().getItem();
        this.outputItem = buf.readItemStack().getItem();
    }

    @Override
    public RecipeType getType() {
        return Skill.Recipe.Type.NETHERITE;
    }

    @Override
    public boolean matches(BasicInventory inventory) {
        return inventory.getStack(1).getItem().equals(inputItem) && inventory.getStack(2).getItem().equals(Items.NETHERITE_INGOT);
    }

    @Override
    public int numberOfInputs() {
        return 2;
    }

    @Override
    public boolean canCraft(PlayerEntity playerEntity) {
        boolean hasSkill = super.canCraft(playerEntity);
        if (!hasSkill) return false;

        boolean hasNetherite = false;
        boolean hasInputItem = false;
        for (ItemStack stack : playerEntity.inventory.main) {
            if (stack.getItem().equals(inputItem))
                hasInputItem = true;
            if (stack.getItem().equals(Items.NETHERITE_INGOT))
                hasNetherite = true;

            if (hasInputItem && hasNetherite) return true;
        }

        return false;
    }

    static final ItemStack netherite = new ItemStack(Items.NETHERITE_INGOT);
    private ItemStack icon = null;
    @Override
    public ItemStack getItem(int i, float deltaTick) {
        if (i == 1)
            return netherite;

        if (i == 0 && icon == null)
            icon = new ItemStack(inputItem, 1);

        return i == 0 ? icon : ItemStack.EMPTY;
    }

    @Override
    public void fillInputSlots(PlayerInventory playerInventory, Inventory blockInventory) {
        if (blockInventory.size() <= 2) return;

        boolean hasInputItem = blockInventory.getStack(1).getItem() == this.inputItem;
        boolean hasNetherite = blockInventory.getStack(2).getItem() == Items.NETHERITE_INGOT;

        for (int i = 0; i < playerInventory.main.size(); ++i) {
            ItemStack stack = playerInventory.getStack(i);
            if (!hasInputItem && stack.getItem() == this.inputItem) {
                ItemStack old = blockInventory.getStack(1);
                blockInventory.setStack(1, stack);
                playerInventory.setStack(i, old);
                hasInputItem = true;
            }

            if (!hasNetherite && stack.getItem() == Items.NETHERITE_INGOT) {
                ItemStack old = blockInventory.getStack(2);
                blockInventory.setStack(2, stack);
                playerInventory.setStack(i, old);
                hasNetherite = true;
            }

            if (hasInputItem && hasNetherite) break;
        }
    }

    @Override
    public ItemStack craft(BasicInventory inv) {
        ItemStack inStack = inv.removeStack(1);
        inv.getStack(2).decrement(1);

        ItemStack outStack = new ItemStack(outputItem);
        CompoundTag tag = inStack.getTag();

        outStack.setTag(tag != null ? tag.copy() : null);

        return outStack;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return new ItemStack(outputItem);
    }

    @Override
    public ItemStack getOutput(BasicInventory inv, PlayerEntity playerEntity) {
        ItemStack inStack = inv.getStack(1);

        ItemStack outStack = new ItemStack(outputItem);
        CompoundTag tag = inStack.getTag();

        outStack.setTag(tag != null ? tag.copy() : null);

        return outStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static final RecipeSerializer<NetheriteRecipe> SERIALIZER = new RecipeSerializer<NetheriteRecipe>() {
        class NetheriteRecipeFormat {
            String inputItem;
            String outputItem;
            int value;
            String perk_required;
        }

        @Override
        public NetheriteRecipe read(Identifier id, JsonObject json) {
            NetheriteRecipeFormat netheriteRecipe = new Gson().fromJson(json, NetheriteRecipeFormat.class);
            Item inputItem = Registry.ITEM.get(new Identifier(netheriteRecipe.inputItem));
            Item outputItem = Registry.ITEM.get(new Identifier(netheriteRecipe.outputItem));

            return new NetheriteRecipe(
                    id,
                    Skill.SMITHING,
                    netheriteRecipe.perk_required == null ? Skill.Perk.NONE : Skill.Perk.get(new Identifier(netheriteRecipe.perk_required)),
                    inputItem,
                    outputItem,
                    netheriteRecipe.value
            );
        }

        @Override
        public NetheriteRecipe read(Identifier id, PacketByteBuf buf) {
            return new NetheriteRecipe(id, buf);
        }

        @Override
        public void write(PacketByteBuf buf, NetheriteRecipe recipe) {
            recipe.write(buf);
        }
    };
}
