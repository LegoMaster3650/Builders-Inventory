package _3650.builders_inventory.api.minimessage.color;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.network.chat.TextColor;

/*
 * colors and credit and general code copied directly from
 * https://github.com/KyoriPowered/adventure/blob/main/4/text-minimessage/src/main/java/net/kyori/adventure/text/minimessage/tag/standard/PrideTag.java
 */

public enum PrideFlagGradients {
	// Colours taken from https://www.kapwing.com/resources/official-pride-colors-2021-exact-color-codes-for-15-pride-flags.
	pride(0xE50000, 0xFF8D00, 0xFFEE00, 0x28121, 0x004CFF, 0x770088),
	progress(0xFFFFFF, 0xFFAFC7, 0x73D7EE, 0x613915, 0x000000, 0xE50000, 0xFF8D00, 0xFFEE00, 0x28121, 0x004CFF, 0x770088),
	trans(0x5BCFFB, 0xF5ABB9, 0xFFFFFF, 0xF5ABB9, 0x5BCFFB),
	bi(0xD60270, 0x9B4F96, 0x0038A8),
	pan(0xFF1C8D, 0xFFD700, 0x1AB3FF),
	nb(0xFCF431, 0xFCFCFC, 0x9D59D2, 0x282828),
	lesbian(0xD62800, 0xFF9B56, 0xFFFFFF, 0xD4662A6, 0xA40062),
	ace(0x000000, 0xA4A4A4, 0xFFFFFF, 0x810081),
	agender(0x000000, 0xBABABA, 0xFFFFFF, 0xBAF484, 0xFFFFFF, 0xBABABA, 0x000000),
	demisexual(0x000000, 0xFFFFFF, 0x6E0071, 0xD3D3D3),
	genderqueer(0xB57FDD, 0xFFFFFF, 0x49821E),
	gemderfluid(0xFE76A2, 0xFFFFFF, 0xBF12D7, 0x000000, 0x303CBE),
	intersex(0xFFD800, 0x7902AA, 0xFFD800),
	aro(0x3BA740, 0xA8D47A, 0xFFFFFF, 0xABABAB, 0x000000),
	
	// Colours taken from https://www.hrc.org/resources/lgbtq-pride-flags.
	baker(0xCD66FF, 0xFF6599, 0xFE0000, 0xFE9900, 0xFFFF01, 0x009900, 0x0099CB, 0x350099, 0x990099),
	philly(0x000000, 0x784F17, 0xFE0000, 0xFD8C00, 0xFFE500, 0x119F0B, 0x0644B3, 0xC22EDC),
	queer(0x000000, 0x9AD9EA, 0x00A3E8, 0xB5E51D, 0xFFFFFF, 0xFFC90D, 0xFC6667, 0xFEAEC9, 0x000000),
	gay(0x078E70, 0x26CEAA, 0x98E8C1, 0xFFFFFF, 0x7BADE2, 0x5049CB, 0x3D1A78),
	bigender(0xC479A0, 0xECA6CB, 0xD5C7E8, 0xFFFFFF, 0xD5C7E8, 0x9AC7E8, 0x6C83CF),
	demigender(0x7F7F7F, 0xC3C3C3, 0xFBFF74, 0xFFFFFF, 0xFBFF74, 0xC3C3C3, 0x7F7F7F),
	;
	public final List<TextColor> colors;
	
	private PrideFlagGradients(int... colors) {
		this.colors = Arrays.stream(colors).mapToObj(TextColor::fromRgb).collect(Collectors.toList());
	}
	
	public static Optional<PrideFlagGradients> byName(String name) {
		for (var val : values()) {
			if (val.name().equals(name)) return Optional.of(val);
		}
		return Optional.empty();
	}
}
