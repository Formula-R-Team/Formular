package io.github.formular_team.formular.core.server.net;

import io.github.formular_team.formular.core.server.net.clientbound.KartAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPosePacket;
import io.github.formular_team.formular.core.server.net.serverbound.AddKartPacket;
import io.github.formular_team.formular.core.server.net.serverbound.ControlPacket;
import io.github.formular_team.formular.core.server.net.serverbound.NewUserPacket;

public class Protocol {
    public static ContextualPacketGraph createConnectionFactory() {
        return ContextualPacketGraph.builder()
            .when(ClientContext.class, client -> client
                .accept(SetPosePacket.CREATOR, new SetPosePacket.Handler())
                .accept(KartAddPacket.CREATOR, new KartAddPacket.Handler())
            )
            .when(ServerContext.class, server -> server
                .accept(NewUserPacket.CREATOR, new NewUserPacket.Handler())
                .when(UserContext.class, user -> user
                    .accept(AddKartPacket.CREATOR, new AddKartPacket.Handler())
                    .when(KartContext.class, kart -> kart
                        .accept(ControlPacket.CREATOR, new ControlPacket.Handler())
                    )
                )
            )
            .build();
    }
}
