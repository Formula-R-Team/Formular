package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;

import io.github.formular_team.formular.GameClient;
import io.github.formular_team.formular.server.Kart;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class RaceEndPacket implements Packet {
    private Kart kart;

    public RaceEndPacket(final Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }

    public static class Handler implements PacketHandler<GameClient> {
        @Override
        public void handle(final GameClient game) {

        }
    }

    public static final Creator<RaceEndPacket> CREATOR = new Creator<RaceEndPacket>() {
        @Override
        public RaceEndPacket createFromParcel(final Parcel source) {
            return new RaceEndPacket(source);
        }

        @Override
        public RaceEndPacket[] newArray(final int size) {
            return new RaceEndPacket[size];
        }
    };
}
