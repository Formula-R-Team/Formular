package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;
import android.os.Parcelable;

import io.github.formular_team.formular.server.GameClient;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class SetTimePacket implements Packet {


    public SetTimePacket(final Parcel source) {

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

    public static final Parcelable.Creator<SetTimePacket> CREATOR = new Parcelable.Creator<SetTimePacket>() {
        @Override
        public SetTimePacket createFromParcel(final Parcel source) {
            return new SetTimePacket(source);
        }

        @Override
        public SetTimePacket[] newArray(final int size) {
            return new SetTimePacket[size];
        }
    };
}