package _3650.builders_inventory.mixin.feature.extended_inventory;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.authlib.GameProfile;

import _3650.builders_inventory.feature.extended_inventory.ExtendedInventoryMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}
	
	@Shadow
	@Final
	protected Minecraft minecraft;
	
	@Shadow
	public abstract void clientSideCloseContainer();
	
	@Inject(method = "closeContainer", at = @At("HEAD"), cancellable = true)
	private void builders_inventory_extendedinventory_stfu(CallbackInfo ci) {
		if (this.containerMenu instanceof ExtendedInventoryMenu) {
			this.clientSideCloseContainer();
			ci.cancel();
		}
	}
	
}
