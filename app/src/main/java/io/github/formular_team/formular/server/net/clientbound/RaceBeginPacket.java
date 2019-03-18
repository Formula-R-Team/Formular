package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;

import io.github.formular_team.formular.GameClient;
import io.github.formular_team.formular.server.Kart;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class RaceBeginPacket implements Packet {


    public RaceBeginPacket(final Parcel source) {

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

    public static final Creator<RaceBeginPacket> CREATOR = new Creator<RaceBeginPacket>() {
        @Override
        public RaceBeginPacket createFromParcel(final Parcel source) {
            return new RaceBeginPacket(source);
        }

        @Override
        public RaceBeginPacket[] newArray(final int size) {
            return new RaceBeginPacket[size];
        }
    };
}
