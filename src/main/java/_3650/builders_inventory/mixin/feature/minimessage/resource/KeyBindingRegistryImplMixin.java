package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.KeyMapping;

@Mixin(KeyBindingRegistryImpl.class)
public abstract class KeyBindingRegistryImplMixin {
	
	@Inject(method = "process", at = @At("HEAD"))
	private static void builders_inventory_captureVanillaKeybinds(KeyMapping[] keyMappings, CallbackInfoReturnable<KeyMapping[]> cir) {
		if (ReloadableResourceArg.KEYS.isLoaded()) return;
		ArrayList<String> keys = Arrays.stream(keyMappings)
				.map(key -> key.getName())
				.sorted()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		ReloadableResourceArg.KEYS.loadStr(keys);
	}
	
}
