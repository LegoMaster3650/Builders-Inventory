package _3650.builders_inventory.api.minimessage.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.minimessage.MiniMessageParser;
import _3650.builders_inventory.api.minimessage.MiniMessageResult;
import _3650.builders_inventory.api.minimessage.MiniMessageUtil;
import _3650.builders_inventory.api.minimessage.validator.MiniMessageValidator;
import _3650.builders_inventory.api.minimessage.widgets.wrapper.WrappedTextField;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.minimessage.MiniMessageFeature;
import _3650.builders_inventory.feature.minimessage.chat.ChatMiniMessageContext;
import _3650.builders_inventory.util.StringDiff;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

// help me
/**
 * Methods to call when using this:<br>
 * <br>
 * {@link #tick()}<br>
 * {@link #unknownEdit()}<br>
 * {@link #cursorMoved()}<br>
 * {@link #inputEdited()}<br>
 * {@link #quietUpdate()}<br>
 * {@link #renderPreviewOrError(GuiGraphics)}
 * {@link #renderSuggestions(Minecraft, GuiGraphics, Font, int, int)}<br>
 * {@link #keyPressed(int, int, int)}<br>
 * {@link #mouseScrolled(double, double, double, double)}<br>
 * {@link #mouseClicked(double, double, int)}<br>
 * 
 */
public class MiniMessageInstance {
	
	private final Minecraft minecraft;
	private final Screen screen;
	private final Font font;
	public final WrappedTextField input;
	private final MiniMessageValidator context;
	private final LastParseListener listener;
	private final PreviewOptions previewOptions;
	private final SuggestionsDisplay display;
	
	
	public MiniMessageInstance(
			Minecraft minecraft,
			Screen screen,
			Font font,
			WrappedTextField input,
			MiniMessageValidator context,
			LastParseListener listener,
			PreviewOptions previewOptions,
			SuggestionOptions suggestionOptions) {
		this.minecraft = minecraft;
		this.screen = screen;
		this.font = font;
		this.input = input;
		this.context = context;
		this.listener = listener;
		this.previewOptions = previewOptions;
		this.display = new SuggestionsDisplay(suggestionOptions);
		this.reposition();
	}
	
	@Nullable
	public MiniMessageResult lastParse = null;
	@Nullable
	public HighlightedTextInput inputOverride = null;
	@NotNull
	public List<FormattedCharSequence> previewLines = List.of();
	
	@Nullable
	private String lastValue = null;
	private int updateTimer = 0;
	
	private boolean active = true;
	
	public void setActive(boolean active) {
		this.active = active;
		if (!active && updateTimer > 0 && lastValue != null) {
			this.updateTimer = 0;
			if (this.updateMiniMessage(lastValue)) {
				this.display.suggestionOptions.claimSuggestions();
			}
		}
	}
	
	public void tick() {
		if (updateTimer > 0) {
			--updateTimer;
			if (updateTimer <= 0 && lastValue != null && updateMiniMessage(lastValue)) {
				this.display.suggestionOptions.claimSuggestions();
			}
		}
	}
	
	public boolean unknownEdit() {
		if (!active) return false;
		@Nullable
		final String value = input.getValue();
		
		if (Objects.equals(value, lastValue)) { 
			return this.cursorMoved(value);
		}
		
		return this.inputEdited(value);
	}
	
	public boolean cursorMoved() {
		if (!active) return false;
		return this.cursorMoved(input.getValue());
	}
	
	private boolean cursorMoved(String value) {
		// cursor move
		this.cursorMoved(value, input.getCursorPosition());
		
		// return regardless
		return lastParse != null;
	}
	
	public boolean inputEdited() {
		if (!active) return false;
		return this.inputEdited(input.getValue());
	}
	
	private boolean inputEdited(String value) {
		this.lastValue = value;
		
		// update delay option for slow computers just in case cuz this could lag idk
		final int delay = Config.instance().minimessage_updateDelay;
		if (delay > 0) {
			this.updateTimer = delay;
			this.inputOverride = null;
			this.clear();
			return lastParse != null;
		}
		
		if (value != null && updateMiniMessage(value)) {
			this.display.suggestionOptions.claimSuggestions();
			return true;
		}
		
		return false;
	}
	
	public boolean quietUpdate() {
		@Nullable
		final String value = input.getValue();
		
		boolean messageUpdated = false;
		if (value != null) {
			this.suppressSuggestionUpdate = true;
			this.lastValue = value;
			messageUpdated = updateMiniMessage(value);
			this.suppressSuggestionUpdate = false;
		}
		if (messageUpdated) {
			this.display.suggestionOptions.claimSuggestions();
			return true;
		}
		return false;
	}
	
	private static final Style LITERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
	
	private boolean updateMiniMessage(@NotNull String value) {
		final String originalValue = value;
		
		{
			// only work in certain contexts
			final var newVal = this.context.isValid(this.minecraft, value);
//			BuildersInventory.LOGGER.warn(newVal.toString());
			
			// validate value
			if (newVal.isEmpty() || newVal.get().isEmpty()) {
				this.clearParse();
				return false;
			} else value = newVal.get();
		}
		
		// parse message
		final var mini = MiniMessageParser.parse(value, registryAccess(this.minecraft), ChatMiniMessageContext.currentServerIP);
		this.setLastParse(mini);
		
		// find any text lost
		final var missing = StringDiff.missing(originalValue, value);
		final List<StringDiff> diffs = missing.diffs;
		final Iterator<StringDiff> diffIter = diffs.iterator();
		final int origS = originalValue.length();
		
		// get plaintext with fun syntax highlighting
		MutableComponent highSeq = mini.getFormattedPlain();
		
		// input result
		final var input = new HighlightedTextInput.Builder(origS + missing.length);
		
		final var reconstructor = new FormattedText.StyledContentConsumer<Integer>() {
			public int index = 0;
			public StringDiff diff = diffs.isEmpty() ? null : diffIter.next();
			
			@Override
			public Optional<Integer> accept(Style style, String string) {
				if (input.length + string.length() >= origS) {
					final int i = origS - input.length;
					if (i > 0) {
						final String end = string.substring(0, i);
						input.append(end, style);
					}
					return Optional.of(input.length + i);
				}
				if (diff == null) {
					input.append(string, style);
					return Optional.empty();
				}
				
				if (index + string.length() >= diff.index && diff.index >= index) {
					final int i = diff.index - index;
					
					if (i > 0) {
						final String before = string.substring(0, i);
						input.append(before, style);
						index += before.length();
					}
					input.append(diff.value, style);
					
					diff = diffIter.hasNext() ? diffIter.next() : null;
					
					if (i < string.length()) return accept(style, string.substring(i));
				} else {
					input.append(string, style);
					index += string.length();
				}
				return Optional.empty();
			}
		};
		
		// deal with index 0 diffs first
		while (reconstructor.diff != null && reconstructor.diff.index == 0) {
			input.append(reconstructor.diff.value, LITERAL_STYLE);
			reconstructor.diff = diffIter.hasNext() ? diffIter.next() : null;
		}
		
		// execute order 66
		highSeq.visit(reconstructor, Style.EMPTY).toString();
		
		// clean up remaining diffs
		if (reconstructor.diff != null) {
			input.append(reconstructor.diff.value, Style.EMPTY);
		}
		while (diffIter.hasNext()) {
			input.append(diffIter.next().value, Style.EMPTY);
		}
		
		// build formatted input
		final var formattedInput = input.build();
		
		// get error to yell about
		final int err = StringUtils.indexOfDifference(originalValue, formattedInput.text);
		
		// get preview component depending on if it's an error or not
		MutableComponent previewComponent = null;
		if (err > -1) {
			previewComponent = Component.literal(highSeq.getString()).withStyle(style -> style
					.applyFormat(ChatFormatting.DARK_RED)
					.withHoverEvent(new HoverEvent(
							HoverEvent.Action.SHOW_TEXT,
							Component.translatable("err.builders_inventory.minimessage.mismatch", err)
							.withStyle(ChatFormatting.RED))));
			BuildersInventory.LOGGER.error("FORMAT ERROR at {} for original {} and reconstructed {}", err, originalValue, formattedInput.text);
			this.inputOverride = null;
		} else {
			if (!mini.errors.isEmpty()) {
				previewComponent = Component.empty().withStyle(ChatFormatting.RED);
				final var errors = mini.errors;
				for (int i = 0; i < errors.size(); i++) {
					String error = errors.get(i);
					if (i < errors.size() - 1) error = error + '\n';
					previewComponent.append(Component.literal(error));
				}
			} else if (this.previewOptions.doStandardPreview(this.minecraft, this.screen, this)) previewComponent = mini.getFormatted();
			if (Config.instance().minimessage_syntaxHighlighting) this.inputOverride = formattedInput;
			else this.inputOverride = null;
			this.update(mini);
		}
		
		this.previewLines = List.of();
		if (previewComponent != null) {
			ChatComponent chatLog = this.minecraft.gui.getChat();
			this.previewLines = ComponentRenderUtils.wrapComponents(previewComponent, Mth.floor(chatLog.getWidth() / chatLog.getScale()), this.font);
			this.reposition();
		} else if (this.inputOverride != null) {
			this.reposition();
		}
		return true;
	}
	
	public void clearParse() {
		this.setLastParse(null);
		this.inputOverride = null;
		this.previewLines = List.of();
		this.lastValue = null;
		this.clear();
	}
	
	private void setLastParse(@Nullable MiniMessageResult lastParse) {
		this.lastParse = lastParse;
		this.listener.onParseChange(lastParse);
	}
	
	private static Optional<HolderLookup.Provider> registryAccess(Minecraft mc) {
		return mc.level == null ? Optional.empty() : Optional.of(mc.level.registryAccess());
	}
	
	private final Int2ObjectOpenHashMap<SuggestionList> cache = new Int2ObjectOpenHashMap<>();
	
	public boolean suppressSuggestionUpdate = false;
	
	public Optional<FormattedCharSequence> tryFormatInput(String text, int offset) {
		if (this.inputOverride != null) return Optional.ofNullable(this.inputOverride.subseq(offset, offset + text.length()));
		return Optional.empty();
	}
	
	public boolean canFormat() {
		return this.inputOverride != null;
	}
	
	public FormattedCharSequence format(int start, int end) {
		if (this.inputOverride != null) {
			return this.inputOverride.subseq(start, end);
		} else throw new IllegalStateException("Expected to be ready to format string but was not");
	}
	
	public static interface PreviewOptions {
		
		public static StandardPreviewOptions standard(boolean doStandardPreview) {
			return new StandardPreviewOptions(doStandardPreview);
		}
		
		static class StandardPreviewOptions implements PreviewOptions {
			
			private final boolean doStandardPreview;
			
			private StandardPreviewOptions(boolean doStandardPreview) {
				this.doStandardPreview = doStandardPreview;
			}
			
			@Override
			public boolean doStandardPreview(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return this.doStandardPreview && Config.instance().minimessage_messagePreview;
			}
			
			@Override
			public int getBGColor(Minecraft mc, Screen screen) {
				return 0xA0000000;
			}
			
			@Override
			public float getScale(Minecraft mc, Screen screen) {
				return 1f;
			}
			
			@Override
			public int getWidth(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return screen.width;
			}
			
			@Override
			public int getX(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return 0;
			}
			
			@Override
			public int getLineTextOffset(Minecraft mc, Screen screen) {
				return -8;
			}
			
			@Override
			public int getLineHeight(Minecraft mc, Screen screen) {
				return 9;
			}
			
			@Override
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return widget.input.getY() + widget.input.getHeight() + 2 + Mth.floor(widget.getScaledLineHeight());
			}
			
		}
		
		public static PreviewOptions chat() {
			return new ChatPreviewOptions();
		}
		
		static class ChatPreviewOptions implements PreviewOptions {
			
			private ChatPreviewOptions() {
				
			}
			
			@Override
			public boolean doStandardPreview(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return Config.instance().minimessage_messagePreview;
			}
			
			@Override
			public int getBGColor(Minecraft mc, Screen screen) {
				return ((int)(255.0 * mc.options.textBackgroundOpacity().get())) << 24;
			}
			
			@Override
			public float getScale(Minecraft mc, Screen screen) {
				return (float) mc.gui.getChat().getScale();
			}
			
			@Override
			public int getWidth(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return mc.gui.getChat().getWidth();
			}
			
			@Override
			public int getX(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return 0;
			}
			
			@Override
			public int getLineTextOffset(Minecraft mc, Screen screen) {
				return (int) Math.round(-8.0 * (mc.options.chatLineSpacing().get() + 1.0) + 4.0 * mc.options.chatLineSpacing().get());
			}
			
			@Override
			public int getLineHeight(Minecraft mc, Screen screen) {
				return MiniMessageUtil.getLineHeight(mc.gui.getChat());
			}
			
			@Override
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance widget) {
				return screen.height - 14 - Config.instance().minimessage_chatPreviewHeight;
			}
			
		}
		
		public boolean doStandardPreview(Minecraft mc, Screen screen, MiniMessageInstance widget);
		
		public int getBGColor(Minecraft mc, Screen screen);
		
		public float getScale(Minecraft mc, Screen screen);
		
		public int getWidth(Minecraft mc, Screen screen, MiniMessageInstance widget);
		
		public int getX(Minecraft mc, Screen screen, MiniMessageInstance widget);
		
		public int getLineTextOffset(Minecraft mc, Screen screen);
		
		public int getLineHeight(Minecraft mc, Screen screen);
		
		public int getY(Minecraft mc, Screen screen, MiniMessageInstance widget);
		
	}
	
	private int _previewBGColor;
	private float _previewScale;
	private int _previewWidth;
	private int _previewXMin;
	private int _previewLineTextOffset;
	private int _previewLineHeight;
	private float _previewScaledLineHeight;
	private int _previewYMin;
	
	public void repositionPreview() {
		if (!this.previewLines.isEmpty()) {
			this._previewBGColor = previewOptions.getBGColor(minecraft, screen);
			this._previewScale = previewOptions.getScale(minecraft, screen);
			this._previewWidth = previewOptions.getWidth(minecraft, screen, this);
			this._previewXMin = previewOptions.getX(minecraft, screen, this);
			this._previewLineTextOffset = previewOptions.getLineTextOffset(minecraft, screen);
			this._previewLineHeight = previewOptions.getLineHeight(minecraft, screen);
			this._previewScaledLineHeight = _previewLineHeight * _previewScale;
			this._previewYMin = previewOptions.getY(minecraft, screen, this);
		}
	}
	
	public void renderPreviewOrError(GuiGraphics gui) {
		if (!active) return;
		if (!previewLines.isEmpty()) {
			gui.pose().pushPose();
			gui.pose().translate(_previewXMin, _previewYMin, 0);
			gui.pose().scale(_previewScale, _previewScale, 1f);
			gui.fill(0, 0, Mth.ceil(_previewWidth / _previewScale) + 4 + 4 + 4, - (_previewLineHeight * previewLines.size()), _previewBGColor);
			int y = _previewLineTextOffset;
			for (int i = previewLines.size() - 1; i >= 0; --i) {
				FormattedCharSequence line = previewLines.get(i);
				gui.pose().pushPose();
				gui.pose().translate(0f, 0f, 50f);
				gui.drawString(font, line, 4, y, 0xFFFFFFFF);
				gui.pose().popPose();
				y -= _previewLineHeight;
			}
			gui.pose().popPose();
		}
	}
	
	public float getScaledLineHeight() {
		return this.previewLines.size() * _previewScaledLineHeight;
	}
	
	public float getScaledLineHeight(int ignoreDiff) {
		return ignoreDiff >= this.previewLines.size() ? 0 : ((this.previewLines.size() - ignoreDiff) * _previewScaledLineHeight);
	}
	
	public boolean renderHover(GuiGraphics gui, int mouseX, int mouseY) {
		if (!previewLines.isEmpty()) {
			double localX = toPreviewX(mouseX);
			double localY = toPreviewYLine(mouseY);
			int line = getPreviewLine(localX, localY);
			if (line >= 0) {
				Style style = font.getSplitter().componentStyleAtWidth(previewLines.get(line), Mth.floor(localX));
				if (style != null && style.getHoverEvent() != null) {
					gui.renderComponentHoverEffect(font, style, mouseX, mouseY);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean renderFormatHover(GuiGraphics gui, int mouseX, int mouseY, int start, int end) {
		if (this.inputOverride != null && Config.instance().minimessage_syntaxHighlighting) {
			int inputX = input.getTextX();
			int inputXMax = inputX + input.getInnerWidth();
			int inputY = input.getTextY(start);
			int inputYMax = inputY + input.getLineHeight();
			if (mouseX >= inputX && mouseX < inputXMax && mouseY >= inputY && mouseY < inputYMax) {
				Style style = font.getSplitter().componentStyleAtWidth(inputOverride.subseq(start, end), mouseX - inputX);
				if (style != null && style.getHoverEvent() != null) {
					gui.renderComponentHoverEffect(font, style, mouseX, mouseY);
					return true;
				}
			}
		}
		return false;
	}
	
	public Style tryClickPreview(double mouseX, double mouseY) {
		if (!active) return null;
		if (!previewLines.isEmpty()) {
			double localX = toPreviewX(mouseX);
			double localY = toPreviewYLine(mouseY);
			int line = getPreviewLine(localX, localY);
			if (line >= 0) {
				Style style = font.getSplitter().componentStyleAtWidth(previewLines.get(line), Mth.floor(localX));
				if (style == null) return null;
				final ClickEvent click = style.getClickEvent();
				if (click == null || click.getAction() == ClickEvent.Action.SUGGEST_COMMAND) return null;
				return style;
			}
		}
		return null;
	}
	
	private double toPreviewX(double mouseX) {
		return ((mouseX - 4) / _previewScale) - _previewXMin;
	}
	
	private double toPreviewYLine(double mouseY) {
		return (_previewYMin - mouseY) / (_previewScale * _previewLineHeight);
	}
	
	/**
	 * Make sure to test if line >= 0 outside
	 */
	private int getPreviewLine(double localX, double localY) {
		if (localX >= 0 && localX <= Mth.floor(_previewWidth / _previewScale)) {
			int line = Mth.floor(localY);
			if (line < previewLines.size()) return line; // line >= 0 tested outside
		}
		return -1; // always fails >= 0 as it is not greater than nor equal to zero
	}
	
	@Nullable
	private ArrayList<String> unclosedTags = null;
	@Nullable
	private SuggestionList endSuggestion;
	@Nullable
	private SuggestionList suggestion;
	
	public void clear() {
		cache.clear();
		endSuggestion = null;
		suggestion = null;
		unclosedTags = null;
		if (suppressSuggestionUpdate) return;
		display.clear();
	}
	
	private void update(@NotNull MiniMessageResult msg) {
		this.clear();
		unclosedTags = msg.unclosedTags;
		List<String> unclosed = filterTagClose(msg.trailingText);
		ArrayList<String> formatUnclosed = new ArrayList<>(unclosed.size());
		for (String s : unclosed) formatUnclosed.add("</" + s + '>');
		endSuggestion = new SuggestionList(formatUnclosed, 0);
		minecraft.getProfiler().push("cursorMovedMM");
		cursorMoved(input.getValue(), input.getCursorPosition());
		minecraft.getProfiler().pop();
	}
	
	private void cursorMoved(String value, int cursor) {
		if (suppressSuggestionUpdate) return;
		if (!Config.instance().minimessage_suggestions) {
			this.clear();
			return;
		}
		if (cursor > value.length()) return;
		if (moveCursor(value, cursor)) {
			// get suggestions for finishing tags
			this.display.set(suggestion, suggestion.start, cursor, cursor == value.length());
		} else if (cursor == value.length()) {
			// get suggestions for unclosed tags
			this.display.set(endSuggestion, cursor, cursor, true);
		} else display.clear();
	}
	
	private boolean moveCursor(String value, int cursor) {
		if (cache.containsKey(cursor)) {
			var suggestion = cache.get(cursor);
			if (suggestion.valid) {
				this.suggestion = suggestion;
				return true;
			} else {
				this.suggestion = null;
				return false;
			}
		}
		if (value == null || value.isEmpty()) {
			this.suggestion = null;
			return false;
		}
		final int start = value.lastIndexOf('<', cursor - 1);
		if (start > 0 && value.charAt(start - 1) == '\\') {
			this.suggestion = null;
			return false;
		}
		if (start == -1 || start >= cursor) {
			this.suggestion = null;
			return false;
		}
		final var text = value.substring(start, cursor);
		final var parse = minecraft.level == null ? MiniMessageParser.parseNoRegistry(text) : MiniMessageParser.parse(text, Optional.of(minecraft.level.registryAccess()), ChatMiniMessageContext.currentServerIP);
		if (parse.trailingText == null) {
			this.suggestion = null;
			return false;
		}
		
		// get suggestion list
		final var input = parse.trailingText;
		if (!input.isEmpty() && parse.trailingArgs.isEmpty() && input.charAt(0) == '/') {
			if (cursor == value.length() || value.indexOf('<', cursor) == -1) {
				final var unclosed = filterTagClose(input);
				if (!unclosed.isEmpty()) {
					final var strs = new ArrayList<String>(unclosed.size());
					for (String s : unclosed) strs.add("</" + s + '>');
					this.suggestion = new SuggestionList(strs, cursor - input.length() - 1);
				} else {
					final var strs = MiniMessageFeature.TAG_LOOKUP.suggestTag(input.substring(1));
					this.suggestion = new SuggestionList(strs, cursor - input.length() + 1);
				}
			} else {
				final var strs = MiniMessageFeature.TAG_LOOKUP.suggestTag(input.substring(1));
				this.suggestion = new SuggestionList(strs, cursor - input.length() + 1);
			}
		} else if (parse.trailingArgs.isEmpty()) {
			final var strs = MiniMessageFeature.TAG_LOOKUP.suggestTag(input);
			this.suggestion = new SuggestionList(strs, cursor - input.length());
		} else {
			final var args = parse.trailingArgs;
			final var tagName = args.get(0);
			final var prev = args.size() > 1 ? args.get(args.size() - 1) : null;
			final var strs = MiniMessageFeature.TAG_LOOKUP.suggestArg(tagName, args.size() - 1, prev, input);
			this.suggestion = new SuggestionList(strs, cursor - input.length());
		}
		cache.put(cursor, this.suggestion);
		return true;
	}
	
	private List<String> filterTagClose(String trailingText) {
		if (unclosedTags == null) return List.of();
		return trailingText == null || trailingText.length() <= 1 ? unclosedTags : filterStart(unclosedTags, trailingText.substring(1));
	}
	
	private static ArrayList<String> filterStart(List<String> list, String start) {
		ArrayList<String> result = new ArrayList<>(list.size());
		for (String s : list) if (StringUtils.startsWithIgnoreCase(s, start)) result.add(s);
		return result;
	}
	
	private static class SuggestionList {
		
		public final List<String> strs;
		public final int start;
		public final int size;
		public final boolean valid;
		
		public SuggestionList(List<String> strs, int start) {
			this.strs = strs;
			this.start = start;
			this.size = strs.size();
			this.valid = !strs.isEmpty();
		}
		
	}
	
	public static interface SuggestionOptions {
		
		public static final int BG_MID = 0x80000000;
		public static final int BG_DARK = 0xD0000000;
		
		public static SuggestionOptions standard(int suggestionLimit) {
			return new StandardSuggestionOptions(suggestionLimit);
		}
		
		static class StandardSuggestionOptions implements SuggestionOptions {
			
			private final int suggestionLimit;
			
			private StandardSuggestionOptions(int suggestionLimit) {
				this.suggestionLimit = suggestionLimit;
			}
			
			@Override
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance widget, int x, int suggestionHeight) {
				return widget.previewLines.isEmpty() ? (widget.input.getY() + widget.input.getHeight()) : (widget._previewYMin + 1);
			}
			
			@Override
			public int getColor() {
				return BG_MID;
			}
			
			@Override
			public int getLimit() {
				return this.suggestionLimit;
			}
			
			@Override
			public void claimSuggestions() {
				// Nothing
			}
			
		}
		
		public static SuggestionOptions chat(Supplier<CommandSuggestions> commandSuggestions) {
			return new ChatSuggestionOptions(commandSuggestions);
		}
		
		static class ChatSuggestionOptions implements SuggestionOptions {
			
			private final Supplier<CommandSuggestions> commandSuggestions;
			
			private ChatSuggestionOptions(Supplier<CommandSuggestions> commandSuggestions) {
				this.commandSuggestions = commandSuggestions;
			}
			
			@Override
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance widget, int x, int suggestionHeight) {
				ChatComponent chatc = mc.gui.getChat();
				return screen.height - 12 - 3 - suggestionHeight - ((x >= chatc.getWidth() + 12) ? 0 :
					widget.previewLines.isEmpty() ? 0 : (Mth.ceil(widget.getScaledLineHeight()) + Config.instance().minimessage_chatPreviewHeight));
			}
			
			@Override
			public int getColor() {
				return BG_DARK;
			}
			
			@Override
			public int getLimit() {
				return 10;
			}
			
			@Override
			public void claimSuggestions() {
				this.commandSuggestions.get().setAllowSuggestions(false);
			}
			
		}
		
		public int getY(Minecraft mc, Screen screen, MiniMessageInstance widget, int x, int suggestionHeight);
		
		public int getColor();
		
		public int getLimit();
		
		public void claimSuggestions();
		
	}
	
	public void reposition() {
		this.repositionPreview();
		if (this.display.visible) this.display.reposition();
	}
	
	public boolean renderSuggestions(GuiGraphics gui, int mouseX, int mouseY) {
		if (!active) return false;
		gui.pose().pushPose();
		gui.pose().translate(0, 0, 200f);
		final boolean result = this.display.render(gui, mouseX, mouseY);
		gui.pose().popPose();
		return result;
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!active) return false;
		if (this.display.visible) return this.display.keyPressed(keyCode, scanCode, modifiers);
		else if (keyCode == GLFW.GLFW_KEY_TAB && this.unclosedTags != null) {
			this.cursorMoved(this.input.getValue(), this.input.getCursorPosition());
			return true;
		} else return false;
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!active || !this.display.visible) return false;
		return this.display.mouseScrolled(
				(int)mouseX,
				(int)mouseY,
				Mth.clamp(scrollY, -1.0, 1.0));
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!active || !this.display.visible) return false;
		return this.display.mouseClicked((int)mouseX, (int)mouseY, button);
	}
	
	private class SuggestionsDisplay {
		
		private final SuggestionOptions suggestionOptions;
		
		public boolean visible = false;
		public List<String> suggestion = List.of();
		public int start = 0;
		private int end = 0;
		private boolean atEnd = false;
		private int x = 0;
		private int y = 0;
		private int width = 0;
		private int height = 0;
		
		public int lastMouseX = 0;
		public int lastMouseY = 0;
		
		public int offset = 0;
		public int selected = 0;
		private boolean tabCycles = false;
		
		public SuggestionsDisplay(SuggestionOptions suggestionOptions) {
			this.suggestionOptions = suggestionOptions;
		}
		
		public void clear() {
			this.hide();
			this.suggestion = null;
			MiniMessageInstance.this.input.setSuggestion(null);
		}
		
		private void hide() {
			this.visible = false;
			this.tabCycles = false;
		}
		
		public void set(SuggestionList list, int start, int cursor, boolean atEnd) {
			this.clear();
			if (list == null || !list.valid) return;
			this.visible = true;
			this.suggestion = list.strs;
			this.start = start;
			this.end = cursor;
			this.atEnd = atEnd;
			
			int widthScan = 0;
			for (String str : this.suggestion) widthScan = Math.max(widthScan, MiniMessageInstance.this.font.width(str));
			this.width = widthScan + 1;
			this.height = Math.min(list.size, this.suggestionOptions.getLimit()) * 12;
			this.reposition();
			
			this.offset = 0;
			this.select(0);
		}
		
		public void reposition() {
			final var input = MiniMessageInstance.this.input;
			x = Mth.clamp(
					input.getScreenX(this.start),
					1,
					screen.width - this.width) - 1;
			y = this.suggestionOptions.getY(
					MiniMessageInstance.this.minecraft,
					screen,
					MiniMessageInstance.this,
					x,
					height);
		}
		
		public boolean render(GuiGraphics gui, int mouseX, int mouseY) {
			if (!this.visible) return false;
			final int size = Math.min(this.suggestion.size(), this.suggestionOptions.getLimit());
			final boolean topCut = this.offset > 0;
			final boolean bottomCut = this.suggestion.size() > this.offset + size;
			
			final int bgColor = this.suggestionOptions.getColor();
			
			if (topCut || bottomCut) {
				gui.fill(x, y - 1, x + width, y, bgColor);
				gui.fill(x, y + height, x + width, y + height + 1, bgColor);
				if (topCut) for (int i = 0; i < width; i++) {
					if (i % 2 == 0) gui.fill(x + i, y - 1, x + i + 1, y, 0xFFFFFFFF);
				}
				
				if (bottomCut) for (int i = 0; i < width; i++) {
					if (i % 2 == 0) gui.fill(x + i, y + height, x + i + 1, y + height + 1, 0xFFFFFFFF);
				}
			}
			
			final boolean mouseMoved = mouseX != this.lastMouseX || mouseY != this.lastMouseY;
			if (mouseMoved) {
				this.lastMouseX = mouseX;
				this.lastMouseY = mouseY;
			}
			
			gui.fill(x, y, x + width, y + height, bgColor);
			
			for (int i = 0; i < size; i++) {
				final int index = i + this.offset;
				
				if (mouseMoved
						&& mouseX > x
						&& mouseX < x + width
						&& mouseY > y + (i * 12)
						&& mouseY < y + (i * 12) + 12) {
					select(index);
				}
				
				String s = this.suggestion.get(index);
				gui.drawString(MiniMessageInstance.this.font, s, x + 1, y + 2 + (12 * i), (index == this.selected) ? 0xFFFFFF00 : 0xFFAAAAAA);
			}
			
			return true;
		}
		
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (keyCode == GLFW.GLFW_KEY_UP) {
				cycle(-1);
				tabCycles = false;
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				cycle(1);
				tabCycles = false;
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_TAB) {
				if (tabCycles) {
					cycle(Screen.hasShiftDown() ? -1 : 1);
				}
				useSuggestion();
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				hide();
				return true;
			} else return false;
		}
		
		public boolean mouseScrolled(int mouseX, int mouseY, double delta) {
			if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
				offset = Mth.clamp((int)(offset - delta), 0, Math.max(suggestion.size() - this.suggestionOptions.getLimit(), 0));
				return true;
			} else return false;
		}
		
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
				int ind = (mouseY - y) / 12 + offset;
				if (ind >= 0 && ind < suggestion.size()) {
					select(ind);
					useSuggestion();
				}
				return true;
			} else return false;
		}
		
		private void cycle(int amt) {
			select(selected + amt);
			if (selected < offset) offset = Mth.clamp(selected, 0, Math.max(suggestion.size() - this.suggestionOptions.getLimit(), 0));
			else if (selected > offset + this.suggestionOptions.getLimit() - 1) offset = Mth.clamp(selected + 9, 0, Math.max(suggestion.size() - this.suggestionOptions.getLimit(), 0));
		}
		
		private void select(int i) {
			if (i < 0) i += suggestion.size();
			if (i >= suggestion.size()) i -= suggestion.size();
			this.selected = i;
			String sel = suggestion.get(i);
			String hint = substr(sel, end - start);
			if (hint == null) MiniMessageInstance.this.input.setSuggestion(null);
			else if (atEnd && sel.startsWith(MiniMessageInstance.this.input.getValue().substring(start, end))) MiniMessageInstance.this.input.setSuggestion(hint);
		}
		
		@Nullable
		private static String substr(String s, int begin) {
			return begin >= s.length() ? null : s.substring(begin);
		}
		
		private void useSuggestion() {
			String str = suggestion.get(selected);
			MiniMessageInstance.this.suppressSuggestionUpdate = true;
			final var input = MiniMessageInstance.this.input;
			String original = input.getValue();
			input.setValue(original.substring(0, start) + str + (this.atEnd ? "" : input.getValue().substring(end)));
			input.setSuggestion(null);
			final int cursor = start + str.length();
			input.setCursorPosition(cursor);
			input.setHighlightPos(cursor);
			this.end = cursor;
			MiniMessageInstance.this.suppressSuggestionUpdate = false;
			tabCycles = true;
		}
		
	}
	
}
