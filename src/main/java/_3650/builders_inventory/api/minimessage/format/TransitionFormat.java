package _3650.builders_inventory.api.minimessage.format;

import java.util.List;

import _3650.builders_inventory.api.minimessage.MiniMessageUtil;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

public class TransitionFormat extends Format {
	
	public final int color;
	
	public TransitionFormat(String argString, String tag, List<TextColor> colors, double phase) {
		super(argString, tag);
		if (colors.isEmpty()) colors = List.of(TextColor.fromRgb(0xFFFFFF), TextColor.fromRgb(0x000000));
		this.color = color(colors, (float)phase);
	}
	
	private int color(List<TextColor> colors, float phaseIn) {
		final float phase = (phaseIn < 0 ? -phaseIn : phaseIn) * (colors.size() - 1);
		final int indMin = Mth.floor(phase);
		final int indMax = Mth.ceil(phase);
		if (indMin == indMax) return colors.get(indMin).getValue();
		return MiniMessageUtil.lerpColor(phase - indMin, colors.get(indMin).getValue(), colors.get(indMax).getValue());
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.withColor(this.color);
	}
	
}
