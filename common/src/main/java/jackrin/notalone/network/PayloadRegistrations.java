package jackrin.notalone.network;

import commonnetwork.api.Network;

import java.util.logging.Logger;

public class PayloadRegistrations {
    public void init() {
        Network
                .registerPacket(EntityRotationSyncPayload.TYPE, EntityRotationSyncPayload.STREAM_CODEC, EntityRotationSyncPayload::handle)
                .registerPacket(SyncFovPayload.TYPE, SyncFovPayload.STREAM_CODEC, SyncFovPayload::handle)
                .registerPacket(WhiteEyesSyncPayload.TYPE, WhiteEyesSyncPayload.STREAM_CODEC, WhiteEyesSyncPayload::handle);
    }
}
