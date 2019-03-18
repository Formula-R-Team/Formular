package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;
import android.os.Parcelable;

import io.github.formular_team.formular.GameClient;
import io.github.formular_team.formular.server.Kart;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class RemoveKartPacket implements Packet {
    private Kart kart;

    public RemoveKartPacket(final Parcel source) {

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

    public static final Parcelable.Creator<RemoveKartPacket> CREATOR = new Parcelable.Creator<RemoveKartPacket>() {
        @Override
        public RemoveKartPacket createFromParcel(final Parcel source) {
            return new RemoveKartPacket(source);
        }

        @Override
        public RemoveKartPacket[] newArray(final int size) {
            return new RemoveKartPacket[size];
        }
    };
}

