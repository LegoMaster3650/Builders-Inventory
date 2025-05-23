package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerMixin {
	
	@Shadow
	private Map<String, LanguageInfo> languages;
	@Shadow
	private String currentCode;
	
	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void builders_inventory_captureLanguages(ResourceManager resourceManager, CallbackInfo ci, @Local(ordinal = 0) ClientLanguage lang) {
		ArrayList<String> langKeys = ((ClientLanguageAccessor)lang).getStorage().keySet().stream()
				.sorted()
				.filter(tag -> !tag.startsWith("_")) // I consider _ to be a comment
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		ReloadableResourceArg.LANG.loadStr(langKeys);
	}
	
}
