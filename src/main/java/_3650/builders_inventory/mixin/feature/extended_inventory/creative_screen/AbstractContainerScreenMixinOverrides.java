package _3650.builders_inventory.mixin.feature.extended_inventory.creative_screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixinOverrides extends ScreenMixinOverrides {
	
	@Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
	protected void extractTooltipInjectHead(GuiGraphicsExtractor guiGraphics, int x, int y, CallbackInfo ci) {
		
	}
	
}
