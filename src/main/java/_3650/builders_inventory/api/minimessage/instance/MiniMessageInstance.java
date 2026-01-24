package _3650.builders_inventory.api.minimessage.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
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
import _3650.builders_inventory.mixin.feature.minimessage.ChatComponentInvoker;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

// help me
/**
 * Methods to call when using this:<br>
 * <br>
 * {@link #tick()}<br>
 * {@link #unknownEdit()}<br>
 * {@link #cursorMoved()}<br>
 * {@link #inputEdited()}<br>
 * {@link #quietUpdate()}<br>
 * {@link #renderPreviewOrError(GuiGraphics, ActiveTextCollector, net.minecraft.client.gui.ActiveTextCollector.Parameters)}
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
	private final MiniMessageParseListener listener;
	private final PreviewOptions previewOptions;
	private final SuggestionsDisplay display;
	
	
	public MiniMessageInstance(
			Minecraft minecraft,
			Screen screen,
			Font font,
			WrappedTextField input,
			MiniMessageValidator context,
			MiniMessageParseListener listener,
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
	
	private static class ValidatorHolder implements Consumer<MiniMessageValidator> {
		
		public MiniMessageValidator validator;
		
		public ValidatorHolder(@NotNull MiniMessageValidator initial) {
			this.validator = initial;
		}
		
		@Override
		public void accept(MiniMessageValidator validator) {
			this.validator = validator;
		}
		
	}
	
	private boolean updateMiniMessage(@NotNull String value) {
		final String originalValue = value;
		
		var validatorUsed = new ValidatorHolder(this.context);
		{
			// only work in certain contexts
			final var newVal = this.context.isValid(this.minecraft, value, validatorUsed);
//			BuildersInventory.LOGGER.warn(newVal.toString());
			
			// validate value
			if (newVal.isEmpty() || newVal.get().isEmpty()) {
				this.clearParse();
				return false;
			} else value = newVal.get();
		}
		
		// parse message
		final var parseResult = MiniMessageParser.parse(value, registryAccess(this.minecraft), ChatMiniMessageContext.currentServerIP);
		this.setLastParse(parseResult);
		
		// get plaintext with fun syntax highlighting
		MutableComponent highlighted = parseResult.getFormattedPlainText();
		
		// input result
		final var formatBuilder = new HighlightedTextInput.Builder(originalValue.length());
		
		// construct the formatted input, now the validators' responsibility
		validatorUsed.validator.applyFormattedInput(originalValue, value, highlighted, formatBuilder);
		
		// build formatted input
		final HighlightedTextInput formattedInput = formatBuilder.build();
		
		// get error to yell about
		final int err = StringUtils.indexOfDifference(originalValue, formattedInput.text);
		
		// get preview component depending on if it's an error or not
		MutableComponent previewComponent = null;
		if (err > -1) {
			previewComponent = Component.literal(formattedInput.text).withStyle(style -> style
					.applyFormat(ChatFormatting.DARK_RED)
					.withHoverEvent(new HoverEvent.ShowText(
							Component.translatable("err.builders_inventory.minimessage.mismatch", err)
							.withStyle(ChatFormatting.RED))));
			BuildersInventory.LOGGER.error("FORMAT ERROR at {} for original {} and reconstructed {}", err, originalValue, formattedInput.text);
			this.inputOverride = null;
		} else {
			if (!parseResult.errors.isEmpty()) {
				previewComponent = Component.empty().withStyle(ChatFormatting.RED);
				final var errors = parseResult.errors;
				for (int i = 0; i < errors.size(); i++) {
					String error = errors.get(i);
					if (i < errors.size() - 1) error = error + '\n';
					previewComponent.append(Component.literal(error));
				}
			} else if (this.previewOptions.doStandardPreview(this.minecraft, this.screen, this)) previewComponent = parseResult.getFormatted();
			if (Config.instance().minimessage_syntaxHighlighting) this.inputOverride = formattedInput;
			else this.inputOverride = null;
			this.update(parseResult);
		}
		
		this.previewLines = List.of();
		if (previewComponent != null) {
			int previewScaledWidth = Mth.floor(this.previewOptions.getScaledWidth(this.minecraft, this.screen, this));
			this.previewLines = ComponentRenderUtils.wrapComponents(previewComponent, previewScaledWidth, this.font);
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
			public boolean doStandardPreview(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
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
			public float getScaledWidth(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
				return screen.width;
			}
			
			@Override
			public int getX(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
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
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
				return minimessage.input.getY() + minimessage.input.getHeight() + 2 + Mth.floor(minimessage.getScaledLineHeight());
			}
			
		}
		
		public static PreviewOptions chat() {
			return new ChatPreviewOptions();
		}
		
		static class ChatPreviewOptions implements PreviewOptions {
			
			private ChatPreviewOptions() {
				
			}
			
			@Override
			public boolean doStandardPreview(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
				return Config.instance().minimessage_messagePreview;
			}
			
			@Override
			public int getBGColor(Minecraft mc, Screen screen) {
				return ((int)(255.0 * mc.options.textBackgroundOpacity().get())) << 24;
			}
			
			@Override
			public float getScale(Minecraft mc, Screen screen) {
				return (float) ((ChatComponentInvoker)mc.gui.getChat()).callGetScale();
			}
			
			@Override
			public float getScaledWidth(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
				return ((ChatComponentInvoker)mc.gui.getChat()).callGetWidth() / this.getScale(mc, screen);
			}
			
			@Override
			public int getX(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
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
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance minimessage) {
				return screen.height - 14 - Config.instance().minimessage_chatPreviewHeight;
			}
			
		}
		
		public boolean doStandardPreview(Minecraft mc, Screen screen, MiniMessageInstance minimessage);
		
		public int getBGColor(Minecraft mc, Screen screen);
		
		public float getScale(Minecraft mc, Screen screen);
		
		public float getScaledWidth(Minecraft mc, Screen screen, MiniMessageInstance minimessage);
		
		public int getX(Minecraft mc, Screen screen, MiniMessageInstance minimessage);
		
		public int getLineTextOffset(Minecraft mc, Screen screen);
		
		public int getLineHeight(Minecraft mc, Screen screen);
		
		public int getY(Minecraft mc, Screen screen, MiniMessageInstance minimessage);
		
	}
	
	private int _previewBGColor;
	private float _previewScale;
	private float _previewScaledWidth;
	private int _previewXMin;
	private int _previewLineTextOffset;
	private int _previewLineHeight;
	private float _previewScaledLineHeight;
	private int _previewYMin;
	
	public void repositionPreview() {
		if (!this.previewLines.isEmpty()) {
			this._previewBGColor = previewOptions.getBGColor(minecraft, screen);
			this._previewScale = previewOptions.getScale(minecraft, screen);
			this._previewScaledWidth = previewOptions.getScaledWidth(minecraft, screen, this);
			this._previewXMin = previewOptions.getX(minecraft, screen, this);
			this._previewLineTextOffset = previewOptions.getLineTextOffset(minecraft, screen);
			this._previewLineHeight = previewOptions.getLineHeight(minecraft, screen);
			this._previewScaledLineHeight = _previewLineHeight * _previewScale;
			this._previewYMin = previewOptions.getY(minecraft, screen, this);
		}
	}
	
	public void renderPreviewOrError(GuiGraphics gui, ActiveTextCollector text, ActiveTextCollector.Parameters parameters) {
		if (!active) return;
		if (!previewLines.isEmpty()) {
			gui.pose().pushMatrix();
			gui.pose().translate(_previewXMin, _previewYMin);
			gui.pose().scale(_previewScale, _previewScale);
			parameters = parameters.withPose(new Matrix3x2f(gui.pose()));
			
			gui.fill(0, 0, Mth.ceil(_previewScaledWidth) + 4 + 4 + 4, - (_previewLineHeight * previewLines.size()), _previewBGColor);
			int y = _previewLineTextOffset;
			for (int i = previewLines.size() - 1; i >= 0; --i) {
				FormattedCharSequence line = previewLines.get(i);
				text.accept(TextAlignment.LEFT, 4, y, parameters, line);
				y -= _previewLineHeight;
			}
			
			gui.pose().popMatrix();
		}
	}
	
	public float getScaledLineHeight() {
		return this.previewLines.size() * _previewScaledLineHeight;
	}
	
	public float getScaledLineHeight(int ignoreDiff) {
		return ignoreDiff >= this.previewLines.size() ? 0 : ((this.previewLines.size() - ignoreDiff) * _previewScaledLineHeight);
	}
	
	@Nullable
	private ArrayList<String> unclosedTags = null;
	@Nullable
	private SuggestionList suggestions;
	@Nullable
	private SuggestionList endSuggestions;
	
	public void clear() {
		cache.clear();
		suggestions = null;
		endSuggestions = null;
		unclosedTags = null;
		if (suppressSuggestionUpdate) return;
		display.clear();
	}
	
	private void update(@NotNull MiniMessageResult msg) {
		this.clear();
		unclosedTags = msg.unclosedTags;
		List<String> unclosed = filterTagClose(msg.trailingText);
		ArrayList<String> formatUnclosed = new ArrayList<>(unclosed.size());
		for (String tag : unclosed) formatUnclosed.add("</" + tag + '>');
		endSuggestions = new SuggestionList(formatUnclosed, 0);
		ProfilerFiller profiler = Profiler.get();
		profiler.push("cursorMovedMM");
		cursorMoved(input.getValue(), input.getCursorPosition());
		profiler.pop();
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
			this.display.set(suggestions, suggestions.start, cursor, cursor == value.length());
		} else if (cursor == value.length()) {
			// get suggestions for unclosed tags
			this.display.set(endSuggestions, cursor, cursor, true);
		} else display.clear();
	}
	
	private boolean moveCursor(String value, int cursor) {
		if (cache.containsKey(cursor)) {
			SuggestionList suggestions = cache.get(cursor);
			if (suggestions.valid) {
				this.suggestions = suggestions;
				return true;
			} else {
				this.suggestions = null;
				return false;
			}
		}
		if (value == null || value.isEmpty()) {
			this.suggestions = null;
			return false;
		}
		final int start = value.lastIndexOf('<', cursor - 1);
		if (start > 0 && value.charAt(start - 1) == '\\') {
			this.suggestions = null;
			return false;
		}
		if (start == -1 || start >= cursor) {
			this.suggestions = null;
			return false;
		}
		final var searchText = value.substring(start, cursor);
		final var tagSearch = minecraft.level == null ? MiniMessageParser.parseNoRegistry(searchText) : MiniMessageParser.parse(searchText, Optional.of(minecraft.level.registryAccess()), ChatMiniMessageContext.currentServerIP);
		if (tagSearch.trailingText == null) {
			this.suggestions = null;
			return false;
		}
		
		// get suggestion list
		final var input = tagSearch.trailingText;
		if (!input.isEmpty() && tagSearch.trailingArgs.isEmpty() && input.charAt(0) == '/') {
			if (cursor == value.length() || value.indexOf('<', cursor) == -1) {
				final var unclosed = filterTagClose(input);
				if (!unclosed.isEmpty()) {
					final var vals = new ArrayList<String>(unclosed.size());
					for (String tag : unclosed) vals.add("</" + tag + '>');
					this.suggestions = new SuggestionList(vals, cursor - input.length() - 1);
				} else {
					final var vals = MiniMessageFeature.TAG_LOOKUP.suggestTag(input.substring(1));
					this.suggestions = new SuggestionList(vals, cursor - input.length() + 1);
				}
			} else {
				final var vals = MiniMessageFeature.TAG_LOOKUP.suggestTag(input.substring(1));
				this.suggestions = new SuggestionList(vals, cursor - input.length() + 1);
			}
		} else if (tagSearch.trailingArgs.isEmpty()) {
			final var vals = MiniMessageFeature.TAG_LOOKUP.suggestTag(input);
			this.suggestions = new SuggestionList(vals, cursor - input.length());
		} else {
			final var args = tagSearch.trailingArgs;
			final var tagName = args.get(0);
			final var prev = args.size() > 1 ? args.get(args.size() - 1) : null;
			final var vals = MiniMessageFeature.TAG_LOOKUP.suggestArg(tagName, args.size() - 1, prev, input);
			this.suggestions = new SuggestionList(vals, cursor - input.length());
		}
		cache.put(cursor, this.suggestions);
		return true;
	}
	
	private List<String> filterTagClose(String closingTag) {
		if (unclosedTags == null) return List.of();
		return closingTag == null || closingTag.length() <= 1 ? unclosedTags : filterStart(unclosedTags, closingTag.substring(1));
	}
	
	private static ArrayList<String> filterStart(List<String> list, String start) {
		ArrayList<String> result = new ArrayList<>(list.size());
		for (String s : list) if (Strings.CI.startsWith(s, start)) result.add(s);
		return result;
	}
	
	private static class SuggestionList {
		
		public final List<String> vals;
		public final int start;
		public final int size;
		public final boolean valid;
		
		public SuggestionList(List<String> vals, int start) {
			this.vals = vals;
			this.start = start;
			this.size = vals.size();
			this.valid = !vals.isEmpty();
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
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance minimessage, int x, int suggestionHeight) {
				return minimessage.previewLines.isEmpty() ? (minimessage.input.getY() + minimessage.input.getHeight()) : (minimessage._previewYMin + 1);
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
			public int getY(Minecraft mc, Screen screen, MiniMessageInstance minimessage, int x, int suggestionHeight) {
				final int chatWidth = ((ChatComponentInvoker)mc.gui.getChat()).callGetWidth();
				return screen.height - 12 - 3 - suggestionHeight - ((x >= chatWidth + 12) ? 0 :
					minimessage.previewLines.isEmpty() ? 0 : (Mth.ceil(minimessage.getScaledLineHeight()) + Config.instance().minimessage_chatPreviewHeight));
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
		
		public int getY(Minecraft mc, Screen screen, MiniMessageInstance minimessage, int x, int suggestionHeight);
		
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
		final boolean result = this.display.render(gui, mouseX, mouseY);
		return result;
	}
	
	public boolean keyPressed(KeyEvent event) {
		if (!active) return false;
		if (this.display.visible) return this.display.keyPressed(event);
		else if (event.key() == GLFW.GLFW_KEY_TAB && this.unclosedTags != null) {
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
	
	public boolean mouseClicked(MouseButtonEvent event) {
		if (!active || !this.display.visible) return false;
		return this.display.mouseClicked(event);
	}
	
	private class SuggestionsDisplay {
		
		private final SuggestionOptions suggestionOptions;
		
		public boolean visible = false;
		public List<String> suggestions = List.of();
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
			this.suggestions = null;
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
			this.suggestions = list.vals;
			this.start = start;
			this.end = cursor;
			this.atEnd = atEnd;
			
			int widthScan = 0;
			for (String str : this.suggestions) widthScan = Math.max(widthScan, MiniMessageInstance.this.font.width(str));
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
			final int size = Math.min(this.suggestions.size(), this.suggestionOptions.getLimit());
			final boolean topCut = this.offset > 0;
			final boolean bottomCut = this.suggestions.size() > this.offset + size;
			
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
				
				String s = this.suggestions.get(index);
				gui.drawString(MiniMessageInstance.this.font, s, x + 1, y + 2 + (12 * i), (index == this.selected) ? 0xFFFFFF00 : 0xFFAAAAAA);
			}
			
			return true;
		}
		
		public boolean keyPressed(KeyEvent event) {
			switch (event.key()) {
			case GLFW.GLFW_KEY_UP:
				cycle(-1);
				tabCycles = false;
				return true;
			case GLFW.GLFW_KEY_DOWN:
				cycle(1);
				tabCycles = false;
				return true;
			case GLFW.GLFW_KEY_TAB:
				if (tabCycles) {
					cycle(event.hasShiftDown() ? -1 : 1);
				}
				useSuggestion();
				return true;
			case GLFW.GLFW_KEY_ESCAPE:
				hide();
				return true;
			default:
				return false;
			}
		}
		
		public boolean mouseScrolled(int mouseX, int mouseY, double delta) {
			if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
				offset = Mth.clamp((int)(offset - delta), 0, Math.max(suggestions.size() - this.suggestionOptions.getLimit(), 0));
				return true;
			} else return false;
		}
		
		public boolean mouseClicked(MouseButtonEvent event) {
			final int mouseX = (int)event.x();
			final int mouseY = (int)event.y();
			if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
				int index = (mouseY - y) / 12 + offset;
				if (index >= 0 && index < suggestions.size()) {
					select(index);
					useSuggestion();
				}
				return true;
			} else return false;
		}
		
		private void cycle(int amt) {
			select(selected + amt);
			if (selected < offset) offset = Mth.clamp(selected, 0, Math.max(suggestions.size() - this.suggestionOptions.getLimit(), 0));
			else if (selected > offset + this.suggestionOptions.getLimit() - 1) offset = Mth.clamp(selected + 9, 0, Math.max(suggestions.size() - this.suggestionOptions.getLimit(), 0));
		}
		
		private void select(int i) {
			if (i < 0) i += suggestions.size();
			if (i >= suggestions.size()) i -= suggestions.size();
			this.selected = i;
			String selectedVal = suggestions.get(i);
			String hint = substr(selectedVal, end - start);
			if (hint == null) MiniMessageInstance.this.input.setSuggestion(null);
			else if (atEnd && selectedVal.startsWith(MiniMessageInstance.this.input.getValue().substring(start, end))) MiniMessageInstance.this.input.setSuggestion(hint);
		}
		
		@Nullable
		private static String substr(String s, int begin) {
			return begin >= s.length() ? null : s.substring(begin);
		}
		
		private void useSuggestion() {
			String selectedVal = suggestions.get(selected);
			MiniMessageInstance.this.suppressSuggestionUpdate = true;
			final var input = MiniMessageInstance.this.input;
			String original = input.getValue();
			input.setValue(original.substring(0, start) + selectedVal + (this.atEnd ? "" : input.getValue().substring(end)));
			input.setSuggestion(null);
			final int cursor = start + selectedVal.length();
			input.setCursorPosition(cursor);
			input.setHighlightPos(cursor);
			this.end = cursor;
			MiniMessageInstance.this.suppressSuggestionUpdate = false;
			tabCycles = true;
		}
		
	}
	
}
