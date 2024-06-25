package monifactory.multiblocks;

import monifactory.multiblocks.client.ClientProxy;
import monifactory.multiblocks.common.CommonProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(MoniMultiblocks.MOD_ID)
public class MoniMultiblocks {
    public static final String MOD_ID = "moni_multiblocks";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public MoniMultiblocks() {
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }
}
