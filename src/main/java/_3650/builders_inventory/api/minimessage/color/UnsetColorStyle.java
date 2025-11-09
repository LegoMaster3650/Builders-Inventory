package _3650.builders_inventory.api.minimessage.color;

import _3650.builders_inventory.mixin.feature.minimessage.StyleInvoker;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class UnsetColorStyle {
	
	private final Integer shadowColor;
	private final Boolean bold;
	private final Boolean italic;
	private final Boolean underlined;
	private final Boolean strikethrough;
	private final Boolean obfuscated;
	private final ClickEvent clickEvent;
	private final HoverEvent hoverEvent;
	private final String insertion;
	private final FontDescription font;
	
	public UnsetColorStyle(Style style) {
		this.shadowColor = style.getShadowColor();
		this.bold = style.isBold();
		this.italic = style.isItalic();
		this.underlined = style.isUnderlined();
		this.strikethrough = style.isStrikethrough();
		this.obfuscated = style.isObfuscated();
		this.clickEvent = style.getClickEvent();
		this.hoverEvent = style.getHoverEvent();
		this.insertion = style.getInsertion();
		this.font = style.getFont();
	}
	
	public Style withColor(int color) {
		Style style = StyleInvoker.construct(TextColor.fromRgb(color), this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, hoverEvent, insertion, font);
		return style.equals(Style.EMPTY) ? Style.EMPTY : style;
	}
	
}
