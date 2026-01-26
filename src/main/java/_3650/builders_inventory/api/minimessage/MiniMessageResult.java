package _3650.builders_inventory.api.minimessage;

import java.util.ArrayList;
import java.util.function.Consumer;

import _3650.builders_inventory.api.minimessage.tags.Branch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MiniMessageResult {
	
	final Branch root;
	public final String trailingText;
	public final ArrayList<String> trailingArgs;
	public final ArrayList<String> unclosedTags;
	public final ArrayList<String> warnings;
	public final ArrayList<String> errors;
	
	MiniMessageResult(Branch root, String trailingText, ArrayList<String> trailingArgs, ArrayList<String> unclosedTags, ArrayList<String> warnings, ArrayList<String> errors) {
		this.root = root;
		this.trailingText = trailingText;
		this.trailingArgs = trailingArgs;
		this.unclosedTags = unclosedTags;
		this.warnings = warnings;
		this.errors = errors;
	}
	
	private MutableComponent formatted;
	
	public MutableComponent getFormatted() {
		if (formatted == null) {
			formatted = root.getFormatted();
		}
		return formatted;
	}
	
	private MutableComponent formattedPlainText;
	
	public MutableComponent getFormattedPlainText() {
		if (formattedPlainText == null) {
			formattedPlainText = root.getFormattedPlainText();
		}
		return formattedPlainText;
	}
	
	public void debug(Consumer<Component> output) {
		output.accept(Component.empty());
		output.accept(Component.empty());
		output.accept(Component.literal("DEBUG"));
		output.accept(Component.empty());
		output.accept(this.getFormatted());
		output.accept(this.getFormattedPlainText());
		output.accept(Component.empty());
		output.accept(Component.literal("TRAILING TEXT"));
		output.accept(Component.empty());
		output.accept(Component.literal("->" + trailingText + "<-"));
		output.accept(Component.empty());
		if (trailingArgs.size() > 0) {
			output.accept(Component.empty());
			output.accept(Component.literal("TRAILING ARGS"));
			output.accept(Component.empty());
			for (var arg : trailingArgs) output.accept(Component.literal(String.valueOf(arg)));
			output.accept(Component.empty());
		}
		if (unclosedTags.size() > 0) {
			output.accept(Component.empty());
			output.accept(Component.literal("UNCLOSED TAGS"));
			output.accept(Component.empty());
			for (var tag : unclosedTags) output.accept(Component.literal(String.valueOf(tag)));
			output.accept(Component.empty());
		}
	}
	
}
