package _3650.builders_inventory.mixin.feature.minimessage.screens.chat;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import _3650.builders_inventory.BuildersInventory;
import _3650.builders_inventory.api.minimessage.MiniMessageUtil;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageParseListener;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance.PreviewOptions;
import _3650.builders_inventory.api.minimessage.instance.MiniMessageInstance.SuggestionOptions;
import _3650.builders_inventory.api.minimessage.widgets.wrapper.WrappedTextField;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageButtonGui;
import _3650.builders_inventory.api.widgets.exbutton.ExtendedImageDualButton;
import _3650.builders_inventory.config.Config;
import _3650.builders_inventory.feature.minimessage.chat.ChatMiniMessageContext;
import _3650.builders_inventory.feature.minimessage.chat.ChatMiniMessageButtonDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends ScreenMixinOverrides {
	
	@Unique
	private static final WidgetSprites SPRITES_BUTTON_FORCE = new WidgetSprites(
			BuildersInventory.modLoc("minimessage/button_force"),
			BuildersInventory.modLoc("minimessage/button_force_highlighted"));
	@Unique
	private static final WidgetSprites SPRITES_BUTTON_FORCE_ACTIVE = new WidgetSprites(
			BuildersInventory.modLoc("minimessage/button_force_active"),
			BuildersInventory.modLoc("minimessage/button_force_active_highlighted"));
	
	@Shadow
	protected EditBox input;
	@Shadow
	private String initial;
	@Shadow
	CommandSuggestions commandSuggestions;
	
	@Unique
	private ExtendedImageButtonGui exGui = new ExtendedImageButtonGui();
	@Unique
	private ExtendedImageDualButton buttonForce;
	@Unique
	private boolean resetFocus = false;
	@Unique
	private MiniMessageInstance minimessage;
	
	@Inject(method = "init", at = @At("TAIL"), order = Integer.MAX_VALUE - 1)
	private void builders_inventory_initChat(CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		// load minimessage
		this.minimessage = new MiniMessageInstance(
				this.minecraft,
				(Screen)(Object)this,
				this.font,
				WrappedTextField.editBox(this.input),
				ChatMiniMessageContext.INSTANCE,
				MiniMessageParseListener.IGNORE,
				PreviewOptions.chat(),
				SuggestionOptions.chat(() -> this.commandSuggestions)
				);
		
		// override formatter
		MiniMessageUtil.wrapFormatter(this.input, this.minimessage);
		
		// exGui for button
		this.exGui.init();
		
		// force minimessage button
		final var buttonDisplay = Config.instance().minimessage_chatForceButtonDisplay;
		if (buttonDisplay != ChatMiniMessageButtonDisplay.NONE) {
			// calculate positions (calculations are order-independent)
			final var inputWidth = buttonDisplay.chatWidth(this.input.getWidth());
			final var inputX = buttonDisplay.chatX(this.input.getX());
			final int buttonX = buttonDisplay.buttonX(this.input.getX(), this.input.getWidth());
			
			// resize/move input to make room for button
			this.input.setWidth(inputWidth);
			this.input.setX(inputX);
			
			// make buttons
			this.buttonForce = new ExtendedImageDualButton(buttonX, this.height - 13, 10, 10,
					SPRITES_BUTTON_FORCE,
					button -> {
						if (Screen.hasShiftDown() && Screen.hasControlDown()) ChatMiniMessageContext.loadServerCommandMap();
						else this.updateForce(true);
						this.resetFocus = true;
					},
					List.of(
							Component.translatable("container.builders_inventory.minimessage.tooltip.button.chat_force").withStyle(ChatFormatting.WHITE),
							Component.translatable("container.builders_inventory.minimessage.tooltip.button.chat_force.desc.enable").withStyle(ChatFormatting.GRAY)),
					SPRITES_BUTTON_FORCE_ACTIVE,
					button -> {
						if (Screen.hasShiftDown() && Screen.hasControlDown()) ChatMiniMessageContext.loadServerCommandMap();
						else this.updateForce(false);
						this.resetFocus = true;
					},
					List.of(
							Component.translatable("container.builders_inventory.minimessage.tooltip.button.chat_force").withStyle(ChatFormatting.WHITE),
							Component.translatable("container.builders_inventory.minimessage.tooltip.button.chat_force.desc.disable").withStyle(ChatFormatting.GRAY))
					);
			
			this.exGui.addRenderableWidget(buttonForce);
			this.addRenderableWidget(buttonForce);
			
			this.updateForce(ChatMiniMessageContext.forceChatMinimessage);
		}
	}
	
	@Inject(method = "resize", at = @At("TAIL"))
	private void builders_inventory_resizeChat(Minecraft mc, int width, int height, CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		if (this.minimessage.lastParse != null) this.commandSuggestions.setAllowSuggestions(false);
		this.minimessage.reposition();
	}
	
	@Inject(method = "onEdited", at = @At("HEAD"), cancellable = true)
	private void builders_inventory_editChat(String value, CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		if (this.minimessage.unknownEdit()) ci.cancel();
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
	private void builders_inventory_renderPreviewAndError(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		this.minimessage.renderPreviewOrError(gui);
	}
	
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/CommandSuggestions;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
	private void builders_inventory_renderSuggestions(CommandSuggestions commandSuggestions, GuiGraphics gui, int mouseX, int mouseY, Operation<Void> operation) {
		if (!Config.instance().minimessage_enabledChat || this.minimessage.previewLines.isEmpty()) {
			operation.call(commandSuggestions, gui, mouseX, mouseY);
		} else {
			this.minimessage.renderSuggestions(gui, mouseX, mouseY);
		}
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;getMessageTagAt(DD)Lnet/minecraft/client/GuiMessageTag;"), cancellable = true)
	private void builders_inventory_renderHover(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		if (this.minimessage.renderHover(gui, mouseX, mouseY)) ci.cancel();
		else if (this.minimessage.renderFormatHover(gui, mouseX, mouseY, 0, this.input.getValue().length())) ci.cancel();
		else if (this.exGui.renderTooltip(font, gui, mouseX, mouseY)) ci.cancel();
	}
	
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void builders_inventory_keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!Config.instance().minimessage_enabledChat) return;
		if (this.minimessage.keyPressed(keyCode, scanCode, modifiers)) cir.setReturnValue(true);
		else if (keyCode == GLFW.GLFW_KEY_TAB && !this.minimessage.previewLines.isEmpty()) cir.setReturnValue(true);
	}
	
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void builders_inventory_mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		if (!Config.instance().minimessage_enabledChat) return;
		if (this.minimessage.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) cir.setReturnValue(true);
	}
	
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void builders_inventory_mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!Config.instance().minimessage_enabledChat) return;
		if (this.minimessage.mouseClicked(mouseX, mouseY, button)) cir.setReturnValue(true);
	}
	
	@WrapOperation(method = "moveInHistory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V"))
	private void builders_inventory_historyMove(EditBox input, String value, Operation<Void> operation) {
		if (Config.instance().minimessage_enabledChat) {
			this.minimessage.suppressSuggestionUpdate = true;
			operation.call(input, value);
			this.minimessage.suppressSuggestionUpdate = false;
		} else {
			operation.call(input, value);
		}
	}
	
	@Override
	protected void tickInjectTail(CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		this.minimessage.tick();
	}
	
	/*
	 * Button stuff
	 */
	
	@Override
	protected void clearWidgetsInjectTail(CallbackInfo ci) {
		if (!Config.instance().minimessage_enabledChat) return;
		this.exGui.clearWidgets();
	}
	
	@Unique
	private void updateForce(boolean force) {
		ChatMiniMessageContext.forceChatMinimessage = force;
		if (force) {
			this.buttonForce.secondMode = true;
			this.minimessage.unknownEdit();
		} else {
			this.buttonForce.secondMode = false;
			this.minimessage.clearParse();
		}
	}
	
	@Inject(method = "render", at = @At("HEAD"))
	private void builders_inventory_resetFocus(GuiGraphics gui, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (this.resetFocus) {
			this.resetFocus = false;
			this.setFocused(this.input);
		}
	}
	
	/*
	 * Moving chat if preview is too tall
	 */
	
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"))
	private void builders_inventory_moveChatComponent(ChatComponent chat, GuiGraphics gui, int tickCount, int mouseX, int mouseY, boolean focused, Operation<Void> operation) {
		final Config config = Config.instance();
		final int offsetThreshold = config.minimessage_previewOffsetIgnored;
		if (config.minimessage_enabledChat && config.minimessage_previewOffsetsChat && this.minimessage.previewLines.size() > offsetThreshold) {
			gui.pose().pushPose();
			gui.pose().translate(0, -Mth.ceil(this.minimessage.getScaledLineHeight(offsetThreshold)), 0);
			operation.call(chat, gui, tickCount, mouseX, mouseY, focused);
			gui.pose().popPose();
		} else {
			operation.call(chat, gui, tickCount, mouseX, mouseY, focused);
		}
	}
	
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;getMessageTagAt(DD)Lnet/minecraft/client/GuiMessageTag;"), index = 1)
	private double builders_inventory_moveLabelMouseY(double mouseY) {
		final Config config = Config.instance();
		final int offsetThreshold = config.minimessage_previewOffsetIgnored;
		if (config.minimessage_enabledChat && config.minimessage_previewOffsetsChat && this.minimessage.previewLines.size() > offsetThreshold) {
			return mouseY + Mth.ceil(this.minimessage.getScaledLineHeight(offsetThreshold));
		} else return mouseY;
	}
	
	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;getComponentStyleAt(DD)Lnet/minecraft/network/chat/Style;"), index = 1)
	private double builders_inventory_moveHoverMouseY(double mouseY) {
		final Config config = Config.instance();
		final int offsetThreshold = config.minimessage_previewOffsetIgnored;
		if (config.minimessage_enabledChat && config.minimessage_previewOffsetsChat && this.minimessage.previewLines.size() > offsetThreshold) {
			return mouseY + Mth.ceil(this.minimessage.getScaledLineHeight(offsetThreshold));
		} else return mouseY;
	}
	
}
