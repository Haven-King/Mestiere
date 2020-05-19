package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.widgets.ScrollingGui;
import dev.hephaestus.mestiere.client.gui.screens.SkillCraftingScreen;
import dev.hephaestus.mestiere.client.gui.widgets.BetterListPanel;
import dev.hephaestus.mestiere.client.gui.widgets.RecipeButton;
import dev.hephaestus.mestiere.mixin.RecipeManagerInvoker;
import dev.hephaestus.mestiere.skills.Skill;
import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.*;

public class SkillCrafter extends CottonCraftingController implements ScrollingGui {
    private static final WSprite ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 179f / 512f, 38f / 256f, 215f / 512f, 56f / 256f);
    private static final HashMap<Integer, SkillCrafter> INSTANCES = new HashMap<>();
    public static SkillCrafter getInstance(int syncId) {
        return INSTANCES.get(syncId);
    }


    private final HashMap<Identifier, SkillRecipe> recipeMap = new HashMap<>();
    private final SortedSet<SkillRecipe> recipes = new TreeSet<>();
    private final ScreenHandlerContext context;
    private final Skill skill;
    private final Collection<RecipeType> types;

    private BetterListPanel<SkillRecipe, RecipeButton> listPanel;
    private SkillRecipe recipe;

    public SkillCrafter(int syncId, Skill skill, Collection<RecipeType> types, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext) {
        super(null, syncId, playerInventory, new BasicInventory(3), null);
        this.context = screenHandlerContext;
        this.skill = skill;
        this.types = types;

        // Register our Controller for syncing purposes
        if (playerInventory.player instanceof ServerPlayerEntity) INSTANCES.put(syncId, this);

        drawGui(initializeRecipes());
    }

    // Yes there's a lot of magic numbers in here. There always are in guis!
    private void drawGui(int maxNumberOfInputs) {
        WGridPanel root = new WGridPanel(9);
        setRootPanel(root);

        listPanel = new BetterListPanel<>(
                new ArrayList<>(recipes),
                RecipeButton::new,
                (recipe, button) -> button.init(recipe, this, maxNumberOfInputs)
        );

        root.add(listPanel, 0, 0, 6 + maxNumberOfInputs * 2, 16);
        listPanel.setMargin(0);

        root.add(ARROW, 15 + maxNumberOfInputs * 2, 2, 4, 2);

        WItemSlot outputSlot = new WItemSlot(blockInventory, 0, 1, 1, true, false);
        outputSlot.setModifiable(false);
        root.add(outputSlot, 20 + maxNumberOfInputs * 2, 2);
        root.add(WItemSlot.of(blockInventory, 1), 10 + maxNumberOfInputs * 2, 2);
        root.add(WItemSlot.of(blockInventory, 2), 13 + maxNumberOfInputs * 2, 2);

        root.add(new WLabel(skill.getText(Mestiere.KEY_TYPE.NAME).styled((style -> style.withColor(Formatting.DARK_GRAY)))).setAlignment(Alignment.CENTER), 15 + maxNumberOfInputs * 2, 0);

        root.add(this.createPlayerInventoryPanel(), 7 + maxNumberOfInputs * 2, 8);

        root.validate(this);

    }

    private int initializeRecipes() {
        int r = 0;
        for (RecipeType type : types) {
            for (Object recipe : ((RecipeManagerInvoker) getPlayer().getEntityWorld().getRecipeManager()).getAllOfTypeAccessor(type).values()) {
                if (recipe instanceof SkillRecipe && ((SkillRecipe) recipe).getSkill() == skill && Mestiere.COMPONENT.get(getPlayer()).hasPerk(((SkillRecipe) recipe).getPerk())) {
                    this.recipeMap.put(((SkillRecipe) recipe).getId(), (SkillRecipe) recipe);
                    this.recipes.add(((SkillRecipe) recipe).withPlayer(playerInventory.player));
                    r = Math.max(r, ((SkillRecipe) recipe).numberOfInputs());
                }
            }
        }

        return r;
    }

    @Override
    public ItemStack onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
        ItemStack result = null;
        if (slotNumber == 36 && recipe != null) {
            if (recipe.matches((BasicInventory) blockInventory, null)) {
                if (action == SlotActionType.PICKUP && player.inventory.getCursorStack().isEmpty()) {
                    player.inventory.setCursorStack(recipe.craft((BasicInventory) blockInventory));
                    result = player.inventory.getCursorStack();
                } else if (action == SlotActionType.QUICK_MOVE) {
                    ItemStack stack = recipe.craft((BasicInventory) blockInventory);
                    player.inventory.offerOrDrop(player.world, stack);
                    result = stack;
                }
            }
        }

        if (result == null) {
            result = super.onSlotClick(slotNumber, button, action, player);
        }

        if (recipe == null)
            setRecipeIfMatchFound();


        if (recipe != null)
            updateResult();

        return result;
    }

    private void setRecipeIfMatchFound() {
        for (SkillRecipe recipe : recipes) {
            if (recipe.matches((BasicInventory) blockInventory)) {
                setRecipe(recipe);
                break;
            }
        }
    }

    public PlayerEntity getPlayer() {
        return playerInventory.player;
    }

    public ActionResult setRecipe(Identifier id) {
        return setRecipe(recipeMap.get(id));
    }

    public ActionResult setRecipe(SkillRecipe recipe) {
        if (recipe == null) return ActionResult.FAIL;

        this.recipe = recipe;
        return updateResult();
    }

    public ActionResult updateResult() {
        if (recipe.matches((BasicInventory) blockInventory, null)) {
            blockInventory.setStack(0, recipe.getOutput((BasicInventory) blockInventory));
            return ActionResult.SUCCESS;
        }
        else {
            blockInventory.setStack(0, ItemStack.EMPTY);
            return ActionResult.PASS;
        }
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, blockPos) -> {
            boolean playerAvailable = player.isAlive() || player instanceof ServerPlayerEntity && !((ServerPlayerEntity)player).isDisconnected();

            for(int i = 1; i < blockInventory.size(); ++i) {
                if (playerAvailable)
                    player.inventory.offerOrDrop(world, blockInventory.removeStack(i));
                else
                    player.dropItem(blockInventory.removeStack(i), false);
            }
        });

        INSTANCES.remove(this.syncId);
    }

    public void fillInputSlots() {
        if (recipe != null)
            recipe.fillInputSlots(playerInventory, blockInventory);

        updateResult();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void scroll(int x, int y, double amount) {
        this.listPanel.onMouseScroll(x, y, amount);
    }

    public static class Builder {
        public final static SimpleRegistry<Builder> PROVIDERS = new SimpleRegistry<>();
        public static Builder registerContainer(Block block, Skill skill) {
            return Registry.register(PROVIDERS, Mestiere.newID(Registry.BLOCK.getId(block).getPath()), new Builder(block, skill));
        }

        public static void registerAllContainers() {
            for (Builder provider : PROVIDERS) {
                provider.registerContainer();
            }
        }

        @Environment(EnvType.CLIENT)
        public static void registerAllScreenProviders() {
            for (Builder provider : PROVIDERS) {
                provider.registerScreenProvider();
            }
        }

        public final Block block;
        private final Skill skill;
        private final ArrayList<RecipeType> types;

        public Builder(Block block, Skill skill) {
            this.block = block;
            this.skill = skill;
            this.types = new ArrayList<>();
        }

        public void addTypes(RecipeType... types) {
            this.types.addAll(Arrays.asList(types));
        }

        private SkillCrafter buildContainer(int syncId, Identifier id, PlayerEntity player, PacketByteBuf buf) {
            return new SkillCrafter(syncId,
                    this.skill,
                    this.types,
                    player.inventory,
                    ScreenHandlerContext.create(player.world, buf.readBlockPos()));
        }


        public void registerContainer() {
            ContainerProviderRegistry.INSTANCE.registerFactory(Registry.BLOCK.getId(this.block), this::buildContainer);
        }

        @Environment(EnvType.CLIENT)
        private SkillCraftingScreen buildScreen(int syncId, Identifier id, PlayerEntity player, PacketByteBuf buf) {
            return new SkillCraftingScreen(buildContainer(syncId, id, player, buf));
        }

        @Environment(EnvType.CLIENT)
        public void registerScreenProvider() {
            ScreenProviderRegistry.INSTANCE.registerFactory(Registry.BLOCK.getId(block), this::buildScreen);
        }
    }
}
