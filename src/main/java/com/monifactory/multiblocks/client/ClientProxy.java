package com.monifactory.multiblocks.client;

import com.monifactory.multiblocks.common.CommonProxy;
import com.monifactory.multiblocks.client.data.MMCreativeTabs;

public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        super();
        MMCreativeTabs.init();
    }
}
