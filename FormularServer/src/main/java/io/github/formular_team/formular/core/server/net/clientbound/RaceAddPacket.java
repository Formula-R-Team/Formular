package io.github.formular_team.formular.core.server.net.clientbound;

import java.nio.ByteBuffer;
import java.util.function.Function;

import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.race.Race;
import io.github.formular_team.formular.core.server.net.ByteBuffers;
import io.github.formular_team.formular.core.server.net.ClientContext;
import io.github.formular_team.formular.core.server.net.Context;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class RaceAddPacket implements Packet {
    public static final Function<ByteBuffer, RaceAddPacket> CREATOR = RaceAddPacket::new;

    private final Course course;

    public RaceAddPacket(final Race race) {
        this.course = race.getCourse();
    }

    public RaceAddPacket(final ByteBuffer buf) {
        this.course = ByteBuffers.getCourse(buf);
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        ByteBuffers.putCourse(buf, this.course);
    }

    public static final class Handler implements PacketHandler<ClientContext, RaceAddPacket> {
        @Override
        public Context apply(final ClientContext context, final RaceAddPacket packet) {
            context.getClient().getGame().addCourse(packet.course);
            return context;
        }
    }
}
