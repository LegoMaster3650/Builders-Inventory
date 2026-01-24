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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
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
		MiniMessageUtil.wrapFormatter(this, this.minimessage);
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
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.minimessage.keyPressed(keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
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
	public boolean miniMessageMouseClicked(double mouseX, double mouseY, int button) {
		return this.minimessage.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void miniMessageRender(GuiGraphics gui, int mouseX, int mouseY) {
		if (this.isActive() && this.isFocused()) {
			this.minimessage.renderPreviewOrError(gui);
			this.minimessage.renderSuggestions(gui, mouseX, mouseY);
			this.minimessage.renderHover(gui, mouseX, mouseY);
		}
		this.minimessage.renderFormatHover(gui, mouseX, mouseY, 0, this.getValue().length());
	}
	
}
