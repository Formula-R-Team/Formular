package io.github.formular_team.formular.core.server.net;

import io.github.formular_team.formular.core.server.net.clientbound.KartAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.RaceAddPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetCountPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetLapPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPosePacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPositionPacket;
import io.github.formular_team.formular.core.server.net.serverbound.AddKartPacket;
import io.github.formular_team.formular.core.server.net.serverbound.BeginRacePacket;
import io.github.formular_team.formular.core.server.net.serverbound.ControlPacket;
import io.github.formular_team.formular.core.server.net.serverbound.CreateRacePacket;
import io.github.formular_team.formular.core.server.net.serverbound.JoinRacePacket;
import io.github.formular_team.formular.core.server.net.serverbound.NewUserPacket;

public class Protocol {
    public static ContextualPacketGraph createConnectionFactory() {
        return ContextualPacketGraph.builder()
            .when(ClientContext.class, client -> client
                .accept(SetPosePacket.CREATOR, new SetPosePacket.Handler())
                .accept(KartAddPacket.CREATOR, new KartAddPacket.Handler())
                .accept(RaceAddPacket.CREATOR, new RaceAddPacket.Handler())
                .accept(SetCountPacket.CREATOR, new SetCountPacket.Handler())
                .accept(SetPositionPacket.CREATOR, new SetPositionPacket.Handler())
                .accept(SetLapPacket.CREATOR, new SetLapPacket.Handler())
            )
            .when(ServerContext.class, server -> server
                .accept(NewUserPacket.CREATOR, new NewUserPacket.Handler())
                .when(UserContext.class, user -> user
                    .accept(AddKartPacket.CREATOR, new AddKartPacket.Handler())
                    .accept(JoinRacePacket.CREATOR, new JoinRacePacket.Handler())
                    .accept(CreateRacePacket.CREATOR, new CreateRacePacket.Handler())
                    .accept(BeginRacePacket.CREATOR, new BeginRacePacket.Handler())
                    .when(KartContext.class, kart -> kart
                        .accept(ControlPacket.CREATOR, new ControlPacket.Handler())
                    )
                )
            )
            .build();
    }
}
