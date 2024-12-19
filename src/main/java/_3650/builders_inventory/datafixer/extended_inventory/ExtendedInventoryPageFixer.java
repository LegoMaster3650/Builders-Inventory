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
			return Optional.empty();
		case 1:
			tag = update1to2(tag, dataFixer);
		}
		
		return Optional.of(tag);
	}
	
	private static CompoundTag update1to2(CompoundTag old, DataFixer dataFixer) {
		if (old == null) return null;
		
		// new tag
		CompoundTag tag = new CompoundTag();
		
		// unchanged values
		if (old.contains("locked", Tag.TAG_BYTE)) tag.putBoolean("locked", old.getBoolean("locked"));
		if (old.contains("name", Tag.TAG_STRING)) tag.putString("name", old.getString("name"));

		
		// new values
		
		// DataVersion
		NbtUtils.addCurrentDataVersion(tag);
		
		
		// changed values
		
		// items
		if (!old.contains("items", Tag.TAG_LIST)) {
			BuildersInventory.LOGGER.error("Could not update inventory page data {}: No valid items tag", old);
			return tag;
		}
		
		ListTag oldItems = old.getList("items", Tag.TAG_COMPOUND);
		if (oldItems == null || oldItems.isEmpty()) {
			BuildersInventory.LOGGER.error("Could not update inventory page items {}: Invalid items tag", oldItems);
			return tag;
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
