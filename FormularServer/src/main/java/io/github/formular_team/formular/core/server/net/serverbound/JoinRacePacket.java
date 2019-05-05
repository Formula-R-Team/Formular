package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.GameModel;
import io.github.formular_team.formular.core.KartModel;
import io.github.formular_team.formular.core.SimpleDriver;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.race.RaceListener;
import io.github.formular_team.formular.core.server.net.Connection;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;
import io.github.formular_team.formular.core.server.net.UserContext;
import io.github.formular_team.formular.core.server.net.clientbound.SetCountPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetLapPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPositionPacket;

public class JoinRacePacket implements Packet {
    public static final Function<ByteBuffer, JoinRacePacket> CREATOR = JoinRacePacket::new;

    public JoinRacePacket() {}

    public JoinRacePacket(final ByteBuffer buf) {}

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {}

    public static class Handler implements PacketHandler<UserContext, JoinRacePacket> {
        @Override
        public Context apply(final UserContext context, final JoinRacePacket packet) {
            final GameModel game = context.getServer().getGame();
            final Race race = game.getRace();
            if (race != null) {
                final KartModel kart = game.createKart();
                kart.setColor(context.getUser().getColor());
                game.addKart(kart);
                final Driver userDriver = SimpleDriver.create(context.getUser(), kart);
                game.addDriver(userDriver);
                final Connection conn = context.getRemote();
                // TODO: racer specific listeners
                race.addListener(new RaceListener() {
                    @Override
                    public void onCountDown(final int count) {
                        conn.send(new SetCountPacket(count));
                    }

                    @Override
                    public void onPositionChange(final Driver driver, final int position) {
                        if (userDriver.equals(driver)) {
                            conn.send(new SetPositionPacket(position));
                        }
                    }

                    @Override
                    public void onLapChange(final Driver driver, final int lap) {
                        if (userDriver.equals(driver)) {
                            conn.send(new SetLapPacket(lap));
                        }
                    }
                });
                race.add(userDriver);
            }
            return context;
        }
    }
}
