package _3650.builders_inventory.feature.minimessage.chat;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum ChatMiniMessageButtonDisplay implements NameableEnum {
	// note to self: calculations are order-independent and output of one will NOT affect input of another
	NONE(width -> width, x -> x, (x, width) -> -3650), // this one should never be applied anyways
	LEFT(width -> width - 10, x -> x + 10, (x, width) -> x - 1),
	RIGHT(width -> width - 14, x -> x, (x, width) -> x + width - 13),
	;
	private final IntUnaryOperator chatWidth;
	private final IntUnaryOperator chatX;
	private final IntBinaryOperator buttonX;
	private ChatMiniMessageButtonDisplay(
			IntUnaryOperator chatWidth,
			IntUnaryOperator chatX,
			IntBinaryOperator buttonX
			) {
		this.chatWidth = chatWidth;
		this.chatX = chatX;
		this.buttonX = buttonX;
	}
	public int chatWidth(int width) {
		return this.chatWidth.applyAsInt(width);
	}
	public int chatX(int x) {
		return this.chatX.applyAsInt(x);
	}
	public int buttonX(int x, int width) {
		return this.buttonX.applyAsInt(x, width);
	}
	
	public final String key = "enum.builders_inventory.minimessage.button_position." + this.name().toLowerCase();
	@Override
	public Component getDisplayName() {
		return Component.translatable(key);
	}
	
}
