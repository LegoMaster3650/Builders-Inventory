package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import _3650.builders_inventory.api.minimessage.autocomplete.ReloadableResourceArg;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Mixin(FontManager.class)
public abstract class FontManagerMixin {
	
	@Shadow
	@Final
	private Map<ResourceLocation, FontSet> fontSets;
	
	@Inject(method = "apply", at = @At("TAIL"))
	private void builders_inventory_captureFonts(CallbackInfo ci) {
		ArrayList<ResourceLocation> fonts = fontSets.keySet().stream()
				.filter(s -> !s.getPath().startsWith("include/"))
				.sorted()
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		ReloadableResourceArg.FONTS.loadLoc(fonts);
	}
	
}
