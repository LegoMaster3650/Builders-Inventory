package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Gui.class, priority = Integer.MAX_VALUE - 2)
public abstract class GuiMixin {
	
	@Shadow
	@Final
	private Minecraft minecraft;
	
	/*
	 * Hotbar Render
	 */
	
	// Hotbars
	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 0, shift = At.Shift.AFTER))
	private void builders_inventory_hotbarswapper_renderHotbars(GuiGraphics gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.renderHotbars(gui, minecraft);
	}
	
	// Hotbar Selector
	@WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1))
	private void builders_inventory_hotbarswapper_renderHotbarSelector(GuiGraphics gui, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> operation) {
		if (!HotbarSwapper.renderHotbarSelector(gui, minecraft, sprite)) operation.call(gui, sprite, x, y, width, height);
	}
	
	// Items
	@Inject(
		method = "renderItemHotbar",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"),
		slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 0))
	)
	private void builders_inventory_hotbarswapper_renderItems(GuiGraphics gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.renderItems(gui, deltaTick, minecraft, (GuiInvoker) this);
	}
	
	// Line Labels
	@Inject(method = "renderItemHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;minecraft:Lnet/minecraft/client/Minecraft;", ordinal = 0, shift = At.Shift.BEFORE))
	private void builders_inventory_hotbarswapper_renderLabels(GuiGraphics gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.renderLabels(gui, minecraft);
	}
	
	// Item Name Override
	@ModifyVariable(method = "tick()V", at = @At(value = "STORE", ordinal = 0))
	private ItemStack builders_inventory_hotbarswapper_toolHighlightOverride(ItemStack stack) {
		return HotbarSwapper.toolHighlightOverride(stack, minecraft, (GuiInvoker) this);
	}
	
	/*
	 * HUD Shifting
	 */
	
	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderPlayerHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void builders_inventory_hotbarswapper_shiftHealthBars(Gui hud, GuiGraphics gui, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void builders_inventory_hotbarswapper_shiftVehicleHealth(Gui hud, GuiGraphics gui, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderJumpMeter(Lnet/minecraft/world/entity/PlayerRideableJumping;Lnet/minecraft/client/gui/GuiGraphics;I)V"))
	private void builders_inventory_hotbarswapper_shiftJumpMeter(Gui hud, PlayerRideableJumping rideable, GuiGraphics gui, int x, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, rideable, gui, x);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderExperienceBar(Lnet/minecraft/client/gui/GuiGraphics;I)V"))
	private void builders_inventory_hotbarswapper_shiftExperienceBar(Gui hud, GuiGraphics gui, int x, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui, x);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void builders_inventory_hotbarswapper_shiftSelectedItemName(Gui hud, GuiGraphics gui, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	// Action bar [ can't wrap :( ]
	@Mixin(value = Gui.class, priority = Integer.MIN_VALUE + 2)
	public static abstract class GuiMixinLow {
		
		@Inject(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0))
		private void builders_inventory_hotbarswapper_shiftActionBar(GuiGraphics gui, DeltaTracker deltaTick, CallbackInfo ci) {
			HotbarSwapper.shiftHud(gui);
		}
		
	}
	
	@Inject(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 0, shift = At.Shift.AFTER))
	private void builders_inventory_hotbarswapper_shiftActionBarReset(GuiGraphics gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.shiftHudReset(gui);
	}
	
}
