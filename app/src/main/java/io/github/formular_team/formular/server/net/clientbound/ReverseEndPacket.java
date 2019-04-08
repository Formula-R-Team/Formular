package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;
import android.os.Parcelable;

import io.github.formular_team.formular.server.GameClient;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class ReverseEndPacket implements Packet {


    public ReverseEndPacket(final Parcel source) {

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

    public static final Parcelable.Creator<ReverseEndPacket> CREATOR = new Parcelable.Creator<ReverseEndPacket>() {
        @Override
        public ReverseEndPacket createFromParcel(final Parcel source) {
            return new ReverseEndPacket(source);
        }

        @Override
        public ReverseEndPacket[] newArray(final int size) {
            return new ReverseEndPacket[size];
        }
    };
}
