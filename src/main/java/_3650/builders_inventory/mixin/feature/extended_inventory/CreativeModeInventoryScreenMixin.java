package _3650.builders_inventory.mixin.feature.extended_inventory;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import _3650.builders_inventory.ModKeybinds;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButton;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;

@Debug(export = true)
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
	
	public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
	
	@Shadow
	private static CreativeModeTab selectedTab;
	
	@Unique
	private ExtendedImageButton builders_inventory_extendedinventory_creativeButton;
	
	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;init()V"))
	private void builders_inventory_extendedinventory_addCreativeButton(CallbackInfo ci) {
		if (!Config.instance().extended_inventory_open_button_enabled) return;
		builders_inventory_extendedinventory_creativeButton = ExtendedInventory.createOpenButton(this.width / 2, this.height / 2);
		builders_inventory_extendedinventory_creativeButton.visible = ExtendedInventory.isOpenButtonVisible(selectedTab);
		this.addRenderableWidget(builders_inventory_extendedinventory_creativeButton);
	}
	
	@Inject(
		method = "selectTab",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;clear()V"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/CreativeModeTab$Type;INVENTORY:Lnet/minecraft/world/item/CreativeModeTab$Type;", opcode = Opcodes.GETSTATIC, ordinal = 0),
			to   = @At(value = "FIELD", target = "Lnet/minecraft/world/item/CreativeModeTab$Type;SEARCH:Lnet/minecraft/world/item/CreativeModeTab$Type;", opcode = Opcodes.GETSTATIC, ordinal = 0)
		)
	)
	private void builders_inventory_extendedinventory_creativeButtonVisibility(CreativeModeTab tab, CallbackInfo ci) {
		if (builders_inventory_extendedinventory_creativeButton != null)
			builders_inventory_extendedinventory_creativeButton.visible = ExtendedInventory.isOpenButtonVisible(tab);
	}
	
	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;keyPressed(III)Z", ordinal = 0), cancellable = true)
	private void builders_inventory_extendedinventory_keybind(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (ModKeybinds.OPEN_EXTENDED_INVENTORY.matches(keyCode, scanCode)) {
			ExtendedInventory.open(minecraft);
			cir.setReturnValue(true);
		}
	}
	
}
