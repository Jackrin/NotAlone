package jackrin.notalone.client;

import jackrin.notalone.init.FabricClientRegistrations;
import net.fabricmc.api.ClientModInitializer;

public class NotAloneFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricClientRegistrations.init();
    }
}
