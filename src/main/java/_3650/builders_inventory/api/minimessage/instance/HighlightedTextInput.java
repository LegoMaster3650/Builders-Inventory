package _3650.builders_inventory.api.minimessage.instance;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class HighlightedTextInput {
	
	public final String text;
	private final StyleData[] styles;
	
	public HighlightedTextInput(String text, StyleData[] styles) {
		this.text = text;
		this.styles = styles;
	}
	
	private final Long2ObjectOpenHashMap<FormattedCharSequence> cache = new Long2ObjectOpenHashMap<>();
	
	public FormattedCharSequence subseq(int start, int end) {
		long sig = (start & 0xffffffffL) | (((long)end) << 32);
		if (cache.containsKey(sig)) return cache.get(sig);
		ImmutableList.Builder<FormattedCharSequence> seq = ImmutableList.builder();
		while (start < end && start < text.length()) {
			final StyleData data = styles[start];
			// data.start + data.length = start + data.length - start + data.start = start + data.length - (start - data.start)
			final int dif = Math.min(data.start + data.length, end);
			seq.add(FormattedCharSequence.forward(text.substring(start, dif), data.style));
			start = dif;
		}
		final FormattedCharSequence result = FormattedCharSequence.composite(seq.build());
		cache.put(sig, result);
		return result;
	}
	
	public static class Builder {
		
		private final StringBuilder text;
		private final StyleData[] styles;
		public int length;
		
		public Builder(int size) {
			this.text = new StringBuilder(size);
			this.styles = new StyleData[size];
		}
		
		public void append(String str, Style style) {
			final int len = str.length();
			final int max = length + len;
			text.append(str);
			final StyleData data = new StyleData(len, length, style);
			for (int i = length; i < max; i++) styles[i] = data;
			length = max;
		}
		
		public HighlightedTextInput build() {
			return new HighlightedTextInput(text.toString(), styles);
		}
		
	}
	
	static class StyleData {
		
		public final int length;
		public final int start;
		public final Style style;
		
		public StyleData(int length, int start, Style style) {
			this.length = length;
			this.start = start;
			this.style = style;
		}
		
	}
}
