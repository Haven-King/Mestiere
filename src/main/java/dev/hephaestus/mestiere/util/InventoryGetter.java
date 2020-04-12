package dev.hephaestus.mestiere.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

import java.util.List;

public interface InventoryGetter {
    List<DefaultedList<ItemStack>> getInventory();
}
