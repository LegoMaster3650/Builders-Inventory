package _3650.builders_inventory.feature.minimessage.chat;

import java.util.ArrayList;
import java.util.List;

// This is kept package-private on purpose, the outside world should not be shown the horrors within here
class ChatDiff {
	
	/**
	 * adapted version of the former StringDiff specialized in chat/command diffs<br>
	 * the old solution won't be missed<br>
	 * still the 3rd worst code I've ever written<br>
	 * <br>
	 * scans from end to start<br>
	 * note to self: because it scans from end to start, the swaps list is in REVERSE ORDER<br>
	 * note to non-self: yes, that note is there for the exact reason you think
	 */
	static ChatDiffResult calculate(String original, String modified) {
		// empty string shortcuts
		if (modified.isEmpty()) {
			return original.isEmpty() ? ChatDiffResult.EMPTY : new ChatDiffResult(original, List.of(), "");
		}
		
		// set up character counters
		int origInd = original.length() - 1;
		char origC = original.charAt(origInd);
		int modiInd = modified.length() - 1;
		char modiC = modified.charAt(modiInd);
		
		// 1. get trailing end spaces, defined in String.trim() as any character <= space
		while (origInd > 0) {
			if (origC <= ' ') {
				origC = original.charAt(--origInd);
			} else break;
		}
		if (origInd == 0) {
			// if it goes all the way to 0, the first character MUST be a non-space or modified would be empty
			return new ChatDiffResult("", List.of(), original.substring(1));
		}
		final String trailing = original.substring(origInd + 1);
		
		// 2. find truncated whitespace and compare strings until modified is fully matched
		final List<ChatSwap> swaps = new ArrayList<>();
		int swapTail = -1;
		
		while (origInd > 0) {
			if (Character.isWhitespace(origC)) {
				if (swapTail == -1) swapTail = origInd + 1;
				if (modiC == ' ') {
					if (--modiInd >= 0) modiC = modified.charAt(modiInd);
					else break;
				}
			} else {
				if (swapTail > -1) {
					swaps.add(new ChatSwap(modiInd + 1, original.substring(origInd + 1, swapTail)));
					swapTail = -1;
				}
				if (origC == modiC) {
					if (--modiInd >= 0) modiC = modified.charAt(modiInd);
					else break;
				}
			}
			
			origC = original.charAt(--origInd);
		}
		if (swapTail > -1) {
			if (origInd == 0) {
				swaps.add(new ChatSwap(modiInd + 1, original.substring(1, swapTail)));
				++origInd;
			} else {
				while (origInd > 0) {
					origC = original.charAt(--origInd);
					if (!Character.isWhitespace(origC)) {
						swaps.add(new ChatSwap(0, original.substring(origInd + 1, swapTail)));
						++origInd;
						break;
					}
				}
			}
		}
		
		// 3. save remainder of original as prefix
		final String prefix = original.substring(0, origInd);
		
		// return results
		return new ChatDiffResult(prefix, swaps, trailing);
	}
	
	static class ChatSwap {
		public final int index;
		public final String value;
		
		public ChatSwap(int index, String value) {
			this.index = index;
			this.value = value;
		}
	}
	
	static class ChatDiffResult {
		public static final ChatDiffResult EMPTY = new ChatDiffResult("", List.of(), "");
		
		public final String prefix;
		public final List<ChatSwap> swaps;
		public final String trailing;
		
		private ChatDiffResult(String prefix, List<ChatSwap> swaps, String trailing) {
			this.prefix = prefix;
			this.swaps = swaps;
			this.trailing = trailing;
		}
	}
	
}
