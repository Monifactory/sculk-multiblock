package monifactory.multiblocks.client;

import monifactory.multiblocks.client.data.MMCreativeTabs;
import monifactory.multiblocks.common.CommonProxy;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
        MMCreativeTabs.init();
    }
}
