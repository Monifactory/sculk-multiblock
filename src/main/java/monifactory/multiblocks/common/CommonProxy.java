package monifactory.multiblocks.common;

import com.tterrag.registrate.Registrate;
import monifactory.multiblocks.MoniMultiblocks;
import monifactory.multiblocks.common.data.MMBlocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public class CommonProxy {
    // public static final GTRegistrate REGISTRATE =
    // GTRegistrate.create(MoniMultiblocks.MOD_ID);
    public static final Registrate REGISTRATE = Registrate.create(MoniMultiblocks.MOD_ID);
    /*
     * public static RegistryEntry<CreativeModeTab> DEFAULT = REGISTRATE
     * .defaultCreativeTab("material_block", builder -> builder .displayItems(new
     * RegistrateDisplayItemsGenerator("material_item", REGISTRATE)) .icon(() ->
     * MMBlocks.MESOL_CASING.asStack()) .build()) .register();
     */
    static
    {
        // REGISTRATE.creativeModeTab(() -> DEFAULT);
    }

    public CommonProxy() {

    }

    public static void init() {
        MMBlocks.init();
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event) {
        // this is done to delay initialization of content to be after KJS has set up.
        event.enqueueWork(CommonProxy::init);
    }
}
