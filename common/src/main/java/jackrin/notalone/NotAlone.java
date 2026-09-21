package jackrin.notalone;

import jackrin.notalone.config.NotAloneConfig;
import jackrin.notalone.network.PayloadRegistrations;
import jackrin.notalone.platform.Services;
import net.minecraft.resources.ResourceLocation;

import static jackrin.notalone.Constants.MOD_ID;

public class NotAlone {
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        NotAloneConfig.load(Services.PLATFORM.getConfigDirectory());
        new PayloadRegistrations().init();
    }
}