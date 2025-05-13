package _3650.builders_inventory.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Weird evil hack I made that can tell what is missing from a modified string
 */
public class StringDiff {
	
	public final int index;
	public final String value;
	
	private StringDiff(int index, String value) {
		this.index = index;
		this.value = value;
	}
	
	/**
	 * Only use when checking what's missing from a string with parts removed
	 */
	public static _SDMissingResult missing(String original, String reduced) {
		if (reduced.isEmpty()) {
			return original.isEmpty() ? _SDMissingResult.EMPTY : new _SDMissingResult(List.of(new StringDiff(0, original)), original.length());
		}
		
		ArrayList<StringDiff> diffs = new ArrayList<>();
		int diffLen = 0;
		
		char[] builder = new char[original.length()];
		int bcount = 0;
		
		int orig = 0;
		final int origS = original.length();
		int redu = 0;
		final int reduS = reduced.length();
		char reduC = 0;
		
		boolean matches = true;
		
		while (orig < origS) {
			char origC = original.charAt(orig++);
			if (matches && redu < reduS) reduC = reduced.charAt(redu++);
			
			if (origC != reduC) {
				builder[bcount++] = origC;
				matches = false;
			} else if (!matches) {
				diffs.add(new StringDiff(redu - 1, new String(builder, 0, bcount)));
				diffLen += bcount;
				bcount = 0;
				matches = true;
				if (redu >= reduS) reduC = 0;
			}
		}
		if (bcount > 0) {
			diffs.add(new StringDiff(reduS, new String(builder, 0, bcount)));
			diffLen += bcount;
		}
		//if (bcount > 0) diffs.add(new StringDiff(reduS - 1, new String(builder, 0, bcount)));
//		if (orig + 1 < origS) diffs.add(new StringDiff(reduS - 1, original.substring(orig + 1)));
		return new _SDMissingResult(diffs, diffLen);
	}
	
	public static class _SDMissingResult {
		
		private static final _SDMissingResult EMPTY = new _SDMissingResult(List.of(), 0);
		
		public final List<StringDiff> diffs;
		public final int length;
		
		private _SDMissingResult(List<StringDiff> diffs, int diffLen) {
			this.diffs = diffs;
			this.length = diffLen;
		}
		
	}
	
}
