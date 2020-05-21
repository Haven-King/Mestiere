package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

import static net.minecraft.util.Util.createTranslationKey;

public class Skill {
    private static final SimpleRegistry<Skill> REGISTRY = new SimpleRegistry<>();
    public static Skill NONE;
    public static Skill PRAYER;
    public static Skill SMITHING;
    public static Skill LEATHERWORKING;
    public static Skill FARMING;
    public static Skill SLAYING;
    public static Skill HUNTING;
    public static Skill MINING;


    public static Skill register(Skill skill) {
        return Registry.register(REGISTRY, skill.id, skill);
    }

    public static void forEach(Consumer<? super Skill> action) {
        REGISTRY.forEach(action);
    }

    public static Skill get(Identifier id) {
        return REGISTRY.get(id);
    }

    public final Identifier id;
    public final Formatting format;
    public final SoundEvent sound;
    public final ItemStack icon;

    private final Text name;

    public Skill(Identifier id, Formatting format, SoundEvent sound, ItemStack icon) {
        this.id = id;
        this.format = format;
        this.sound = sound;
        this.icon = icon;

        this.name = new TranslatableText(createTranslationKey("skill", Mestiere.newID(this.id.getPath() + ".name"))).formatted(format);
    }

    public Skill(Identifier id, Formatting format, ItemStack icon) {
        this(id, format, null, icon);
    }

    public MutableText getName() {
        return this.name.copy();
    }

    public static class Perk implements Comparable<Perk> {
        public static Perk NONE;
        public static Perk INVALID;

        public static Perk IRON_INGOT_SMITH;
        public static Perk GOLD_INGOT_SMITH;
        public static Perk DIAMOND_SMITH;
        public static Perk NETHERITE_SMITH;

        public static Perk HUNTER;
        public static Perk SHARP_SHOOTER;

        public static Perk GATHERER;
        public static Perk SEX_GURU;
        public static Perk GREEN_THUMB;

        public static Perk SLAYER;
        public static Perk SNIPER;

        private static final SimpleRegistry<Perk> REGISTRY = new SimpleRegistry<>();
        private static final HashMap<Identifier, ArrayList<Perk>> SKILL_TO_PERK_MAP = new HashMap<>();

        public static Perk register(Perk perk) {
            Registry.register(REGISTRY, perk.id, perk);
            SKILL_TO_PERK_MAP.putIfAbsent(perk.skill.id, new ArrayList<>());
            SKILL_TO_PERK_MAP.get(perk.skill.id).add(perk);
            return perk;
        }

        public static Perk get(Identifier id) {
            return REGISTRY.get(id) == null ? INVALID : REGISTRY.get(id);
        }

        public static List<Perk> list(Skill skill) {
            SKILL_TO_PERK_MAP.putIfAbsent(skill.id, new ArrayList<>());
            return SKILL_TO_PERK_MAP.get(skill.id);
        }

        public final Identifier id;
        public final Skill skill;
        public final int level;
        public final ItemStack icon;

        private Text name;
        private Text description;
        private Text message;

        private boolean hardcore = false;
        private boolean scalesWithLevel = false;
        private int maxLevel = -1;


        public Perk(Skill skill, String id, int level, Item icon) {
            this.skill = skill;
            this.id = Mestiere.newID(skill.id.getPath() + "." + id);
            this.level = level;
            this.icon = new ItemStack(icon, 1);

            this.name = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".name")));
            this.description = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".description")));
            this.message = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".message")));
        }

        public Perk isHardcore(boolean hardcore) {
            this.hardcore = hardcore;
            return this;
        }

        public Perk scales(int maxLevel) {
            if (maxLevel > this.level) {
                this.scalesWithLevel = true;
                this.maxLevel = maxLevel;
            }
            return this;
        }

        public void setName(Text text) {
            this.name = text;
        }

        public void setDescription(Text text) {
            this.description = text;
        }

        public void setMessage(Text text) {
            this.message = text;
        }

        public MutableText getName() {
            return this.name.copy();
        }

        public MutableText getDescription() {
            return this.description.copy();
        }

        public MutableText getMessage() {
            return this.message.copy();
        }

        public List<Text> getTooltips() {
            List<Text> tooltips = new ArrayList<>();

            tooltips.add(this.getName().formatted(this.skill.format));
            tooltips.add(this.getDescription());
            if (this.scales()) {
                tooltips.add(new TranslatableText("mestiere.scales", this.maxLevel).styled(style -> style.withColor(Formatting.DARK_GRAY)));
            }

            return tooltips;
        }

        public boolean isHardcore() {
            return hardcore;
        }

        public boolean scales() {
            return scalesWithLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public int compareTo(Perk perk) {
            return Integer.compare(this.level, perk.level);
        }
    }

    public abstract static class Recipe implements net.minecraft.recipe.Recipe<BasicInventory>, Comparable  {
        public static class Type {
            public static RecipeType NETHERITE;
            public static RecipeType LEATHERWORKING;
            public static RecipeType ARMOR;
            public static RecipeType TOOLS;

            public static RecipeType register(Identifier id, RecipeType<?> type, RecipeSerializer<?> serializer) {
                Registry.register(Registry.RECIPE_SERIALIZER, id, serializer);
                return Registry.register(Registry.RECIPE_TYPE, id, type);
            }
        }

        private final Identifier id;
        private final Skill skill;
        private final Perk perk;
        private final int value;

        private PlayerEntity player;


        public Recipe(Identifier id, Skill skill, Perk perk, int value) {
            this.id = id;
            this.skill = skill;
            this.perk = perk;
            this.value = value;
        }

        @SuppressWarnings("CopyConstructorMissesField")
        public Recipe(Recipe recipe) {
            this.id = recipe.id;
            this.skill = recipe.skill;
            this.perk = recipe.perk;
            this.value = recipe.value;
        }

        public Recipe(Identifier id, PacketByteBuf buf) {
            this.id = id;
            this.skill = get(buf.readIdentifier());
            this.perk = Perk.get(buf.readIdentifier());
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

            if (o instanceof Recipe) {
                result = -Boolean.compare(canCraft(getPlayer()), ((Recipe) o).canCraft(getPlayer()));
                result = result == 0 ? Integer.compare(getValue(), ((Recipe)o).getValue()) : result;
                result = result == 0 ? getId().compareTo(((Recipe) o).getId()) : result;
            }
            return result;
        }

        public abstract boolean matches(BasicInventory inventory);

        public abstract int numberOfInputs();
        public abstract ItemStack getOutput(BasicInventory inv);

        public int getValue() {return this.value;}
        public Perk getPerk() {return this.perk;}
        public Skill getSkill() {return this.skill;}

        public Recipe withPlayer(PlayerEntity player) {
            this.player = player;
            return this;
        }

        public PlayerEntity getPlayer() {return this.player;}

        public boolean canCraft(PlayerEntity playerEntity) {
            return Mestiere.COMPONENT.get(player).hasPerk(getPerk()) || player.world.getGameRules().getBoolean(Mestiere.HARDCORE) || !getPerk().isHardcore();
        }

        protected void write(PacketByteBuf buf) {
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
            public abstract IntList getRawIds();
            public abstract int count();
            public abstract boolean matches(ItemStack stack);
            public abstract void write(PacketByteBuf buf);
        }
    }
}
