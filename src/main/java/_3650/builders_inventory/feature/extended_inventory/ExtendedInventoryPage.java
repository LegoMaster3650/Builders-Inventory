package _3650.builders_inventory.feature.extended_inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryPage {
	
	public static final ExtendedInventoryPage INVALID = new ExtendedInventoryPage(false);
	
	private final NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
	
	public final boolean valid ;
	
	private boolean locked = true;
	private String name = "";
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
	
	public ListTag createTag() {
		var tag = new ListTag();
		
		for (var stack : items) {
			tag.add(stack.save(new CompoundTag()));
		}
		
		return tag;
	}

	public static ExtendedInventoryPage of(ListTag items, boolean locked, String name) {
		var page = new ExtendedInventoryPage(true);
		
		for (int i = 0; i < items.size(); i++) {
			page.items.set(i, ItemStack.of(items.getCompound(i)));
		}
		page.changed = false;
		page.locked = locked;
		
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
	
}
