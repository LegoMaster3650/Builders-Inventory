package _3650.builders_inventory.feature.extended_inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryContainer implements Container {
	
	private ExtendedInventoryPage page = ExtendedInventoryPage.INVALID;
	private int pageId = -1;
	private boolean valid = false;
	private boolean locked = true;
	
	public void reset() {
		this.page = ExtendedInventoryPage.INVALID;
		this.pageId = -1;
		this.valid = false;
		this.locked = true;
	}
	
	public ExtendedInventoryContainer() {}
	
	public void setPage(int page) {
		this.page = ExtendedInventoryPages.get(page);
		this.pageId = page;
		this.refresh();
		ExtendedInventoryPages.setChanged();
	}
	
	public void refresh() {
		this.valid = this.page.valid;
		this.locked = this.page.isLocked();
	}
	
	public int getPage() {
		return this.pageId;
	}
	
	public String getName() {
		return this.page.getName();
	}
	
	public void setName(String name) {
		this.page.setName(name);
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public boolean canModify() {
		return valid && !locked;
	}
	
	public void setLocked(boolean locked) {
		this.page.setLocked(locked);
	}
	
	@Override
	public int getContainerSize() {
		return 54;
	}
	
	@Override
	public boolean isEmpty() {
		return page.isEmpty();
	}
	
	@Override
	public ItemStack getItem(int slot) {
		if (!valid) return ItemStack.EMPTY;
		return slot >= 0 && slot < 54 ? page.get(slot) : ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (!valid) return ItemStack.EMPTY;
		ItemStack stack = slot >= 0 && slot < 54 && !(page.get(slot)).isEmpty() && amount > 0 ? (page.get(slot)).split(amount) : ItemStack.EMPTY;
		if (!stack.isEmpty()) this.setChanged();
		return stack;
	}
	
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (!valid) return ItemStack.EMPTY;
		ItemStack stack = page.get(slot);
		if (stack.isEmpty()) return ItemStack.EMPTY;
		else {
			this.setItem(slot, ItemStack.EMPTY); // screw the "no update" this is very important to update
			return stack;
		}
	}
	
	@Override
	public void setItem(int slot, ItemStack stack) {
		if (!valid) return;
		page.set(slot, stack);
		this.setChanged();
	}
	
	public ItemStack swapItem(int slot, ItemStack stack) {
		if (!valid) return ItemStack.EMPTY;
		ItemStack old = page.set(slot, stack);
		this.setChanged();
		return old;
	}
	
	@Override
	public void setChanged() {
		if (valid) page.setChanged();
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	@Override
	public void clearContent() {
		if (!valid) return;
		page.clear();
		this.setChanged();
	}
	
}
