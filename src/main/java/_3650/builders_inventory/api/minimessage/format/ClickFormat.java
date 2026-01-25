package _3650.builders_inventory.api.minimessage.format;

import java.net.URI;
import java.util.Optional;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ClickFormat extends Format {
	
	public final ClickContents<?> contents;
	
	public ClickFormat(String argString, String tag, ClickContents<?> contents) {
		super(argString, tag);
		this.contents = contents;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withClickEvent(contents.build()));
	}
	
	public static OpenUrlContents openUrl(URI uri) {
		return new OpenUrlContents(uri);
	}
	
	public static RunCommandContents runCommand(String command) {
		return new RunCommandContents(command);
	}
	
	public static SuggestCommandContents suggestCommand(String command) {
		return new SuggestCommandContents(command);
	}
	
	public static ChangePageContents changePage(int page) {
		return new ChangePageContents(page);
	}
	
	public static CopyToClipboardContents copyToClipboard(String value) {
		return new CopyToClipboardContents(value);
	}
	
	public static CustomContents custom(ResourceLocation id, Optional<Tag> payload) {
		return new CustomContents(id, payload);
	}
	
	public static abstract class ClickContents<T extends ClickEvent> {
		public final ClickEvent.Action type;
		
		protected ClickContents(ClickEvent.Action type) {
			this.type = type;
		}
		protected abstract T build();
	}
	
	private static class OpenUrlContents extends ClickContents<ClickEvent.OpenUrl> {
		public final URI uri;
		private OpenUrlContents(URI uri) {
			super(ClickEvent.Action.OPEN_URL);
			this.uri = uri;
		}
		@Override
		protected ClickEvent.OpenUrl build() {
			return new ClickEvent.OpenUrl(this.uri);
		}
	}
	
	private static class RunCommandContents extends ClickContents<ClickEvent.RunCommand> {
		public final String command;
		private RunCommandContents(String command) {
			super(ClickEvent.Action.RUN_COMMAND);
			this.command = command;
		}
		@Override
		protected ClickEvent.RunCommand build() {
			return new ClickEvent.RunCommand(this.command);
		}
	}
	
	private static class SuggestCommandContents extends ClickContents<ClickEvent.SuggestCommand> {
		public final String command;
		private SuggestCommandContents(String command) {
			super(ClickEvent.Action.SUGGEST_COMMAND);
			this.command = command;
		}
		@Override
		protected ClickEvent.SuggestCommand build() {
			return new ClickEvent.SuggestCommand(this.command);
		}
	}
	
	private static class ChangePageContents extends ClickContents<ClickEvent.ChangePage> {
		public final int page;
		private ChangePageContents(int page) {
			super(ClickEvent.Action.CHANGE_PAGE);
			this.page = page;
		}
		@Override
		protected ClickEvent.ChangePage build() {
			return new ClickEvent.ChangePage(this.page);
		}
	}
	
	private static class CopyToClipboardContents extends ClickContents<ClickEvent.CopyToClipboard> {
		public final String value;
		private CopyToClipboardContents(String value) {
			super(ClickEvent.Action.COPY_TO_CLIPBOARD);
			this.value = value;
		}
		@Override
		protected ClickEvent.CopyToClipboard build() {
			return new ClickEvent.CopyToClipboard(this.value);
		}
	}
	
	private static class CustomContents extends ClickContents<ClickEvent.Custom> {
		public final ResourceLocation id;
		public final Optional<Tag> payload;
		private CustomContents(ResourceLocation id, Optional<Tag> payload) {
			super(ClickEvent.Action.CUSTOM);
			this.id = id;
			this.payload = payload;
		}
		@Override
		protected ClickEvent.Custom build() {
			return new ClickEvent.Custom(this.id, this.payload);
		}
	}
	
}
