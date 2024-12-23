package _3650.builders_inventory.feature.extended_inventory;

import _3650.builders_inventory.BuildersInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryPage {
	
	public static final ExtendedInventoryPage INVALID = new ExtendedInventoryPage(false);
	
	private final NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
	
	public final boolean valid ;
	
	private boolean locked = true;
	private String name = "";
	public ItemStack icon = ItemStack.EMPTY;
	public ItemStack originalIcon = ItemStack.EMPTY;
	public boolean iconDataActive = false;
	public int iconScaleDown = 0;
	private boolean changed = false;
	
	public ExtendedInventoryPage() {
		this(true);
		this.changed = true;
	}
	
	private ExtendedInventoryPage(boolean valid) {
		this.valid = valid;
	}
	
	public String getName() {
		return this.name;
	}
	
	private void setNameInternal(String name) {
		this.name = name.isBlank() ? "" : name;
	}
	
	public void setName(String name) {
		if (!this.name.equals(name)) this.setChanged();
		this.setNameInternal(name);
	}
	
	public boolean isLocked() {
		return this.locked;
	}
	
	public void setLocked(boolean locked) {
		this.locked = locked;
		this.setChanged();
	}
	
	public ItemStack get(int slot) {
		return locked ? items.get(slot).copy() : items.get(slot);
	}
	
	public ItemStack set(int slot, ItemStack stack) {
		return valid ? locked ? items.get(slot).copy() : items.set(slot, stack) : stack;
	}
	
	public void clear() {
		if (valid && !locked) items.clear();
	}
	
	public ListTag createTag(HolderLookup.Provider registryAccess) {
		var tag = new ListTag();
		
		for (var stack : items) {
			var result = ItemStack.OPTIONAL_CODEC.encodeStart(registryAccess.createSerializationContext(NbtOps.INSTANCE), stack).resultOrPartial();
			if (result.isPresent()) tag.add(result.get());
		}
		
		return tag;
	}

	public static ExtendedInventoryPage of(
			HolderLookup.Provider registryAccess,
			ListTag items,
			boolean locked,
			String name,
			ItemStack icon,
			ItemStack originalIcon,
			boolean iconDataActive,
			int iconScaleDown) {
		var page = new ExtendedInventoryPage(true);
		
		for (int i = 0; i < items.size(); i++) {
			CompoundTag itemTag = items.getCompound(i);
			
			var result = ItemStack.OPTIONAL_CODEC
					.parse(registryAccess.createSerializationContext(NbtOps.INSTANCE), itemTag)
					.resultOrPartial(err -> BuildersInventory.LOGGER.error("Could not parse extended inventory item: '{}'", err))
					.orElse(ItemStack.EMPTY);
			page.items.set(i, result);
		}
		page.changed = false;
		page.locked = locked;
		page.icon = icon;
		page.originalIcon = originalIcon;
		page.iconDataActive = iconDataActive;
		page.iconScaleDown = iconScaleDown;
		
		page.setNameInternal(name);
		
		return page;
	}
	
	public boolean isEmpty() {
		for (var stack : items) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}
	
	public void setChanged() {
		changed = true;
		ExtendedInventoryPages.setChanged();
	}
	
	public boolean resetChanged() {
		boolean val = changed;
		changed = false;
		return val;
	}
	
	void discreteChange() {
		changed = true;
	}
	
}
