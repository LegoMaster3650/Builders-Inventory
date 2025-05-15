package _3650.builders_inventory.feature.extended_inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExtendedInventoryMenu extends AbstractContainerMenu {
	
	public final Slot destroyItemSlot;
	
	private final InventoryMenu inventoryMenu;
	
	protected ExtendedInventoryMenu(int containerId, InventoryMenu playerInv) {
		super(null, containerId);
		this.inventoryMenu = playerInv;
		
		// Actual Slots
		for (int row = 0; row < 6; row++) {
			int y = 18 + row * 18;
			for (int col = 0; col < 9; col++) {
				int x = 26 + col * 18;
				
				this.addSlot(new ExtendedInventorySlot(ExtendedInventory.PAGE_CONTAINER, col + (row * 9), x, y));
			}
		}
		
		// Creative Slots
		for (int i = 0; i < playerInv.slots.size(); i++) {
			if (i < 9 || i >= 45) continue;
			
			int j = i - 9;
			
			int x = 26 + (j % 9) * 18;
			int y = i < 36 ? (140 + (j / 9) * 18) : 198;
			
			this.addSlot(new WrappedSlot(playerInv.slots.get(i), i, x, y));
		}
		this.addSlot(destroyItemSlot = new Slot(ExtendedInventory.VOID_CONTAINER, 0, 190, 198));
	}
	
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem() && slot != this.destroyItemSlot) {
			ItemStack target = slot.getItem();
			stack = target.copy();
			if (index < 54) {
				if (!this.moveItemStackTo(target, 54, this.slots.size() - 1, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(target, 0, 54, false)) {
				return ItemStack.EMPTY;
			}
			
			if (target.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		
		return stack;
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	@Override
	public ItemStack getCarried() {
		return this.inventoryMenu.getCarried();
	}
	
	@Override
	public void setCarried(ItemStack stack) {
		this.inventoryMenu.setCarried(stack);
	}
	
	@Override
	public boolean canDragTo(Slot slot) {
		return slot != this.destroyItemSlot && (!(slot instanceof ExtendedInventorySlot es) || es.exContainer.canModify());
	}
	
	@Override
	public void setItem(int slotId, int stateId, ItemStack stack) {
		if (slotId >= 9 && slotId < 45) {
			this.inventoryMenu.setItem(slotId, stateId, stack);
//			this.inventoryMenu.broadcastChanges();
		}
	}
	
	/** Basically just {@linkplain net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper CreativeModeInventoryScreen$SlotWrapper} */
	protected static class WrappedSlot extends Slot {
		
		public final Slot target;
		
		public WrappedSlot(Slot target, int slot, int x, int y) {
			super(target.container, slot, x, y);
			this.target = target;
		}
		
		@Override
		public void onTake(Player player, ItemStack stack) {
			this.target.onTake(player, stack);
		}
		
		@Override
		public boolean mayPlace(ItemStack stack) {
			return this.target.mayPlace(stack);
		}
		
		@Override
		public ItemStack getItem() {
			return this.target.getItem();
		}
		
		@Override
		public boolean hasItem() {
			return this.target.hasItem();
		}
		
		@Override
		public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
			this.target.setByPlayer(newStack, oldStack);
		}
		
		@Override
		public void set(ItemStack stack) {
			this.target.set(stack);
		}
		
		@Override
		public void setChanged() {
			this.target.setChanged();
		}
		
		@Override
		public int getMaxStackSize(ItemStack stack) {
			return this.target.getMaxStackSize(stack);
		}
		
		@Override
		public ResourceLocation getNoItemIcon() {
			return super.getNoItemIcon();
		}
		
//		@Nullable
//		@Override
//		public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
//			return this.target.getNoItemIcon();
//		}
		
		@Override
		public ItemStack remove(int amount) {
			return this.target.remove(amount);
		}
		
		@Override
		public boolean isActive() {
			return this.target.isActive();
		}
		
		@Override
		public boolean mayPickup(Player player) {
			return this.target.mayPickup(player);
		}
		
		@Override
		public boolean allowModification(Player player) {
			return this.target.allowModification(player);
		}
		
	}
	
	public static class ExtendedInventorySlot extends Slot {
		
		public final ExtendedInventoryContainer exContainer;
		
		public ExtendedInventorySlot(ExtendedInventoryContainer container, int slot, int x, int y) {
			super(container, slot, x, y);
			this.exContainer = container;
		}
		
		@Override
		public boolean mayPickup(Player player) {
			return this.exContainer.isValid() && (this.getItem().isEmpty() || this.getItem().isItemEnabled(player.level().enabledFeatures()));
		}
		
		@Override
		public boolean mayPlace(ItemStack stack) {
			return this.exContainer.isValid();
		}
		
	}
	
}
