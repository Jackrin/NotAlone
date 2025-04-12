package jackrin.notalone.client;

import commonnetwork.api.Network;
import jackrin.notalone.network.SyncFovPayload;
import net.minecraft.client.Minecraft;

public class ClientSyncHandler {
    private static double lastSentFov = 0;
    private static double lastSentAspectRatio = 0;

    public static void tickClient(Minecraft client) {
        if (client.player != null) {
            double playerFov = client.options.fov().get();
            double aspectRatio = (double) client.getWindow().getScreenWidth() / client.getWindow().getScreenHeight();

            if (playerFov != lastSentFov || aspectRatio != lastSentAspectRatio) {
                lastSentFov = playerFov;
                lastSentAspectRatio = aspectRatio;
                SyncFovPayload payload = new SyncFovPayload(playerFov, aspectRatio);
                Network.getNetworkHandler().sendToServer(payload);
            }
        }
    }
}

