package dev.hephaestus.mestiere.util;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.Iterator;

public class OreManager extends PersistentState {
    private static final String SAVE_KEY = Mestiere.MOD_ID + "_ores";
    private final ServerWorld world;

    private ArrayList<Long> ores = new ArrayList<>();

    public static OreManager getInstance(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new OreManager(world), SAVE_KEY);
    }

    public OreManager(ServerWorld world) {
        super(SAVE_KEY);
        this.world = world;
        this.markDirty();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ores.clear();
        long[] ores = tag.getLongArray(SAVE_KEY);
        for (Long l : ores) {
            this.ores.add(l);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.put(SAVE_KEY, new LongArrayTag(this.ores));
        return tag;
    }

    public void put(BlockPos pos) {
        ores.add(pos.asLong());
        this.markDirty();
    }

    public void update() {
        Mestiere.debug("Marking %d ores for update", ores.size());
        for (Long l : ores) {
            world.getChunkManager().markForUpdate(BlockPos.fromLong(l));
        }
    }
}
