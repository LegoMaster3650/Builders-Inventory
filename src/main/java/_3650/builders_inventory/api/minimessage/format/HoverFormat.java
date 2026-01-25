package _3650.builders_inventory.api.minimessage.format;

import java.util.Optional;
import java.util.UUID;

import _3650.builders_inventory.api.minimessage.tags.Node;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HoverFormat extends Format {
	
	public final HoverContents<?> contents;
	
	public HoverFormat(String argString, String tag, HoverContents<?> contents) {
		super(argString, tag);
		this.contents = contents;
	}
	
	@Override
	public MutableComponent format(MutableComponent component) {
		return component.setStyle(component.getStyle().withHoverEvent(contents.build()));
	}
	
	public static TextContents text(Node contents) {
		return new TextContents(contents);
	}
	
	public static ItemContents item(Item item, int count, DataComponentPatch components) {
		@SuppressWarnings("deprecation")
		var itemHolder = item.builtInRegistryHolder();
		return new ItemContents(new ItemStack(itemHolder, count, components));
	}
	
	public static EntityContents entity(EntityType<?> type, UUID id) {
		return new EntityContents(type, id, Optional.empty());
	}
	
	public static EntityContents entity(EntityType<?> type, UUID id, Node name) {
		return new EntityContents(type, id, Optional.of(name));
	}
	
	public static abstract class HoverContents<T extends HoverEvent> {
		public final HoverEvent.Action type;
		
		protected HoverContents(HoverEvent.Action type) {
			this.type = type;
		}
		protected abstract T build();
	}
	
	private static class TextContents extends HoverContents<HoverEvent.ShowText> {
		public final Node contents;
		private TextContents(Node contents) {
			super(HoverEvent.Action.SHOW_TEXT);
			this.contents = contents;
		}
		@Override
		protected HoverEvent.ShowText build() {
			return new HoverEvent.ShowText(this.contents.getFormatted());
		}
	}
	
	private static class ItemContents extends HoverContents<HoverEvent.ShowItem> {
		public final HoverEvent.ShowItem contents;
		private ItemContents(ItemStack contents) {
			super(HoverEvent.Action.SHOW_ITEM);
			this.contents = new HoverEvent.ShowItem(contents);
		}
		@Override
		protected HoverEvent.ShowItem build() {
			return this.contents;
		}
	}
	
	private static class EntityContents extends HoverContents<HoverEvent.ShowEntity> {
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
		protected HoverEvent.ShowEntity build() {
			return new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(this.type, this.id, this.name.map(Node::getFormatted)));
		}
	}
	
}