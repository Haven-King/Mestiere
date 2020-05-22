package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.widgets.ScrollingGui;
import dev.hephaestus.mestiere.client.gui.screens.SkillCraftingScreen;
import dev.hephaestus.mestiere.client.gui.widgets.WBetterListPanel;
import dev.hephaestus.mestiere.mixin.RecipeManagerInvoker;
import dev.hephaestus.mestiere.skills.Skill;
import io.github.cottonmc.cotton.gui.CottonCraftingController;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
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


    private final HashMap<Identifier, Skill.Recipe> recipeMap = new HashMap<>();
    private final SortedSet<Skill.Recipe> recipes = new TreeSet<>();
    private final ScreenHandlerContext context;
    private final Skill skill;
    private final Collection<RecipeType> types;

    private WBetterListPanel<Skill.Recipe, RecipeButton> listPanel;
    private Skill.Recipe recipe;

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

        listPanel = new WBetterListPanel<>(
                new ArrayList<>(recipes),
                RecipeButton::new,
                (recipe, button) -> button.init(recipe, this, maxNumberOfInputs)
        );

        root.add(listPanel, 0, 0, 6 + maxNumberOfInputs * 2, 16);
        listPanel.setMargin(0);

        root.add(ARROW, 15 + maxNumberOfInputs * 2, 2, 4, 2);

        WItemSlot outputSlot = new WItemSlot(blockInventory, 0, 1, 1, true, false);
        outputSlot.setInsertingAllowed(false);
        root.add(outputSlot, 20 + maxNumberOfInputs * 2, 2);
        root.add(WItemSlot.of(blockInventory, 1), 10 + maxNumberOfInputs * 2, 2);
        root.add(WItemSlot.of(blockInventory, 2), 13 + maxNumberOfInputs * 2, 2);

        root.add(new WLabel(skill.getName().styled((style -> style.withColor(Formatting.DARK_GRAY)))).setAlignment(Alignment.CENTER), 15 + maxNumberOfInputs * 2, 0);

        root.add(this.createPlayerInventoryPanel(), 7 + maxNumberOfInputs * 2, 8);

        root.validate(this);
    }

    private int initializeRecipes() {
        int r = 0;
        for (RecipeType type : types) {
            //noinspection unchecked
            for (Object recipe : ((RecipeManagerInvoker) getPlayer().getEntityWorld().getRecipeManager()).getAllOfTypeAccessor(type).values()) {
                if (recipe instanceof Skill.Recipe && ((Skill.Recipe) recipe).getSkill() == skill && Mestiere.COMPONENT.get(getPlayer()).hasPerk(((Skill.Recipe) recipe).getPerk())) {
                    this.recipeMap.put(((Skill.Recipe) recipe).getId(), (Skill.Recipe) recipe);
                    this.recipes.add(((Skill.Recipe) recipe).withPlayer(playerInventory.player));
                    r = Math.max(r, ((Skill.Recipe) recipe).numberOfInputs());
                }
            }
        }

        return r;
    }

    @Override
    public ItemStack onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
        if (slotNumber == 36 && recipe != null && recipe.matches((BasicInventory) blockInventory, null)) {
            recipe.craft((BasicInventory) blockInventory, player);
            context.run((world, pos) -> {
                world.playSound(pos.getX(), pos.getY(), pos.getZ(), skill.getCraftSound(), SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            });
        } else if (recipe == null) {
            setRecipeIfMatchFound();
        }

        ItemStack result = super.onSlotClick(slotNumber, button, action, player);

        updateOutput();

        return result;
    }

    private void setRecipeIfMatchFound() {
        for (Skill.Recipe recipe : recipes) {
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

    public ActionResult setRecipe(Skill.Recipe recipe) {
        if (recipe == null) return ActionResult.FAIL;

        this.recipe = recipe;
        return updateOutput();
    }

    public ActionResult updateOutput() {
        if (recipe != null && recipe.matches((BasicInventory) blockInventory, null)) {
            blockInventory.setStack(0, recipe.getOutput((BasicInventory) blockInventory, getPlayer()));
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

        updateOutput();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void scroll(int x, int y, double amount) {
        this.listPanel.onMouseScroll(x, y, amount);
    }

    public static class Builder {
        public final static SimpleRegistry<Builder> REGISTRY = new SimpleRegistry<>();
        public static Builder registerContainer(Block block, Skill skill) {
            return Registry.register(REGISTRY, Mestiere.newID(Registry.BLOCK.getId(block).getPath()), new Builder(block, skill));
        }

        public static void registerAllContainers() {
            for (Builder provider : REGISTRY) {
                provider.registerContainer();
            }
        }

        @Environment(EnvType.CLIENT)
        public static void registerAllScreenProviders() {
            for (Builder provider : REGISTRY) {
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

    private static class RecipeButton extends WButton {
        private static final WSprite ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 15f / 512f, 171f / 256f, 25f / 512f, 180f / 256f);
        private static final WSprite CROSSED_ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 25f / 512f, 171f / 256f, 35f / 512f, 180f / 256f);

        static {
            ARROW.setSize(10, 9);
            CROSSED_ARROW.setSize(10, 9);
        }


        private Skill.Recipe recipe;

        public RecipeButton() {

        }

        private int animCounter = 0;
        private SkillCrafter controller;
        private int maxNumberOfInputs = 1;

        public void init(Skill.Recipe recipe, SkillCrafter controller, int maxNumberOfInputs) {
            this.recipe = recipe;
            this.setEnabled(recipe.canCraft(controller.getPlayer()));

            this.controller = controller;
            this.maxNumberOfInputs = maxNumberOfInputs;
        }


        @Override
        @Environment(EnvType.CLIENT)
        public void paintBackground(int x, int y, int mouseX, int mouseY) {
            ++animCounter;

            super.paintBackground(x, y, mouseX, mouseY);

            if (this.isEnabled())
                ARROW.paintBackground(x + 9 + 18*(maxNumberOfInputs), y + 6);
            else
                CROSSED_ARROW.paintBackground(x + 9 + 18*(maxNumberOfInputs), y + 6);

            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            for (int i = 0; i < recipe.numberOfInputs(); ++i) {
                ItemStack itemStack = recipe.getItem(i, animCounter);
                itemRenderer.renderGuiItem(itemStack, x+2 + 18*i, y+2);
                itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x+2 + 18*i, y+2);
            }

            itemRenderer.renderGuiItem(recipe.getOutput(), x+45 + 18*(maxNumberOfInputs-1), y+2);
            itemRenderer.renderGuiItemOverlay(textRenderer, recipe.getOutput(), x+45 + 18*(maxNumberOfInputs-1), y+2);
        }

        @Override
        public void onClick(int x, int y, int button) {
            if (this.isEnabled()) {
                controller.setRecipe(recipe);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeByte(controller.syncId);
                buf.writeIdentifier(recipe.getId());
                ClientSidePacketRegistry.INSTANCE.sendToServer(Mestiere.SELECT_RECIPE_ID, buf);
            }
        }
    }
}
