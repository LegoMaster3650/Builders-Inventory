package _3650.builders_inventory.mixin.feature.minimessage;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

@Mixin(Style.class)
public interface StyleInvoker {
	
	@Invoker("<init>")
	public static Style construct(
			@Nullable TextColor color,
			@Nullable Boolean bold,
			@Nullable Boolean italic,
			@Nullable Boolean underlined,
			@Nullable Boolean strikethrough,
			@Nullable Boolean obfuscated,
			@Nullable ClickEvent clickEvent,
			@Nullable HoverEvent hoverEvent,
			@Nullable String insertion,
			@Nullable ResourceLocation font
	) {
		// dummy method body
		return null;
	}
	
}
