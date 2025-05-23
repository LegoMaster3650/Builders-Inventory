package _3650.builders_inventory.mixin.feature.minimessage.screens;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public abstract class ScreenMixinOverrides extends AbstractContainerEventHandler {
	
	@Shadow
	protected Minecraft minecraft;
	@Shadow
	public int height;
	@Shadow
	protected Font font;
	
	@Shadow
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);
	
	@Inject(method = "clearWidgets", at = @At("TAIL"))
	protected void clearWidgetsInjectTail(CallbackInfo ci) {
		
	}
	
	@Inject(method = "tick", at = @At("TAIL"))
	protected void tickInjectTail(CallbackInfo ci) {
		
	}
	
}
