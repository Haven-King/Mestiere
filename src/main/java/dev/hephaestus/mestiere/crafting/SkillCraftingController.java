package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.ScrollingGui;
import dev.hephaestus.mestiere.client.gui.widgets.BetterListPanel;
import dev.hephaestus.mestiere.client.gui.widgets.RecipeButton;
import dev.hephaestus.mestiere.mixin.RecipeManagerMixin;
import dev.hephaestus.mestiere.skills.Skill;
import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.container.BlockContext;
import net.minecraft.container.Slot;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SkillCraftingController extends CottonCraftingController implements ScrollingGui {
    private static final WSprite ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 179f / 512f, 38f / 256f, 215f / 512f, 56f / 256f);
    private BetterListPanel<SkillRecipe, RecipeButton> listPanel;
    private final BlockContext context;


    private final List<SkillRecipe> recipes;

    private static final HashMap<Integer, SkillCraftingController> INSTANCES = new HashMap<>();

    private SkillRecipe recipe;

    public static SkillCraftingController getInstance(int syncId) {
        return INSTANCES.get(syncId);
    }

    public SkillCraftingController(int syncId, Skill skill, PlayerInventory playerInventory, BlockContext context) {
        super(SkillRecipe.Type.INSTANCE, syncId, playerInventory, new BasicInventory(3), null);
        Mestiere.debug("Player (init): %s", this.playerInventory.player.toString());
        INSTANCES.put(syncId, this);
        this.context = context;

//        this.addSlot(new CraftingResultSlot(playerInventory.player, (CraftingInventory) this.blockInventory, this.result, 0, -1000000, -1000000));
//        this.addSlot(new Slot(blockInventory, 0, -1000000, -1000000));
//        this.addSlot(new Slot(blockInventory, 1, -1000000, -1000000));


        WGridPanel root = new WGridPanel(9);
        setRootPanel(root);

        List<SkillRecipe> recipeList = new ArrayList<>();

        for (Recipe recipe : ((RecipeManagerMixin)getPlayer().getEntityWorld().getRecipeManager()).getAllOfTypeAccessor(
                SkillRecipe.Type.INSTANCE
        ).values()) if (recipe instanceof SkillRecipe) recipeList.add((SkillRecipe) recipe);

        recipes = recipeList.stream()
                .filter(recipe -> recipe.getSkill() == skill && Mestiere.COMPONENT.get(getPlayer()).hasPerk(recipe.getPerk()))
                .map((recipe -> new Pair<>(recipe, recipe.canCraft(getPlayer()))))
                .sorted((pair1, pair2) -> {
                    int c = -Boolean.compare(pair1.getRight(), pair2.getRight());
                    return c == 0 ? Integer.compare(pair1.getLeft().getValue(), pair2.getLeft().getValue()) : c;
                })
                .map(Pair::getLeft)
                .collect(Collectors.toList());

        if (!(playerInventory.player instanceof ServerPlayerEntity)) {
            listPanel = new BetterListPanel<>(
                    recipes,
                    RecipeButton::new,
                    (recipe, button) -> button.init(recipe, this)
            );

            root.add(listPanel, 0, 0, 10, 16);
            listPanel.setMargin(0);

            root.add(ARROW, 19, 2, 4, 2);
        }

        WItemSlot outputSlot = new WItemSlot(blockInventory, 0, 1, 1, true, false);
        outputSlot.setModifiable(false);
        root.add(outputSlot, 24, 2);
        root.add(WItemSlot.of(blockInventory, 1), 14, 2);
        root.add(WItemSlot.of(blockInventory, 2), 17, 2);

        root.add(this.createPlayerInventoryPanel(), 11, 6);

        root.validate(this);
    }

    public void dumpInventory() {
        String s = blockInventory.getInvStack(0).toString();
        for (int i = 1; i < blockInventory.getInvSize(); ++i)
            s = s.concat(String.format(", %s", blockInventory.getInvStack(i).toString()));

        Mestiere.debug("Inventory: [%s]", s);
    }

    @Override
    public ItemStack onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
        dumpInventory();
        Mestiere.debug("Slot: %d, Action: %s", slotNumber, action);

        if (slotNumber == 36 && action == SlotActionType.PICKUP && recipe != null) {
            if (recipe.matches((BasicInventory) blockInventory, null)) {
                if (player.inventory.getCursorStack().isEmpty()) {
                    player.inventory.setCursorStack(recipe.craft((BasicInventory) blockInventory));
                    slots.get(slotNumber).markDirty();
                    return player.inventory.getCursorStack();
                }
            }
        }

        return super.onSlotClick(slotNumber, button, action, player);
    }

    public PlayerEntity getPlayer() {
        return playerInventory.player;
    }

    public CraftingInventory getInventory() {
        return (CraftingInventory) blockInventory;
    }

    public ItemStack setRecipe(Identifier id) {
        ItemStack result = ItemStack.EMPTY;

        for (SkillRecipe recipe : recipes) {
            if (recipe.getId().equals(id)) {
                result = setRecipe(recipe);
                break;
            }
        }

        dumpInventory();
        return result;
    }

    public ItemStack setRecipe(SkillRecipe recipe) {
        if (recipe.matches((BasicInventory) blockInventory, null)) {
            ItemStack output = recipe.getOutput();
            blockInventory.setInvStack(0, output);
            this.recipe = recipe;
            return output;
        }
        else return ItemStack.EMPTY;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, blockPos) -> {
            boolean playerAvailable = player.isAlive() || player instanceof ServerPlayerEntity && !((ServerPlayerEntity)player).method_14239();

            for(int i = 0; i < blockInventory.getInvSize(); ++i) {
                dumpInventory();
                if (playerAvailable)
                    player.inventory.offerOrDrop(world, blockInventory.removeInvStack(i));
                else
                    player.dropItem(blockInventory.removeInvStack(i), false);
            }
        });

        INSTANCES.remove(this.syncId);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void scroll(int x, int y, double amount) {
        this.listPanel.onMouseScroll(x, y, amount);
    }
}
