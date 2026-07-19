package jackrin.notalone;

import jackrin.notalone.network.PayloadRegistrations;
import net.minecraft.resources.Identifier;

import static jackrin.notalone.Constants.MOD_ID;

public class NotAlone {
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        new PayloadRegistrations().init();
    }
}