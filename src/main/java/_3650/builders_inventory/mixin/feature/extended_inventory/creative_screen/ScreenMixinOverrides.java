package _3650.builders_inventory.mixin.feature.extended_inventory.creative_screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
abstract class ScreenMixinOverrides {
	
	@Shadow
	protected Minecraft minecraft;
	@Shadow
	public int width;
	@Shadow
	public int height;
	@Shadow
	protected Font font;
	
	@Shadow
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);
	
	@Inject(method = "clearWidgets", at = @At("TAIL"))
	protected void clearWidgetsInjectTail(CallbackInfo ci) {
		
	}
	
}
