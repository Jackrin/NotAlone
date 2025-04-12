package jackrin.notalone.network;

import commonnetwork.api.Network;

public class PayloadRegistrations {
    public void init() {
        Network
                .registerPacket(EntityRotationSyncPayload.type(), EntityRotationSyncPayload.class, EntityRotationSyncPayload.STREAM_CODEC, EntityRotationSyncPayload::handle)
                .registerPacket(SyncFovPayload.type(), SyncFovPayload.class, SyncFovPayload.STREAM_CODEC, SyncFovPayload::handle);
    }
}
