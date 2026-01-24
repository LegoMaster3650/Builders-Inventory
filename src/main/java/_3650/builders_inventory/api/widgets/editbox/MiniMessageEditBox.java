package _3650.builders_inventory.api.widgets.editbox;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import _3650.builders_inventory.api.minimessage.MiniMessageUtil;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageParseListener;
import _3650.builders_inventory.api.minimessage.instance.MMInstanceConstructor;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance;
import _3650.builders_inventory.api.minimessage.widgets.MiniMessageEventListener;
import _3650.builders_inventory.api.minimessage.widgets.wrapper.WrappedTextField;
import _3650.builders_inventory.mixin.feature.minimessage.EditBoxAccessor;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * @deprecated This widget is not currently used or maintained and may be removed without warning or may be fixed and un-deprecated without warning<br>
 * An {@link EditBox} with a {@link MiniMessageInstance} mostly handled within<br>
 * You still need to handle calling the following:<br>
 * {@link #miniMessageTick()}<br>
 * {@link #miniMessageMouseScrolled(double, double, double, double)}<br>
 * {@link #miniMessageMouseClicked(double, double, int)}<br>
 * These are either not possible in a widget or occur outside the widget's area and need full-screen coverage
 */
@Deprecated
public class MiniMessageEditBox extends EditBox implements MiniMessageEventListener {
	
	private Consumer<String> newResponder;
	
	public final MiniMessageInstance minimessage;
	
	public MiniMessageEditBox(MMInstanceConstructor widget, Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
		this.minimessage = widget.construct(WrappedTextField.editBox(this), MiniMessageParseListener.IGNORE);
		this.initMiniMessage();
	}
	
	public MiniMessageEditBox(MMInstanceConstructor widget, Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message) {
		super(font, x, y, width, height, editBox, message);
		this.minimessage = widget.construct(WrappedTextField.editBox(this), MiniMessageParseListener.IGNORE);
		this.initMiniMessage();
	}
	
	private void initMiniMessage() {
		super.setResponder(this::responder);
		MiniMessageUtil.addFormatter(this, this.minimessage);
	}
	
	@Override
	public void setResponder(Consumer<String> newResponder) {
		this.newResponder = newResponder;
	}
	
	private void responder(String str) {
		if (!this.minimessage.unknownEdit() && this.newResponder != null) this.newResponder.accept(str);
	}
	
	@Override
	public void setFocused(boolean focused) {
		if (((EditBoxAccessor)this).getCanLoseFocus() || focused) {
			this.minimessage.setActive(focused);
			if (focused) {
				this.minimessage.unknownEdit();
			} else {
				this.setSuggestion(null);
			}
		}
		super.setFocused(focused);
	}
	
	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.minimessage.keyPressed(event)) return true;
		return super.keyPressed(event);
	}
	
	@Override
	public void miniMessageTick() {
		this.minimessage.tick();
	}
	
	@Override
	public boolean miniMessageMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return this.minimessage.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	
	@Override
	public boolean miniMessageMouseClicked(MouseButtonEvent event) {
		return this.minimessage.mouseClicked(event);
	}
	
	@Override
	public void miniMessageRender(GuiGraphics gui, int mouseX, int mouseY) {
		if (this.isActive() && this.isFocused()) {
			ActiveTextCollector text = gui.textRenderer(GuiGraphics.HoveredTextEffects.TOOLTIP_AND_CURSOR);
			ActiveTextCollector.Parameters parameters = text.defaultParameters();
			
			this.minimessage.renderPreviewOrError(gui, text, parameters);
			if (this.minimessage.canFormat()) {
				var inputAccess = ((EditBoxAccessor)this);
				var formattedInput = this.minimessage.format(inputAccess.getDisplayPos(), this.getValue().length());
				// see mixin.feature.minimessage.screens.ChatScreenMixin
				text.accept(TextAlignment.LEFT, inputAccess.getTextX(), inputAccess.getTextY(), parameters.withOpacity(0f), formattedInput);
			}
			
			this.minimessage.renderSuggestions(gui, mouseX, mouseY);
		}
	}
	
}
