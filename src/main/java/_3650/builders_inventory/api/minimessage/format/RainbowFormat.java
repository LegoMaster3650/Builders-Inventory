package _3650.builders_inventory.api.minimessage.format;

import java.awt.Color;
import java.util.Optional;
import java.util.PrimitiveIterator;

import _3650.builders_inventory.api.minimessage.color.UnsetColorStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class RainbowFormat extends Format {
	
	public final boolean invert;
	public final double phase;
	
	private float increment;
	private float hue;
	
	public RainbowFormat(String argString, String tag, boolean invert, int phase) {
		super(argString, tag);
		this.invert = invert;
		this.phase = phase / 10.0;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		int length = component.getString().length();
		increment = (invert ? -1f : 1f) / length;
		hue = invert ? increment + (float)(phase) : (float)phase;
		if (hue < 0) hue += 1f;
		return component.getStyle().getColor() == null ? formatNode(component) : component;
	}
	
	private MutableComponent formatNode(Component component) {
		var contents = component.toFlatList();
		var result = Component.empty();
		
		for (var content : contents) {
			if (content.getStyle().getColor() == null) {
				var style = new UnsetColorStyle(content.getStyle());
				final StringBuilder text = new StringBuilder();
				content.getContents().visit((str) -> {
					text.append(str);
					return Optional.empty();
				});
				// some of this adapted from adventure's minimessage code
				final int[] holder = new int[1];
				for (final PrimitiveIterator.OfInt it = text.codePoints().iterator(); it.hasNext();) {
					holder[0] = it.nextInt();
					result.append(Component.literal(new String(holder, 0, 1))
							.withStyle(style.withColor(Color.HSBtoRGB(hue, 1f, 1f))));
					hue = (hue + increment) % 1f;
					if (hue < 0) hue += 1f;
				}
				// this part's pretty original though
				if (!content.getSiblings().isEmpty()) {
					var siblings = Component.empty().withStyle(content.getStyle());
					for (var sibling : content.getSiblings()) {
						siblings.append(formatNode(sibling));
					}
					result.append(siblings);
				}
			} else {
				result.append(content);
				hue = (hue + (increment * content.getString().length())) % 1f;
				if (hue < 0) hue += 1f;
			}
		}
		
		return result;
	}
	
}
