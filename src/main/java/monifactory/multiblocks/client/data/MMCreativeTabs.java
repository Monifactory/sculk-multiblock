package monifactory.multiblocks.client.data;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import monifactory.multiblocks.MoniMultiblocks;
import monifactory.multiblocks.common.CommonProxy;
import monifactory.multiblocks.common.data.MMBlocks;
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
