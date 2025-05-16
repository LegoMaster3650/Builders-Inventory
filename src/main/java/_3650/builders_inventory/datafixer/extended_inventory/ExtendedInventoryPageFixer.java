package _3650.builders_inventory.datafixer.extended_inventory;

import java.util.Optional;

import com.mojang.datafixers.DataFixer;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.datafixer.ModDataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.References;

public class ExtendedInventoryPageFixer {
	
	private static final int VERSION_1_20_4 = 3700;
	
	public static Optional<CompoundTag> update(CompoundTag tag, int version) {
		Minecraft mc = Minecraft.getInstance();
		DataFixer dataFixer = mc.getFixerUpper();
		
		// do not put break after any updates, they're meant to be sequential
		switch (version) {
		default:
			return tryUpdateItems(tag, dataFixer);
		case 1:
			tag = update1to2(tag, dataFixer);
		}
		
		return Optional.ofNullable(tag);
	}
	
	private static Optional<CompoundTag> tryUpdateItems(CompoundTag old, DataFixer dataFixer) {
		int oldVersion = NbtUtils.getDataVersion(old, VERSION_1_20_4);
		if (oldVersion < ModDataFixer.currentVersion()) {
			BuildersInventory.LOGGER.info("Updating outdated items for extended inventory");
			
			if (!old.contains("items", Tag.TAG_LIST)) {
				BuildersInventory.LOGGER.error("Could not update inventory page data {}: No valid items tag", old);
				return Optional.empty();
			}
			
			CompoundTag tag = old.copy();
			ListTag oldItems = old.getList("items", Tag.TAG_COMPOUND);
			if (oldItems == null || oldItems.isEmpty()) {
				BuildersInventory.LOGGER.error("Could not update inventory page items {}: Invalid items tag", oldItems);
				return Optional.empty();
			}
			
			ListTag items = new ListTag();
			for (int i = 0; i < oldItems.size(); i++) {
				CompoundTag item = ModDataFixer.updateToCurrentVersion(dataFixer, oldItems.getCompound(i), oldVersion, References.ITEM_STACK);
				items.add(item);
			}
			tag.put("items", items);
			
			if (old.contains("icon", Tag.TAG_COMPOUND)) {
				tag.put("icon", ModDataFixer.updateToCurrentVersion(dataFixer, old.getCompound("icon"), oldVersion, References.ITEM_STACK));
			}
			
			if (old.contains("original_icon", Tag.TAG_COMPOUND)) {
				tag.put("original_icon", ModDataFixer.updateToCurrentVersion(dataFixer, old.getCompound("original_icon"), oldVersion, References.ITEM_STACK));
			}
			
			return Optional.of(tag);
		}
		return Optional.empty();
	}
	
	private static CompoundTag update1to2(CompoundTag old, DataFixer dataFixer) {
		if (old == null) return null;
		
		// items
		if (!old.contains("items", Tag.TAG_LIST)) {
			BuildersInventory.LOGGER.error("Could not update inventory page data {}: No valid items tag", old);
			return null;
		}
		
		CompoundTag tag = old.copy();
		ListTag oldItems = old.getList("items", Tag.TAG_COMPOUND);
		if (oldItems == null || oldItems.isEmpty()) {
			BuildersInventory.LOGGER.error("Could not update inventory page items {}: Invalid items tag", oldItems);
			return null;
		}
		
		ListTag items = new ListTag();
		for (int i = 0; i < oldItems.size(); i++) {
			CompoundTag item = ModDataFixer.updateToCurrentVersion(dataFixer, oldItems.getCompound(i), VERSION_1_20_4, References.ITEM_STACK);
			if (item.contains("id", Tag.TAG_STRING) && item.getString("id").equals("minecraft:air")) {
				items.add(new CompoundTag());
			} else {
				items.add(item);
			}
		}
		tag.put("items", items);
		
		// version
		tag.putInt("version", 2);
		
		return tag;
	}
	
}
