package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Hud.class, priority = Integer.MAX_VALUE - 2)
public abstract class HudMixin {
	
	@Shadow
	@Final
	private Minecraft minecraft;
	
	/*
	 * Hotbar Render
	 */
	
	// Hotbars
	@Inject(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0, shift = At.Shift.AFTER))
	private void builders_inventory_hotbarswapper_extractHotbars(GuiGraphicsExtractor gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.renderHotbars(gui, minecraft);
	}
	
	// Hotbar Selector
	@WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
	private void builders_inventory_hotbarswapper_extractHotbarSelector(GuiGraphicsExtractor gui, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> operation) {
		if (!HotbarSwapper.renderHotbarSelector(gui, minecraft, sprite)) operation.call(gui, pipeline, sprite, x, y, width, height);
	}
	
	// Items
	@Inject(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
	private void builders_inventory_hotbarswapper_extractItems(GuiGraphicsExtractor gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.renderItems(gui, deltaTick, minecraft, (HudInvoker) this);
	}
	
	// Line Labels
	@Inject(method = "extractItemHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Hud;minecraft:Lnet/minecraft/client/Minecraft;", ordinal = 0, shift = At.Shift.BEFORE))
	private void builders_inventory_hotbarswapper_extractLabels(GuiGraphicsExtractor gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.renderLabels(gui, minecraft);
	}
	
	// Item Name Override
	@ModifyVariable(method = "tick()V", at = @At(value = "STORE", ordinal = 0))
	private ItemStack builders_inventory_hotbarswapper_toolHighlightOverride(ItemStack stack) {
		return HotbarSwapper.toolHighlightOverride(stack, minecraft, (HudInvoker) this);
	}
	
	/*
	 * HUD Shifting
	 */
	
	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractPlayerHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void builders_inventory_hotbarswapper_shiftHealthBars(Hud hud, GuiGraphicsExtractor gui, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractVehicleHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void builders_inventory_hotbarswapper_shiftVehicleHealth(Hud hud, GuiGraphicsExtractor gui, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void builders_inventory_hotbarswapper_shiftInfoBarBg(ContextualBar bar, GuiGraphicsExtractor gui, DeltaTracker deltaTick, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(bar, gui, deltaTick);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
	private void builders_inventory_hotbarswapper_shiftExperienceLevel(GuiGraphicsExtractor gui, Font font, int level, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(gui, font, level);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void builders_inventory_hotbarswapper_shiftInfoBar(ContextualBar bar, GuiGraphicsExtractor gui, DeltaTracker deltaTick, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(bar, gui, deltaTick);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void builders_inventory_hotbarswapper_shiftSelectedItemName(Hud hud, GuiGraphicsExtractor gui, Operation<Void> operation) {
		HotbarSwapper.shiftHud(gui);
		operation.call(hud, gui);
		HotbarSwapper.shiftHudReset(gui);
	}
	
	// Action bar [ can't wrap :( ]
	@Mixin(value = Hud.class, priority = Integer.MIN_VALUE + 2)
	public static abstract class HudMixinLow {
		
		@Inject(method = "extractOverlayMessage", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;", ordinal = 0))
		private void builders_inventory_hotbarswapper_shiftActionBar(GuiGraphicsExtractor gui, DeltaTracker deltaTick, CallbackInfo ci) {
			HotbarSwapper.shiftHud(gui);
		}
		
	}
	
	@Inject(method = "extractOverlayMessage", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;", ordinal = 0, shift = At.Shift.AFTER))
	private void builders_inventory_hotbarswapper_shiftActionBarReset(GuiGraphicsExtractor gui, DeltaTracker deltaTick, CallbackInfo ci) {
		HotbarSwapper.shiftHudReset(gui);
	}
	
}
