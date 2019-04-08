package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.GameClient;
import io.github.formular_team.formular.server.Kart;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class AddKartPacket implements Packet {
    private Kart kart;

    public AddKartPacket(final Parcel source) {

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

    public static final Creator<AddKartPacket> CREATOR = new Creator<AddKartPacket>() {
        @Override
        public AddKartPacket createFromParcel(final Parcel source) {
            return new AddKartPacket(source);
        }

        @Override
        public AddKartPacket[] newArray(final int size) {
            return new AddKartPacket[size];
        }
    };
}
