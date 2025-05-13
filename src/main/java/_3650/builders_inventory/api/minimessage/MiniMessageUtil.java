package _3650.builders_inventory.api.minimessage;

import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance;
import _3650.builders_inventory.mixin.feature.minimessage.ChatComponentInvoker;
import _3650.builders_inventory.mixin.feature.minimessage.EditBoxAccessor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;

public class MiniMessageUtil {
	
	public static final int lerpColor(float phase, int a, int b) {
		final float lphase = Math.min(1f, Math.max(0f, phase));
		final int ar = (a >> 16) & 0xFF;
		final int br = (b >> 16) & 0xFF;
		final int ag = (a >> 8) & 0xFF;
		final int bg = (b >> 8) & 0xFF;
		final int ab = (a >> 0) & 0xFF;
		final int bb = (b >> 0) & 0xFF;
		return
				((Math.round(ar + lphase * (br - ar)) & 0xFF) << 16) |
				((Math.round(ag + lphase * (bg - ag)) & 0xFF) << 8) |
				((Math.round(ab + lphase * (bb - ab)) & 0xFF) << 0);
	}
	
	public static int getLineHeight(ChatComponent chat) {
		return ((ChatComponentInvoker)chat).callGetLineHeight();
	}
	
	public static void wrapFormatter(EditBox input, MiniMessageInstance widget) {
		final var original = ((EditBoxAccessor)input).getFormatter();
		input.setFormatter((text, offset) -> {
			if (widget.canFormat()) return widget.format(offset, offset + text.length());
			else return original.apply(text, offset);
		});
	}
	
}
