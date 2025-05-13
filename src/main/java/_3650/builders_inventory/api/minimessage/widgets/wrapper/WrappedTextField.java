package _3650.builders_inventory.api.minimessage.widgets.wrapper;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.components.EditBox;

// acronym isnt intentional i swear
public interface WrappedTextField {
	
	public static WrappedTextField editBox(EditBox input) {
		return new WTFEditBox(input);
	}
	
	public String getValue();
	
	public void setValue(String str);
	
	public void setSuggestion(@Nullable String str);
	
	public int getCursorPosition();
	
	public void setCursorPosition(int pos);
	
	public void setHighlightPos(int pos);
	
	public int getTextX();
	
	public int getScreenX(int charNum);
	
	public int getY();
	
	public int getTextY(int pos);
	
	public int getInnerWidth();
	
	public int getHeight();
	
	public int getLineHeight();
	
}