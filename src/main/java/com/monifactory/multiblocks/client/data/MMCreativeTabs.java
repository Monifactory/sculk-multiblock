package com.monifactory.multiblocks.client.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.monifactory.multiblocks.common.CommonProxy;
import com.monifactory.multiblocks.common.data.MMBlocks;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.monifactory.multiblocks.MoniMultiblocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class MMCreativeTabs {
    public static RegistryEntry<CreativeModeTab> DEFAULT = CommonProxy.REGISTRATE
        .defaultCreativeTab(MoniMultiblocks.MOD_ID,
            builder -> builder
                .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(
                    MoniMultiblocks.MOD_ID, CommonProxy.REGISTRATE))
                .icon(MMBlocks.ABYSSAL_CASING::asStack).title(Component.literal("Moni Multiblocks"))
                .build())
        .register();

    public static void init() {

    }
}
