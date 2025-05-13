package _3650.builders_inventory.api.minimessage.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator;

import _3650.builders_inventory.api.minimessage.MiniMessageUtil;
import _3650.builders_inventory.api.minimessage.color.UnsetColorStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

public class GradientFormat extends Format {
	
	public final List<TextColor> colors;
	public final float phase;
	public final int size;
	public final int realsize;
	
	private float increment;
	private float ind;
	private boolean overflow;
	
	private int indMin;
	private int indMax;
	private int colMin;
	private int colMax;
	
	public GradientFormat(String argString, String tag, List<TextColor> colors, double phase) {
		super(argString, tag);
		if (colors.isEmpty()) colors = List.of(TextColor.fromRgb(0xFFFFFF), TextColor.fromRgb(0x000000));
		if (phase < 0) {
			this.colors = new ArrayList<>(colors.size());
			this.phase = 1 + (float)phase;
			for (int i = colors.size() - 1; i >= 0; --i) {
				this.colors.add(colors.get(i));
			}
		} else {
			this.colors = colors;
			this.phase = (float)phase;
		}
		this.size = colors.size() - 1;
		this.realsize = colors.size();
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		int length = component.getString().length() - 1;
		
		increment = (1f / length) * size;
		ind = 0;
		overflow = false;
		
		indMin = -1;
		indMax = -1;
		colMin = 0;
		colMax = 0;
		
		updateAndColor(this.phase * size);
		
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
					final int color = updateAndColor(ind + increment);
					result.append(Component.literal(new String(holder, 0, 1))
							.withStyle(style.withColor(color)));
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
				updateAndColor(ind + (increment * content.getString().length()));
			}
		}
		
		return result;
	}
	
	private int updateAndColor(float ind) {
		final float prevInd = this.ind;
		final int col = MiniMessageUtil.lerpColor(prevInd - indMin, colMin, colMax);
		if (ind > realsize) {
			ind -= realsize;
			overflow = false;
		}
		this.ind = ind;
		if (!overflow && (ind > indMax || ind < indMin)) {
			if (ind > size) {
				indMin = size;
				indMax = 0;
				overflow = true;
			} else {
				indMin = Mth.floor(ind);
				indMax = Mth.ceil(ind);
			}
//			System.out.println("DEBUG " + prevInd + "-" + ind + " TO " + indMin + "-" + indMax);
			colMin = colors.get(indMin).getValue();
			colMax = colors.get(indMax).getValue();
		}
		
		return col;
	}
	
}
