package _3650.builders_inventory.api.minimessage.format;

import java.util.Optional;
import java.util.UUID;

import _3650.builders_inventory.api.minimessage.tags.Node;
import _3650.builders_inventory.mixin.feature.minimessage.HoverEvent_ItemStackInfoInvoker;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class HoverFormat extends Format {
	
	public final HoverContents<?> contents;
	
	public HoverFormat(String argString, String tag, HoverContents<?> contents) {
		super(argString, tag);
		this.contents = contents;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withHoverEvent(contents.event()));
	}
	
	public static TextContents text(Node contents) {
		return new TextContents(contents);
	}
	
	public static ItemContents item(Item item, int count, DataComponentPatch components) {
		@SuppressWarnings("deprecation")
		var itemHolder = item.builtInRegistryHolder();
		return new ItemContents(HoverEvent_ItemStackInfoInvoker.construct(itemHolder, count, components));
	}
	
	public static EntityContents entity(EntityType<?> type, UUID id) {
		return new EntityContents(type, id, Optional.empty());
	}
	
	public static EntityContents entity(EntityType<?> type, UUID id, Node name) {
		return new EntityContents(type, id, Optional.of(name));
	}
	
	public static abstract class HoverContents<T> {
		public final HoverEvent.Action<T> type;
		
		protected HoverContents(HoverEvent.Action<T> type) {
			this.type = type;
		}
		public HoverEvent event() {
			return new HoverEvent(this.type, this.build());
		}
		protected abstract T build();
	}
	
	private static class TextContents extends HoverContents<Component> {
		public final Node contents;
		private TextContents(Node contents) {
			super(HoverEvent.Action.SHOW_TEXT);
			this.contents = contents;
		}
		@Override
		protected Component build() {
			return this.contents.getFormatted();
		}
	}
	
	private static class ItemContents extends HoverContents<HoverEvent.ItemStackInfo> {
		public final HoverEvent.ItemStackInfo contents;
		private ItemContents(HoverEvent.ItemStackInfo contents) {
			super(HoverEvent.Action.SHOW_ITEM);
			this.contents = contents;
		}
		@Override
		protected HoverEvent.ItemStackInfo build() {
			return this.contents;
		}
	}
	
	private static class EntityContents extends HoverContents<HoverEvent.EntityTooltipInfo> {
		public final EntityType<?> type;
		public final UUID id;
		public final Optional<Node> name;
		private EntityContents(EntityType<?> type, UUID id, Optional<Node> name) {
			super(HoverEvent.Action.SHOW_ENTITY);
			this.type = type;
			this.id = id;
			this.name = name;
		}
		@Override
		protected HoverEvent.EntityTooltipInfo build() {
			return new HoverEvent.EntityTooltipInfo(this.type, this.id, this.name.map(Node::getFormatted));
		}
	}
	
}