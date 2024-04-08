package _3650.builders_inventory.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import _3650.builders_inventory.config.Config;

public class BuildersInventoryModMenu implements ModMenuApi {
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> Config.HANDLER.generateGui().generateScreen(parent);
	}
	
}
