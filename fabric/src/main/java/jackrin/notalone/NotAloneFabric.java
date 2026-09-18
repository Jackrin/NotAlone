package jackrin.notalone;

import jackrin.notalone.init.FabricRegistrations;
import net.fabricmc.api.ModInitializer;

public class NotAloneFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        NotAlone.init();
        FabricRegistrations.init();
    }
}
