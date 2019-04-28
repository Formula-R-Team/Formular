package io.github.formular_team.formular.core.server.net;

import io.github.formular_team.formular.core.server.net.clientbound.KartAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPosePacket;
import io.github.formular_team.formular.core.server.net.serverbound.AddKartPacket;
import io.github.formular_team.formular.core.server.net.serverbound.ControlPacket;
import io.github.formular_team.formular.core.server.net.serverbound.NewUserPacket;

public class Protocol {
    public static StateManager createConnectionFactory() {
        return StateManager.builder()
            .in(ClientContext.class, client -> client
                .put(SetPosePacket.CREATOR, new SetPosePacket.Handler())
                .put(KartAddPacket.CREATOR, new KartAddPacket.Handler())
            )
            .in(ServerContext.class, server -> server
                .put(NewUserPacket.CREATOR, new NewUserPacket.Handler())
                .in(UserContext.class, user -> user
                    .put(AddKartPacket.CREATOR, new AddKartPacket.Handler())
                    .in(KartContext.class, kart -> kart
                        .put(ControlPacket.CREATOR, new ControlPacket.Handler())
                    )
                )
            )
            .build();
    }
}
