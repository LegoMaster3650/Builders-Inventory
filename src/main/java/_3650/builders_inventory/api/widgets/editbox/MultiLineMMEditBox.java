package _3650.builders_inventory.api.widgets.editbox;

import java.util.ArrayList;
import java.util.function.IntConsumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.minimessage.MiniMessageResult;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageParseListener;
import _3650.builders_inventory.api.minimessage.instance.MMInstanceConstructor;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance;
import _3650.builders_inventory.api.minimessage.widgets.MiniMessageEventListener;
import _3650.builders_inventory.api.minimessage.widgets.wrapper.WrappedTextField;
import _3650.builders_inventory.api.util.StringPos;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;

// scary evil monolithic class thats an amalgam of EditBox, MultiLineEditBox, AbstractScrollWidget and MultilineTextField with some MiniMessageWidget thrown in for bad luck
// if this crashes again im gonna be the one crashing next
/**
 * An Abomination.<br>
 * You still need to handle calling the following:<br>
 * {@link #miniMessageTick()}<br>
 * {@link #miniMessageMouseScrolled(double, double, double, double)}<br>
 * {@link #miniMessageMouseClicked(double, double, int)}<br>
 * These are either not possible in a widget or occur outside the widget's area and need full-screen coverage
 */
public class MultiLineMMEditBox extends AbstractWidget implements MiniMessageEventListener {
	
	private static final int CURSOR_COLOR = 0xFFD0D0D0;
	
	private static final double SCROLL_RATE = 4.5;
	
	private final MMInstanceConstructor mmConstructor;
	private final EditBoxTheme theme;
	private final Font font;
	private final ArrayList<StringPos> displayLines = new ArrayList<>();
	private final ArrayList<FormatLine> formatLines = new ArrayList<>();
	
	
	private int maxLength = Integer.MAX_VALUE;
	
	@Nullable
	private String suggestion = null;
	
	private static final IntConsumer IGNORE_INT = x -> {};
	private static final Runnable IGNORE_RUN = () -> {};
	
	private LinedMMEditBoxListener changeListener = LinedMMEditBoxListener.IGNORE;
	private FormatLineConsumer lineEditListener = FormatLineConsumer.IGNORE;
	private IntConsumer lineCreateListener = IGNORE_INT;
	private IntConsumer lineDeleteListener = IGNORE_INT;
	private Runnable linesResetListener = IGNORE_RUN;
	
	@Nullable
	private MiniMessageInstance activeMiniMessage = null;
	
	private boolean showLineNumbers = false;
	private int lineNumWidth = 0;
	
	private boolean externalScrollbar = false;
	private int scrollbarPadding = 0;
	private int scrollbarHeight = 0;
	private int rightPadding = 0;
	
	
	private String value;
	private int cursor;
	private int selectCursor;
	private boolean selecting;
	
	private double scrollAmount;
	private boolean scrolling;
	private long focusedTime = Util.getMillis();
	
	public MultiLineMMEditBox(MMInstanceConstructor mmConstructor, EditBoxTheme options, Font font, int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
		this.mmConstructor = mmConstructor;
		this.theme = options;
		this.font = font;
		this.setValue("");
	}
	
	public void setChangeListener(LinedMMEditBoxListener changeListener) {
		this.changeListener = changeListener;
	}
	
	public void setMiniMessageListener(
			FormatLineConsumer lineEditListener,
			IntConsumer lineCreateListener,
			IntConsumer lineDeleteListener,
			Runnable linesResetListener) {
		this.lineEditListener = lineEditListener;
		this.lineCreateListener = lineCreateListener;
		this.lineDeleteListener = lineDeleteListener;
		this.linesResetListener = linesResetListener;
	}
	
	public void setMaxLength(int limit) {
		this.maxLength = limit;
	}
	
	public void setSuggestion(@Nullable String suggestion) {
		this.suggestion = suggestion;
	}
	
	public void setShowLineNumbers(boolean showLineNumbers) {
		this.showLineNumbers = showLineNumbers;
		this.refreshLineNumWidth();
	}
	
	public void setExternalScrollbar(boolean externalScrollbar) {
		this.externalScrollbar = externalScrollbar;
		this.refreshScrollbar();
	}
	
	public void setValue(String value) {
		this.value = StringUtil.truncateStringIfNecessary(
				value,
				this.maxLength,
				false);
		this.cursor = this.value.length();
		this.selectCursor = this.cursor;
		var initLines = this.refreshFormatLines();
		this.reflowDisplayLines();
		this.scrollToCursor();
		for (var line : initLines) line.initUpdate();
	}
	
	public String getValue() {
		return this.value;
	}
	
	public String getText(StringPos pos) {
		return this.value.substring(pos.beginIndex, pos.endIndex);
	}
	
	public void insertText(String text) {
		if (!text.isEmpty() || this.hasSelection()) {
			StringPos selection = this.getSelected();
			this.insertTextCommon(text, selection);
		}
	}
	
	protected void insertTextInternal(String text) {
		if (!text.isEmpty() || this.hasSelection()) {
			StringPos selection = this.getSelected();
			String string = this.insertTextCommon(text, selection);
			this.changeListener.onInsert(string, selection.beginIndex, selection.endIndex);
		}
	}
	
	private String insertTextCommon(String text, StringPos selection) {
		String string = StringUtil.truncateStringIfNecessary(
				StringUtil.filterText(text, true),
				this.maxLength - this.value.length(),
				false);
		this.value = new StringBuilder(this.value).replace(selection.beginIndex, selection.endIndex, string).toString();
		this.cursor = selection.beginIndex + string.length();
		this.selectCursor = this.cursor;
		var initLines = this.refreshFormatRange(selection.beginIndex, selection.endIndex, this.cursor);
		this.reflowDisplayLines();
		this.scrollToCursor();
		for (var line : initLines) line.initUpdate();
		return string;
	}
	
	public void deleteText(int length) {
		this.deleteTextCommon(length);
		this.insertText("");
	}
	
	protected void deleteTextInternal(int length) {
		this.deleteTextCommon(length);
		this.insertTextInternal("");
	}
	
	private void deleteTextCommon(int length) {
		if (!this.hasSelection()) {
			this.selectCursor = Mth.clamp(this.cursor + length, 0, this.value.length());
		}
	}
	
	public int getCursorPos() {
		return this.cursor;
	}
	
	public void setSelected(int begin, int end) {
		this.selectCursor = begin;
		this.cursor = end;
	}
	
	public StringPos getSelected() {
		return new StringPos(Math.min(this.selectCursor, this.cursor), Math.max(this.selectCursor, this.cursor));
	}
	
	public String getSelectedText() {
		var selection = this.getSelected();
		return this.value.substring(selection.beginIndex, selection.endIndex);
	}
	
	public boolean hasSelection() {
		return this.selectCursor != this.cursor;
	}
	
	public int getLineCount() {
		return this.displayLines.size();
	}
	
	public boolean hasMaxLength() {
		return this.maxLength != Integer.MAX_VALUE;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean inBounds = this.inBounds(mouseX, mouseY);
		if (inBounds && button == InputConstants.MOUSE_BUTTON_LEFT) {
			this.selecting = Screen.hasShiftDown();
			this.seekCursorScreen(mouseX, mouseY);
			return true;
		} else {
			final int xMin = this.getX() + this.width - this.scrollbarPadding;
			final int xMax = xMin + Math.max(this.scrollBarWidth(), this.scrollbarPadding);
			final int yMin = this.getY();
			final int yMax = this.getY() + this.height;
			boolean clickedScrollbar = this.scrollBarVisible() && mouseX >= xMin && mouseX <= xMax && mouseY >= yMin && mouseY < yMax;
			if (clickedScrollbar && button == InputConstants.MOUSE_BUTTON_LEFT) {
				this.scrolling = true;
				return true;
			} else return inBounds || clickedScrollbar;
		}
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.isActive() && this.isFocused() && this.scrolling) {
			final int scrollSnapEdge = this.innerPadding() - this.theme.borderThickness;
			if (mouseY < this.getY() + scrollSnapEdge) {
				this.setScrollAmount(0);
			} else if (mouseY > this.getY() + this.height - scrollSnapEdge) {
				this.setScrollAmount(this.getMaxScrollAmount());
			} else {
				double scrollMod = Math.max(1, this.getMaxScrollAmount() / (this.height - this.totalVerticalPadding() - this.getScrollBarHeight()));
				this.setScrollAmount(this.scrollAmount + dragY * scrollMod);
			}
			return true;
		} else if (this.inBounds(mouseX, mouseY) && button == InputConstants.MOUSE_BUTTON_LEFT) {
			this.selecting = true;
			this.seekCursorScreen(mouseX, mouseY);
			this.selecting = Screen.hasShiftDown();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			this.scrolling = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!this.isActive()) return false;
		this.setScrollAmount(this.scrollAmount - scrollY * SCROLL_RATE);
		return true;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!this.isActive()) return false;
		this.selecting = Screen.hasShiftDown();
		if (keyCode != InputConstants.KEY_UP && keyCode != InputConstants.KEY_DOWN) {
			if (this.activeMiniMessage != null) {
				if (this.activeMiniMessage.keyPressed(keyCode, scanCode, modifiers)) return true;
			}
		}
		if (Screen.isSelectAll(keyCode)) {
			this.cursor = this.value.length();
			this.selectCursor = 0;
			return true;
		} else if (Screen.isCopy(keyCode)) {
			Minecraft mc = Minecraft.getInstance();
			mc.keyboardHandler.setClipboard(this.getSelectedText());
			return true;
		} else if (Screen.isCut(keyCode)) {
			Minecraft mc = Minecraft.getInstance();
			mc.keyboardHandler.setClipboard(this.getSelectedText());
			this.insertTextInternal("");
			return true;
		} else if (Screen.isPaste(keyCode)) {
			Minecraft mc = Minecraft.getInstance();
			this.insertTextInternal(mc.keyboardHandler.getClipboard());
			return true;
		} else {
			switch (keyCode) {
			case InputConstants.KEY_RETURN:
			case InputConstants.KEY_NUMPADENTER:
				this.insertTextInternal("\n");
				return true;
			case InputConstants.KEY_BACKSPACE:
				if (Screen.hasControlDown()) {
					var pos = this.getPreviousWord();
					this.deleteTextInternal(pos.beginIndex - this.cursor);
				} else {
					this.deleteTextInternal(-1);
				}
				return true;
			case InputConstants.KEY_DELETE:
				if (Screen.hasControlDown()) {
					var pos = this.getNextWord();
					this.deleteTextInternal(pos.beginIndex - this.cursor);
				} else {
					this.deleteTextInternal(1);
				}
				return true;
			case InputConstants.KEY_RIGHT:
				if (Screen.hasControlDown()) {
					var pos = this.getNextWord();
					this.seekCursor(Whence.ABSOLUTE, pos.beginIndex);
				} else {
					this.seekCursor(Whence.RELATIVE, 1);
				}
				return true;
			case InputConstants.KEY_LEFT:
				if (Screen.hasControlDown()) {
					var pos = this.getPreviousWord();
					this.seekCursor(Whence.ABSOLUTE, pos.beginIndex);
				} else {
					this.seekCursor(Whence.RELATIVE, -1);
				}
				return true;
			case InputConstants.KEY_DOWN:
				if (!Screen.hasControlDown()) {
					this.seekCursorLine(1);
				}
				return true;
			case InputConstants.KEY_UP:
				if (!Screen.hasControlDown()) {
					this.seekCursorLine(-1);
				}
				return true;
			case InputConstants.KEY_PAGEUP:
				this.seekCursor(Whence.ABSOLUTE, 0);
				return true;
			case InputConstants.KEY_PAGEDOWN:
				this.seekCursor(Whence.END, 0);
				return true;
			case InputConstants.KEY_HOME:
				if (Screen.hasControlDown()) {
					this.seekCursor(Whence.ABSOLUTE, 0);
				} else {
					this.seekCursor(Whence.ABSOLUTE, this.getDisplayLineAt(this.cursor).beginIndex);
				}
				return true;
			case InputConstants.KEY_END:
				if (Screen.hasControlDown()) {
					this.seekCursor(Whence.END, 0);
				} else {
					this.seekCursor(Whence.ABSOLUTE, this.getDisplayLineAt(this.cursor).endIndex);
				}
				return true;
			default:
				return false;
			}
		}
	}
	
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (this.isActive() && this.isFocused() && StringUtil.isAllowedChatCharacter(codePoint)) {
			this.insertTextInternal(Character.toString(codePoint));
			return true;
		}
		return false;
	}
	
	@Override
	public void miniMessageTick() {
		if (!this.isActive() || !this.isFocused()) return;
		if (this.activeMiniMessage != null) {
			this.activeMiniMessage.tick();
		}
	}
	
	@Override
	public boolean miniMessageMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!this.isActive() || !this.isFocused()) return false;
		if (this.activeMiniMessage != null) {
			if (this.activeMiniMessage.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		}
		return false;
	}
	
	@Override
	public boolean miniMessageMouseClicked(double mouseX, double mouseY, int button) {
		if (!this.isActive() || !this.isFocused()) return false;
		if (this.activeMiniMessage != null) {
			if (this.activeMiniMessage.mouseClicked(mouseX, mouseY, button)) return true;
		}
		return false;
	}
	
	@Override
	public void miniMessageRender(GuiGraphics gui, int mouseX, int mouseY) {
		if (this.isActive() && this.isFocused() && this.activeMiniMessage != null) {
			this.activeMiniMessage.renderPreviewOrError(gui);
			this.activeMiniMessage.renderSuggestions(gui, mouseX, mouseY);
			this.activeMiniMessage.renderHover(gui, mouseX, mouseY);
		}
		int y = this.getY() + this.innerPadding();
		var formatLine = this.formatLines.get(0);
		int lineCounter = 0;
		for (var line : this.displayLines) {
			if (formatLine.line.endIndex < line.beginIndex) {
				formatLine = this.formatLines.get(++lineCounter);
			}
			if (this.inVerticalBounds(y, y + 9)) {
				formatLine.minimessage.renderFormatHover(gui, mouseX, mouseY, line.beginIndex, line.endIndex);
			}
			y += 9;
		}
	}
	
	public void seekCursor(Whence whence, int pos) {
		final var oldLine = this.getFormatLineAt(this.cursor);
		switch (whence) {
		case ABSOLUTE:
			this.cursor = pos;
			break;
		case RELATIVE:
			this.cursor = Util.offsetByCodepoints(this.value, this.cursor, pos);
			break;
		case END:
			this.cursor = this.value.length() + pos;
			break;
		}
		this.cursor = Mth.clamp(this.cursor, 0, this.value.length());
		this.scrollToCursor();
		final var newLine = this.getFormatLineAt(this.cursor);
		if (oldLine != newLine) {
			oldLine.setActive(oldLine.isActive());
		}
		newLine.cursorMoved();
		if (!this.selecting) {
			this.selectCursor = this.cursor;
		}
	}
	
	public void seekCursorLine(int offset) {
		if (offset != 0) {
			int maxX = this.font.width(this.value.substring(this.getDisplayLineAt(this.cursor).beginIndex, this.cursor)) + 2;
			var cursorLineNum = this.getDisplayLineNumberAt(this.cursor);
			var targetLine = this.getDisplayLineByNumber(cursorLineNum + offset);
			int linePos = this.font.plainSubstrByWidth(this.value.substring(targetLine.beginIndex, targetLine.endIndex), maxX).length();
			this.seekCursor(Whence.ABSOLUTE, targetLine.beginIndex + linePos);
		}
	}
	
	public void seekCursorToPoint(double x, double y) {
		int row = Mth.floor(x);
		int col = Mth.floor(y / 9.0);
		var line = this.getDisplayLineByNumber(col);
		int offset = this.font.plainSubstrByWidth(this.value.substring(line.beginIndex, line.endIndex), row).length();
		this.seekCursor(Whence.ABSOLUTE, line.beginIndex + offset);
	}
	
	@Override
	public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (this.visible) {
			this.renderBackground(gui);
			final int borderThickness = this.theme.borderThickness;
			gui.enableScissor(this.getX() + borderThickness, this.getY() + borderThickness, this.getX() + this.width - borderThickness, this.getY() + this.height - borderThickness);
			gui.pose().pushPose();
			gui.pose().translate(0, -this.scrollAmount, 0);
			// wrapping this in a try catch to gather more info when it crashes because I don't trust this code
			try {
				this.renderContents(gui, mouseX, mouseY, partialTick);
			} catch (Exception e) {
				BuildersInventory.LOGGER.error("MultiLineMMEditBox.renderContents is about to crash. Printing report.");
				BuildersInventory.LOGGER.error("cursor=" + this.cursor);
				BuildersInventory.LOGGER.error("value=" + this.value);
				throw e;
			}
			gui.pose().popPose();
			gui.disableScissor();
			if (this.scrollBarVisible()) {
				int scrollBarHeight = this.getScrollBarHeight();
				final int x = this.getX() + this.width - this.scrollbarPadding + this.theme.scrollbarPadding;
				final int y = Math.max(
						this.getY() + this.innerPadding(),
						this.getY() + this.innerPadding() + (int)this.scrollAmount * (this.height - this.totalVerticalPadding() - scrollBarHeight) / this.getMaxScrollAmount());
				gui.blitSprite(RenderType::guiTextured, this.theme.spritesScrollbar.get(this.isActive(), this.isFocused()), x, y, 1, this.theme.scrollbarWidth, scrollBarHeight);
			}
			if (this.hasMaxLength()) {
				int charLimit = this.maxLength;
				Component error = Component.translatable("gui.multiLineEditBox.character_limit", this.value.length(), charLimit);
				gui.drawString(this.font, error, this.getX() + this.width - this.font.width(error), this.getY() + this.height + 4, 0xFFA0A0A0);
			}
		}
	}
	
	private void renderBackground(GuiGraphics gui) {
		ResourceLocation background = this.theme.spritesBackground.get(this.isActive(), this.isFocused());
		gui.blitSprite(RenderType::guiTextured, background, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		final int borderThickness = this.theme.borderThickness;
		if (this.showLineNumbers) gui.fill(this.getX() + borderThickness, this.getY() + borderThickness, this.getX() + this.lineNumWidth + borderThickness, this.getY() + this.getHeight() - borderThickness, this.theme.lineNumBackgroundColor);
	}
	
	private void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		String str = this.value;
		if (!str.isEmpty() || this.isFocused()) {
			final int cursor = this.cursor;
			final int textColor = this.isActive() ? this.theme.textColor : this.theme.disabledTextColor;
			final boolean blink = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L;
			
			int y = this.getY() + this.innerPadding();
			var formatLine = this.formatLines.get(0);
			var formatPos = new StringPos(-1, -1);
			int lineCounter = 0;
			for (var line : this.displayLines) {
				final boolean lineVisible = this.inVerticalBounds(y, y + 9);
				
				int x = this.getX() + this.leftPadding();
				
				// current format line
				if (formatPos.endIndex < line.beginIndex) {
					formatLine = this.formatLines.get(lineCounter++);
					formatPos = formatLine.line;
					// line numbers
					if (lineVisible && this.showLineNumbers) {
						final String lineNumStr = new StringBuilder().append(lineCounter).append("").toString();
						final int strWidth = this.font.width(lineNumStr);
						gui.drawString(this.font, lineNumStr, x - strWidth - 4, y, this.theme.lineNumColor, true);
					}
				}
				
				// line contents
				if (lineVisible) {
					if (blink && cursor >= line.beginIndex && cursor <= line.endIndex) {
						x = gui.drawString(this.font, this.format(formatLine, str, line.beginIndex, cursor), x, y, textColor) - 1;
						
						gui.drawString(this.font, this.format(formatLine, str, cursor, line.endIndex), x, y, textColor);
						
						gui.fill(RenderType.guiOverlay(), x, y, x + 1, y + 9, CURSOR_COLOR);
					} else {
						x = gui.drawString(this.font, this.format(formatLine, str, line.beginIndex, line.endIndex), x, y, textColor);
						
						if (!this.hasSelection() && cursor >= line.beginIndex && cursor == line.endIndex && line.endIndex == formatPos.endIndex) {
							if (this.suggestion != null) {
								gui.drawString(this.font, this.suggestion, x - 1, y, this.theme.suggestionColor);
							}
							
							if (blink) {
								if (this.cursor == line.beginIndex) x -= 1;
								gui.drawString(this.font, "_", x, y, textColor);
							}
						}
					}
				}
				y += 9;
			}
			
			if (this.hasSelection()) {
				var selection = this.getSelected();
				int xMin = this.getX() + this.leftPadding();
				y = this.getY() + this.innerPadding();
				
				for (var line : this.displayLines) {
					if (selection.beginIndex > line.endIndex) {
						y += 9;
					} else {
						if (line.beginIndex > selection.endIndex) break;
						if (this.inVerticalBounds(y, y + 9)) {
							int xStart = this.font.width(str.substring(line.beginIndex, Math.max(selection.beginIndex, line.beginIndex)));
							int xEnd;
							if (selection.endIndex > line.endIndex) {
								xEnd = this.width - this.totalHorizontalPadding();
							} else {
								xEnd = this.font.width(str.substring(line.beginIndex, selection.endIndex));
							}
							
							gui.fill(RenderType.guiTextHighlight(), xMin + xStart, y, xMin + xEnd, y + 9, 0xFF0000FF);
						}
						y += 9;
					}
				}
			}
		} else if (str.isEmpty() && this.showLineNumbers) {
			int x = this.getX() + this.leftPadding();
			int y = this.getY() + this.innerPadding();
			final String lineNumStr = "1";
			final int strWidth = this.font.width(lineNumStr);
			gui.drawString(this.font, lineNumStr, x - strWidth - 4, y, this.theme.lineNumColor);
		}
	}
	
	private FormattedCharSequence format(FormatLine line, String str, int beginIndex, int endIndex) {
		if (line.minimessage.canFormat()) {
			return line.minimessage.format(beginIndex - line.line.beginIndex, endIndex - line.line.beginIndex);
		} else {
			return FormattedCharSequence.forward(str.substring(beginIndex, endIndex), Style.EMPTY);
		}
	}
	
	@Override
	protected MutableComponent createNarrationMessage() {
		return Component.translatable("gui.narrate.editBox", this.getMessage(), this.value);
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
	}
	
	private void scrollToCursor() {
		double scroll = this.scrollAmount;
		var currentLine = this.getDisplayLineByNumber((int)(scroll / 9.0));
		if (this.cursor <= currentLine.beginIndex) {
			scroll = this.getDisplayLineNumberAt(this.cursor) * 9;
		} else {
			var line = this.getDisplayLineByNumber((int)((scroll + this.height) / 9.0) - 1);
			if (this.cursor >= line.endIndex) {
				scroll = this.getDisplayLineNumberAt(this.cursor) * 9 - this.height + 9 + this.totalVerticalPadding();
			}
		}
		
		this.setScrollAmount(scroll);
	}
	
	private double getDisplayableLineCount() {
		return (this.height - this.totalVerticalPadding()) / 9.0;
	}
	
	private void seekCursorScreen(double mouseX, double mouseY) {
		double x = mouseX - this.getX() - this.leftPadding();
		double y = mouseY - this.getY() - this.innerPadding() + this.scrollAmount;
		this.seekCursorToPoint(x, y);
	}
	
	public StringPos getPreviousWord() {
		if (this.value.isEmpty()) {
			return StringPos.EMPTY;
		} else {
			int cursor = Mth.clamp(this.cursor, 0, this.value.length() - 1);
			
			while (cursor > 0 && Character.isWhitespace(this.value.charAt(cursor - 1))) cursor--;
			while (cursor > 0 && !Character.isWhitespace(this.value.charAt(cursor - 1))) cursor--;
			
			return new StringPos(cursor, this.getWordEndPosition(cursor));
		}
	}
	
	public StringPos getNextWord() {
		if (this.value.isEmpty()) {
			return StringPos.EMPTY;
		} else {
			int cursor = Mth.clamp(this.cursor, 0, this.value.length() - 1);
			
			while (cursor < this.value.length() && Character.isWhitespace(this.value.charAt(cursor))) cursor++;
			while (cursor < this.value.length() && !Character.isWhitespace(this.value.charAt(cursor))) cursor++;
			
			return new StringPos(cursor, this.getWordEndPosition(cursor));
		}
	}
	
	private int getWordEndPosition(int cursor) {
		while (cursor < this.value.length() && !Character.isWhitespace(this.value.charAt(cursor))) cursor++;
		return cursor;
	}
	
	private void reflowDisplayLines() {
		this.displayLines.clear();
		if (this.value.isEmpty()) {
			this.displayLines.add(StringPos.EMPTY);
		} else {
			this.font.getSplitter().splitLines(
					this.value,
					this.getInnerWidth(),
					Style.EMPTY,
					false,
					(style, start, end) -> this.displayLines.add(new StringPos(start, end)));
			if (this.value.charAt(this.value.length() - 1) == '\n') {
				this.displayLines.add(new StringPos(this.value.length(), this.value.length()));
			}
		}
		this.refreshScrollbar();
	}
	
	private ArrayList<FormatLine> refreshFormatLines() {
		final var initLines = new ArrayList<FormatLine>();
		
		this.linesResetListener.run();
		this.formatLines.clear();
		final String value = this.value;
		if (value.isEmpty()) {
			var line = formatLine(StringPos.EMPTY, initLines.size());
			this.formatLines.add(line);
			initLines.add(line);
		} else {
			int i = 0;
			int start = 0;
			boolean endsOnNewline = false;
			while (i < value.length()) {
				if (value.charAt(i) == '\n') {
					var line = formatLine(new StringPos(start, i), initLines.size());
					this.formatLines.add(line);
					initLines.add(line);
					start = ++i;
					endsOnNewline = true;
					continue;
				}
				endsOnNewline = false;
				i++;
			}
			if (endsOnNewline || start < i) {
				var line = formatLine(new StringPos(start, i), initLines.size());
				this.formatLines.add(line);
				initLines.add(line);
			}
		}
		this.refreshLineNumWidth();
		
		return initLines;
	}
	
	private ArrayList<FormatLine> refreshFormatRange(int beginIndex, int endIndexOld, int endIndexNew) {
		final var initLines = new ArrayList<FormatLine>();
		// find first line to replace
		int lineNum = 0;
		while (lineNum < this.formatLines.size()) {
			var line = this.formatLines.get(lineNum).line;
			if (beginIndex >= line.beginIndex && beginIndex <= line.endIndex) break;
			lineNum++;
		}
//		if (lineNum >= this.formatLines.size()) lineNum = this.formatLines.size() - 1;
		
		// count lines being replaced
		int targets = 0;
		while (lineNum + targets < this.formatLines.size()) {
			var line = this.formatLines.get(lineNum + targets).line;
			targets++;
			if (line.endIndex >= endIndexOld) break;
		}
//		if (lineNum + targets >= this.formatLines.size()) targets = this.formatLines.size() - lineNum - 1;
		
		// determine new lines
		ArrayList<StringPos> newLines = new ArrayList<>();
		final int beginLineIndex = this.formatLines.get(lineNum).line.beginIndex;
//		final int endLineIndex = this.formatLines.get(lineNum + targets - 1).line.endIndex;
		{
			final String value = this.value;
			int i = beginLineIndex;
			int start = i;
			boolean endsOnNewline = false;
			boolean breakAtNextLine = false;
			while (i < value.length()) {
				if (i >= endIndexNew) {
					endsOnNewline = false;
					breakAtNextLine = true;
				}
				if (value.charAt(i) == '\n') {
					newLines.add(new StringPos(start, i));
					start = ++i;
					if (breakAtNextLine) break;
					endsOnNewline = true;
					continue;
				}
				endsOnNewline = false;
				i++;
			}
			if (endsOnNewline || start < i) {
				newLines.add(new StringPos(start, i));
			}
		}
		if (newLines.isEmpty()) {
			newLines.add(new StringPos(beginIndex, endIndexNew));
		}
		
		// add lines to fit list size
		if (newLines.size() < targets) {
			// delete excess targets
			int i = targets;
			while (i > newLines.size()) { // while loop works better than for loop here
				final int deleteIndex = lineNum + --i;
				this.lineDeleteListener.accept(deleteIndex);
				this.formatLines.remove(deleteIndex);
			}
			targets = newLines.size();
		} else { // newLines.size() >= targets
			// add excess new lines
			for (int i = targets; i < newLines.size(); i++) {
				final int createIndex = lineNum + i;
				var line = formatLine(newLines.get(i), createIndex);
				this.formatLines.add(createIndex, line);
				initLines.add(line);
			}
		}
		
		// replace lines
		for (int i = 0; i < targets; i++) {
			var line = this.formatLines.get(lineNum + i);
			line.line = newLines.get(i);
			initLines.add(line);
		}
		
		// shift all lines after
		final int lineShift = endIndexNew - endIndexOld;
		final int lineIndexShift = newLines.size() - targets;
		for (int i = lineNum + newLines.size(); i < this.formatLines.size(); i++) {
			var line = this.formatLines.get(i);
			line.line = line.line.shift(lineShift);
			line.lineIndex += lineIndexShift;
		}
		
		// refresh line number offset
		this.refreshLineNumWidth();
		
		// return
		return initLines;
	}
	
	private void refreshLineNumWidth() {
		if (this.showLineNumbers) {
			final int maxDigits = (int)(Math.log10(this.formatLines.size()) + 1);
			this.lineNumWidth = (6 * maxDigits) + 3;
		} else {
			this.lineNumWidth = 0;
		}
	}
	
	private StringPos getDisplayLineAt(int pos) {
		int lineCounter = 0;
		int lineCounterEnd = -1;
		for (int i = 0; i < this.displayLines.size(); i++) {
			var line = this.displayLines.get(i);
			if (lineCounterEnd < line.beginIndex) {
				lineCounterEnd = this.formatLines.get(lineCounter++).line.endIndex;
			}
			final var lineEndIndex = line.endIndex;
			if (pos >= line.beginIndex && (pos < lineEndIndex || (pos == lineEndIndex && lineEndIndex == lineCounterEnd))) {
				return line;
			}
		}
		return this.displayLines.get(this.displayLines.size() - 1);
	}
	
	private int getDisplayLineNumberAt(int pos) {
		int lineCounter = 0;
		int lineCounterEnd = -1;
		for (int i = 0; i < this.displayLines.size(); i++) {
			var line = this.displayLines.get(i);
			if (lineCounterEnd < line.beginIndex) {
				lineCounterEnd = this.formatLines.get(lineCounter++).line.endIndex;
			}
			final var lineEndIndex = line.endIndex;
			if (pos >= line.beginIndex && (pos < lineEndIndex || (pos == lineEndIndex && lineEndIndex == lineCounterEnd))) {
				return i;
			}
		}
		return this.displayLines.size() - 1;
	}
	
	private StringPos getDisplayLineByNumber(int line) {
		return this.displayLines.get(Mth.clamp(line, 0, this.displayLines.size() - 1));
	}
	
	private FormatLine getFormatLineAt(int pos) {
		for (int i = 0; i < this.formatLines.size(); i++) {
			var line = this.formatLines.get(i);
			if (pos >= line.line.beginIndex && pos <= line.line.endIndex) return line;
		}
		return this.formatLines.get(this.formatLines.size() - 1);
	}
	
//	private int getFormatLineNumberAt(int pos) {
//		for (int i = 0; i < this.formatLines.size(); i++) {
//			var line = this.formatLines.get(i).line;
//			if (pos >= line.beginIndex && pos <= line.endIndex) return i;
//		}
//		return this.formatLines.size() - 1;
//	}
//	
//	private FormattedLine getFormatLineByNumber(int line) {
//		return this.formatLines.get(Mth.clamp(line, 0, this.formatLines.size() - 1));
//	}
	
	@Override
	public void setFocused(boolean focused) {
		if (focused) {
			for (var line : this.formatLines) line.setActive(true);
		} else {
			for (var line : this.formatLines) line.setActive(false);
		}
		super.setFocused(focused);
		if (focused) this.focusedTime = Util.getMillis();
	}
	
	protected boolean inVerticalBounds(int top, int bottom) {
		return bottom - this.scrollAmount >= this.getY() + this.innerPadding() && top - this.scrollAmount <= this.getY() + this.height - this.innerPadding();
	}
	
	protected boolean inBounds(double x, double y) {
		return x >= this.getX() && x < this.getX() + this.width - this.scrollbarPadding && y >= this.getY() && y < this.getY() + this.getHeight();
	}
	
	public int innerPadding() {
		return this.theme.innerPadding;
	}
	
	public int leftPadding() {
		return this.innerPadding() + this.lineNumWidth;
	}
	
	public int rightPadding() {
		return this.rightPadding;
	}
	
	public int totalHorizontalPadding() {
		return this.leftPadding() + this.rightPadding();
	}
	
	public int totalVerticalPadding() {
		return innerPadding() * 2;
	}
	
	public double scrollAmount() {
		return this.scrollAmount;
	}
	
	public void setScrollAmount(double scroll) {
		this.scrollAmount = Mth.clamp(scroll, 0.0, this.getMaxScrollAmount());
	}
	
	private int getScrollBarHeight() {
		return this.scrollbarHeight;
	}
	
	private void refreshScrollbar() {
		if (this.externalScrollbar) {
			this.scrollbarPadding = 0;
			this.rightPadding = this.innerPadding();
		} else {
			this.scrollbarPadding = this.theme.scrollbarWidth + this.theme.scrollbarPadding * 2;
			if (this.theme.scrollbarPadding < this.theme.borderThickness) this.scrollbarPadding += this.theme.borderThickness - this.theme.scrollbarPadding;
			this.rightPadding = this.scrollbarPadding;
		}
		final int scrollbarEdgeHeight = this.theme.scrollbarEdgeHeight;
		final int scrollbarScale = this.theme.scrollbarScale;
		final int innerViewHeight = this.height - this.totalVerticalPadding();
		final int minScrollbarHeight = Math.max(Mth.positiveCeilDiv(innerViewHeight, 6), scrollbarEdgeHeight + scrollbarScale);
		int scrollbarHeight = (int)((innerViewHeight * innerViewHeight) / (float)this.getInnerHeight());
		scrollbarHeight = Mth.clamp(scrollbarHeight, minScrollbarHeight, this.height - this.totalVerticalPadding() - 1);
		scrollbarHeight = Math.round((float)(scrollbarHeight - scrollbarEdgeHeight) / scrollbarScale) * scrollbarScale;
		if (scrollbarHeight < minScrollbarHeight - scrollbarEdgeHeight) scrollbarHeight += scrollbarScale;
		if (scrollbarHeight > this.height - this.totalVerticalPadding() - Math.max(scrollbarEdgeHeight, 1)) scrollbarHeight -= scrollbarScale;
		this.scrollbarHeight = scrollbarHeight + scrollbarEdgeHeight;
	}
	
	public int getMaxScrollAmount() {
		return Math.max(0, this.getInnerHeight() + this.totalVerticalPadding() - this.height);
	}
	
	public boolean scrollBarVisible() {
		return this.getLineCount() > this.getDisplayableLineCount();
	}
	
	public int scrollBarWidth() {
		return this.theme.scrollbarWidth + this.theme.scrollbarPadding * 2;
	}
	
	public int getInnerWidth() {
		return this.width - this.totalHorizontalPadding();
	}
	
	public int getInnerHeight() {
		return 9 * this.getLineCount();
	}
	
	@FunctionalInterface
	public static interface FormatLineConsumer {
		public static final FormatLineConsumer IGNORE = (line, lastParse) -> {};
		public void onParseChange(int line, @Nullable MiniMessageResult lastParse);
	}
	
	private FormatLine formatLine(StringPos line, int lineIndex) {
		this.lineCreateListener.accept(lineIndex);
		return new FormatLine(this, line, lineIndex);
	}
	
	private static class FormatLine implements WrappedTextField, MiniMessageParseListener {
		
		private final MultiLineMMEditBox input;
		private final MiniMessageInstance minimessage;
		
		private StringPos line;
		private int lineIndex;
		
		public FormatLine(MultiLineMMEditBox input, StringPos line, int lineIndex) {
			this.input = input;
			this.line = line;
			this.lineIndex = lineIndex;
			this.minimessage = input.mmConstructor.construct(this, this);
		}
		
		@Override
		public void onParseChange(@Nullable MiniMessageResult parseResult) {
			this.input.lineEditListener.onParseChange(this.lineIndex, parseResult);
		}
		
		public boolean isActive() {
			return input.cursor >= line.beginIndex && input.cursor <= line.endIndex;
		}
		
//		public void updateActive() {
//			boolean active = this.isActive();
//			this.widget.setActiveNoUpdate(active);
//		}
		
//		public void unknownEdit() {
//			this.updateActive();
//			this.widget.unknownEdit();
//		}
		
		public void cursorMoved() {
			boolean active = this.isActive();
			this.minimessage.setActive(active);
			if (active) {
				this.input.activeMiniMessage = this.minimessage;
				this.minimessage.cursorMoved();
			} else {
				this.input.suggestion = null;
			}
		}
		
//		public void inputEdited() {
//			this.updateActive();
//			this.widget.inputEdited();
//		}
		
		public void initUpdate() {
			boolean active = this.isActive();
			this.minimessage.setActive(active);
			if (active) {
				this.input.activeMiniMessage = this.minimessage;
				this.minimessage.unknownEdit();
			} else {
				this.minimessage.quietUpdate();
			}
		}
		
		public void setActive(boolean active) {
			this.minimessage.setActive(active);
			if (active) {
				this.input.activeMiniMessage = this.minimessage;
				this.minimessage.unknownEdit();
			} else {
				this.input.suggestion = null;
			}
		}
		
		@Override
		public String getValue() {
			return this.input.getText(this.line);
		}
		
		@Override
		public void setValue(String str) {
			this.input.setSelected(line.beginIndex, line.endIndex);
			this.input.insertTextInternal(str);
		}
		
		@Override
		public void setSuggestion(@Nullable String str) {
			this.input.suggestion = str;
		}
		
		@Override
		public int getCursorPosition() {
			return this.input.cursor - this.line.beginIndex;
		}
		
		@Override
		public void setCursorPosition(int pos) {
			this.input.selecting = false;
			this.input.seekCursor(Whence.ABSOLUTE, pos + this.line.beginIndex);
		}
		
		@Override
		public void setHighlightPos(int pos) {
			this.input.selectCursor = pos + this.line.beginIndex;
		}
		
		@Override
		public int getTextX() {
			return this.input.getX() + this.input.leftPadding();
		}
		
		@Override
		public int getScreenX(int charNum) {
			final String value = this.getValue();
			if (charNum > value.length()) {
				return this.getTextX();
			} else {
				final int endPos = this.line.beginIndex + charNum;
				final var dispLine = this.input.getDisplayLineAt(endPos);
				final String lineText = this.input.value.substring(dispLine.beginIndex, endPos);
				return this.getTextX() + this.input.font.width(lineText);
			}
		}
		
		@Override
		public int getY() {
			return this.input.getY();
		}
		
		@Override
		public int getTextY(int pos) {
			final int lineNum = this.input.getDisplayLineNumberAt(pos);
			return this.input.getY() + this.input.innerPadding() + (lineNum * 9) - (int)this.input.scrollAmount;
		}
		
		@Override
		public int getInnerWidth() {
			return this.input.getInnerWidth();
		}
		
		@Override
		public int getHeight() {
			return this.input.getHeight();
		}
		
		@Override
		public int getLineHeight() {
			return 9;
		}
		
	}
	
}
