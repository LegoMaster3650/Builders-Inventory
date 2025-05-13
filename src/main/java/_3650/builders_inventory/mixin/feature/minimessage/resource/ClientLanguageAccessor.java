package _3650.builders_inventory.mixin.feature.minimessage.resource;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.resources.language.ClientLanguage;

@Mixin(ClientLanguage.class)
public interface ClientLanguageAccessor {
	
	@Accessor("storage")
	public Map<String, String> getStorage();
	
}
