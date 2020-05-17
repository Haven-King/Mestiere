package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.ScrollingGui;
import dev.hephaestus.mestiere.client.gui.widgets.BetterListPanel;
import dev.hephaestus.mestiere.client.gui.widgets.RecipeButton;
import dev.hephaestus.mestiere.mixin.RecipeManagerInvoker;
import dev.hephaestus.mestiere.skills.Skill;
import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.container.BlockContext;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class SkillCraftingController extends CottonCraftingController implements ScrollingGui {
    private static final WSprite ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 179f / 512f, 38f / 256f, 215f / 512f, 56f / 256f);
    private BetterListPanel<SkillRecipe, RecipeButton> listPanel;
    private final BlockContext context;

    private static final HashMap<Integer, SkillCraftingController> INSTANCES = new HashMap<>();

    private final HashMap<Identifier, SkillRecipe> recipeMap = new HashMap<>();
    private SkillRecipe recipe;

    public static SkillCraftingController getInstance(int syncId) {
        return INSTANCES.get(syncId);
    }

    public SkillCraftingController(int syncId, Skill skill, PlayerInventory playerInventory, BlockContext context) {
        super(SkillRecipe.Type.INSTANCE, syncId, playerInventory, new BasicInventory(3), null);
        Mestiere.debug("Player (init): %s", this.playerInventory.player.toString());
        if (playerInventory.player instanceof ServerPlayerEntity)
            INSTANCES.put(syncId, this);
        this.context = context;

        WGridPanel root = new WGridPanel(9);
        setRootPanel(root);

        int maxNumberOfInputs = 0;
        SortedSet<SkillRecipe> recipes = new TreeSet<>();
        for (Recipe recipe : ((RecipeManagerInvoker)getPlayer().getEntityWorld().getRecipeManager()).getAllOfTypeAccessor(SkillRecipe.Type.INSTANCE).values())
            if (recipe instanceof SkillRecipe && ((SkillRecipe) recipe).getSkill() == skill && Mestiere.COMPONENT.get(getPlayer()).hasPerk(((SkillRecipe) recipe).getPerk())) {
                recipeMap.put(recipe.getId(), (SkillRecipe) recipe);
                recipes.add(((SkillRecipe) recipe).withPlayer(playerInventory.player));
                maxNumberOfInputs = Math.max(maxNumberOfInputs, ((SkillRecipe) recipe).numberOfComponents());
            }

        if (!(playerInventory.player instanceof ServerPlayerEntity)) {
            listPanel = new BetterListPanel<>(
                    new ArrayList<>(recipes),
                    RecipeButton::new,
                    (recipe, button) -> button.init(recipe, this)
            );

            root.add(listPanel, 0, 0, 6 + maxNumberOfInputs * 2, 16);
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
        Mestiere.debug("Slot: %d, Action: %s", slotNumber, action);

        if (slotNumber == 36 && recipe != null) {
            if (recipe.matches((BasicInventory) blockInventory, null)) {
                if (action == SlotActionType.PICKUP) {
                    if (player.inventory.getCursorStack().isEmpty()) {
                        player.inventory.setCursorStack(recipe.craft((BasicInventory) blockInventory));
                        if (!recipe.matches((BasicInventory)blockInventory, null))
                            blockInventory.setInvStack(0, ItemStack.EMPTY);
                        return player.inventory.getCursorStack();
                    }
                } else if (action == SlotActionType.QUICK_MOVE) {
                    ItemStack stack = recipe.craft((BasicInventory) blockInventory);
                    player.inventory.offerOrDrop(player.world, stack);
                    if (!recipe.matches((BasicInventory)blockInventory, null))
                        blockInventory.setInvStack(0, ItemStack.EMPTY);
                    return stack;
                }
            }
        }

        return super.onSlotClick(slotNumber, button, action, player);
    }

    public PlayerEntity getPlayer() {
        return playerInventory.player;
    }

    public ItemStack setRecipe(Identifier id) {
        ItemStack result = setRecipe(recipeMap.get(id));
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

            for(int i = 1; i < blockInventory.getInvSize(); ++i) {
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
