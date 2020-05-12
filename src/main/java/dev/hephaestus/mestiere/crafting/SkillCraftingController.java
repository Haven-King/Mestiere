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
import net.minecraft.container.BlockContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SkillCraftingController extends CottonCraftingController implements ScrollingGui {
    private static final WSprite ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 179f / 512f, 38f / 256f, 215f / 512f, 56f / 256f);
    private BetterListPanel<SkillRecipe, RecipeButton> listPanel;
    public final SkillCrafterInventory inventory;
    private final BlockContext context;

    private final List<SkillRecipe> recipes;

    private static final HashMap<Integer, SkillCraftingController> INSTANCES = new HashMap<>();

    public static SkillCraftingController getInstance(int syncId) {
        return INSTANCES.get(syncId);
    }

    public SkillCraftingController(int syncId, Skill skill, PlayerInventory playerInventory, BlockContext context) {
        super(SkillRecipe.Type.INSTANCE, syncId, playerInventory, new SkillCrafterInventory(2), null);
        INSTANCES.put(syncId, this);
        this.context = context;

        this.inventory = ((SkillCrafterInventory)blockInventory);
        this.inventory.setContainer(this);

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

    public PlayerEntity getPlayer() {
        return playerInventory.player;
    }

    public void setRecipe(Identifier id) {
        for (SkillRecipe recipe : recipes) {
            if (recipe.getId().equals(id) && setRecipe(recipe).isAccepted()) {
                break;
            }
        }
    }

    private void update(int slot, ItemStack stack) {
        inventory.setInvStack(slot, stack);
        if (getPlayer() instanceof ServerPlayerEntity)
            ((ServerPlayerEntity)playerInventory.player).networkHandler.sendPacket(new ContainerSlotUpdateS2CPacket(syncId, slot, stack));
    }

    private ActionResult takeIngredient(SkillRecipe recipe, int slot) {
        Ingredient ingredient = slot == 1 ? recipe.getFirstIngredient() : recipe.getSecondIngredient();
        int ingredientCount = slot == 1 ? recipe.getFirstIngredientCount() : recipe.getSecondIngredientCount();

        ItemStack stack1 = inventory.getInvStack(slot);
        if (!ingredient.test(stack1) || stack1.getCount() < ingredientCount) {
            for (int i = 0; i < playerInventory.main.size(); ++i) {
                ItemStack stack = playerInventory.main.get(i);
                boolean bl1 = ingredient.test(stack);
                Mestiere.debug("%b, %d, %d", bl1, stack.getCount(), ingredientCount);
                if (bl1 && stack.getCount() >= ingredientCount) {
                    Mestiere.debug("Putting %s in slot %d", stack.getItem().toString(), slot);
                    playerInventory.main.set(i, stack1);
                    update(slot, stack);
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.FAIL;
    }

    public ActionResult setRecipe(SkillRecipe recipe) {
        if (inventory.validateRecipe(recipe).isAccepted()) return ActionResult.SUCCESS;

        if (!takeIngredient(recipe, 1).isAccepted()) return ActionResult.FAIL;
        if (!takeIngredient(recipe, 2).isAccepted()) return ActionResult.FAIL;

        if (inventory.validateRecipe(recipe).isAccepted()) return ActionResult.SUCCESS;
        else return ActionResult.FAIL;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, blockPos) -> {
            boolean playerAvailable = player.isAlive() || player instanceof ServerPlayerEntity && !((ServerPlayerEntity)player).method_14239();

            for(int i = 1; i < inventory.getInvSize(); ++i) {
                if (playerAvailable)
                    player.inventory.offerOrDrop(world, inventory.removeInvStack(i));
                else
                    player.dropItem(inventory.removeInvStack(i), false);
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
