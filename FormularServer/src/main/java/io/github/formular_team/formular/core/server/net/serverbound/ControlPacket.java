package io.github.formular_team.formular.core.server.net.serverbound;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Function;

import io.github.formular_team.formular.core.Kart;
import io.github.formular_team.formular.core.SimpleControlState;
import io.github.formular_team.formular.core.server.net.KartContext;
import io.github.formular_team.formular.core.server.net.Packet;
import io.github.formular_team.formular.core.server.net.PacketHandler;

public class ControlPacket implements Packet {
    public static final Function<ByteBuffer, ControlPacket> CREATOR = ControlPacket::new;

    private final Kart.ControlState state;

    public ControlPacket(final Kart.ControlState state) {
        this.state = Objects.requireNonNull(state);
    }

    public ControlPacket(final ByteBuffer buf) {
        this.state = new SimpleControlState();
        this.state.setThrottle(buf.getFloat());
        this.state.setBrake(buf.getFloat());
        this.state.setSteeringAngle(buf.getFloat());
    }

    @Override
    public Function<ByteBuffer, ? extends Packet> creator() {
        return CREATOR;
    }

    @Override
    public void write(final ByteBuffer buf) {
        buf.putFloat(this.state.getThrottle());
        buf.putFloat(this.state.getBrake());
        buf.putFloat(this.state.getSteeringAngle());
    }

    public static class Handler implements PacketHandler<KartContext, ControlPacket> {
        @Override
        public KartContext apply(final KartContext context, final ControlPacket packet) {
            final Kart kart = context.getKart();
            kart.getControlState().copy(packet.state);
            return context;
        }
    }
}
