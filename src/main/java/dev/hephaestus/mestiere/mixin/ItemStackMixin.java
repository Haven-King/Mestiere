package dev.hephaestus.mestiere.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow public abstract Item getItem();

	private EquipmentSlot equipmentSlot;

	@Inject(method = "getAttributeModifiers", at = @At("HEAD"))
	private void captureEquipmentSlot(EquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
		this.equipmentSlot = equipmentSlot;
	}

	@Redirect(method = "getAttributeModifiers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/HashMultimap;create()Lcom/google/common/collect/HashMultimap;"))
	private HashMultimap<EntityAttribute, EntityAttributeModifier> initializeAttributeMap() {
		HashMultimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
		map.putAll(this.getItem().getAttributeModifiers(this.equipmentSlot));
		return map;
	}
}
