package jackrin.notalone;


import jackrin.notalone.init.NeoForgeRegistrations;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class NotAloneNeoForge {

    public NotAloneNeoForge(IEventBus eventBus) {
        NotAlone.init();
        NeoForgeRegistrations.register(eventBus);
    }
}