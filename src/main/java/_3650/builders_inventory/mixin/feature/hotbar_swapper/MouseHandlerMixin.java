package _3650.builders_inventory.mixin.feature.hotbar_swapper;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import _3650.builders_inventory.feature.hotbar_swapper.HotbarSwapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	
	@Shadow
	@Final
	private Minecraft minecraft;
	
	@Inject(method = "onScroll", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
	private void builders_inventory_hotbarswapper_mouseScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (HotbarSwapper.mouseScroll(yoffset * minecraft.options.mouseWheelSensitivity().get())) ci.cancel();
	}
	
}
