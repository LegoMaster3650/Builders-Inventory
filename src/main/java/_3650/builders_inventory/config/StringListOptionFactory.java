package _3650.builders_inventory.config;

import java.util.List;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.autogen.ListGroup;
import dev.isxander.yacl3.config.v2.api.autogen.OptionAccess;
import dev.isxander.yacl3.config.v2.api.autogen.ListGroup.ControllerFactory;
import dev.isxander.yacl3.config.v2.api.autogen.ListGroup.ValueFactory;

public class StringListOptionFactory implements ValueFactory<String>, ControllerFactory<String> {
	
	public StringListOptionFactory() {}
	
	@Override
	public String provideNewValue() {
		return "";
	}
	
	@Override
	public ControllerBuilder<String> createController(ListGroup annotation, ConfigField<List<String>> field, OptionAccess storage, Option<String> option) {
		return StringControllerBuilder.create(option);
	}
	
}
