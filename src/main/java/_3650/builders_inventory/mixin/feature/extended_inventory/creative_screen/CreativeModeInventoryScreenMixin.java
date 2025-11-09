package _3650.builders_inventory.mixin.feature.extended_inventory.creative_screen;

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
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButtonGui;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.extended_inventory.ExtendedInventory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.CreativeModeTab;

@Debug(export = true)
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreenMixinOverrides {
	
	@Shadow
	private static CreativeModeTab selectedTab;
	
	@Unique
	private ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	@Unique
	private ExtendedImageButton creativeButton;
	
	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;init()V"))
	private void builders_inventory_extendedinventory_addCreativeButton(CallbackInfo ci) {
		if (!Config.instance().extended_inventory_open_button_enabled) return;
		this.exGui.init();
		this.creativeButton = ExtendedInventory.createOpenButton(this.width / 2, this.height / 2);
		this.creativeButton.visible = ExtendedInventory.isOpenButtonVisible(selectedTab);
		this.addRenderableWidget(creativeButton);
		this.exGui.addRenderableWidget(creativeButton);
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
		if (this.creativeButton != null)
			this.creativeButton.visible = ExtendedInventory.isOpenButtonVisible(tab);
	}
	
	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", ordinal = 0), cancellable = true)
	private void builders_inventory_extendedinventory_keybind(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (ModKeybinds.OPEN_EXTENDED_INVENTORY.matches(event)) {
			ExtendedInventory.open(minecraft);
			cir.setReturnValue(true);
		}
	}
	
	@Override
	protected void renderTooltipInjectHead(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
		if (!Config.instance().extended_inventory_open_button_enabled) return;
		if (this.exGui.renderTooltip(this.font, guiGraphics, x, y)) ci.cancel();
	}
	
	@Override
	protected void clearWidgetsInjectTail(CallbackInfo ci) {
		if (!Config.instance().extended_inventory_open_button_enabled) return;
		this.exGui.clearWidgets();
	}
	
}
