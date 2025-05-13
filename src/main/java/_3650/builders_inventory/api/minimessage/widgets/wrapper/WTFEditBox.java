package _3650.builders_inventory.api.minimessage.widgets.wrapper;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.components.EditBox;

class WTFEditBox implements WrappedTextField {
	
	private final EditBox input;
	
	WTFEditBox(EditBox input) {
		this.input = input;
	}
	
	@Override
	public String getValue() {
		return input.getValue();
	}
	
	@Override
	public void setValue(String str) {
		input.setValue(str);
	}
	
	@Override
	public void setSuggestion(@Nullable String str) {
		input.setSuggestion(str);
	}
	
	@Override
	public int getCursorPosition() {
		return input.getCursorPosition();
	}
	
	@Override
	public void setCursorPosition(int pos) {
		input.setCursorPosition(pos);
	}
	
	@Override
	public void setHighlightPos(int pos) {
		input.setHighlightPos(pos);
	}
	
	@Override
	public int getTextX() {
		return input.isBordered() ? input.getX() + 4 : input.getX();
	}
	
	@Override
	public int getScreenX(int charNum) {
		final int screenX = input.getScreenX(charNum);
		return input.isBordered() ? screenX + 4 : screenX;
	}
	
	@Override
	public int getY() {
		return input.getY();
	}
	
	@Override
	public int getTextY(int pos) {
		return input.isBordered() ? input.getY() + (input.getHeight() - 8) / 2 : input.getY();
	}
	
	@Override
	public int getInnerWidth() {
		return input.getInnerWidth();
	}
	
	@Override
	public int getHeight() {
		return input.getHeight();
	}
	
	@Override
	public int getLineHeight() {
		return input.isBordered() ? 8 : input.getHeight();
	}
	
}
