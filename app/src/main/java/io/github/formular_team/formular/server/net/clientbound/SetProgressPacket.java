package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;
import android.os.Parcelable;

import io.github.formular_team.formular.GameClient;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class SetProgressPacket implements Packet {


    public SetProgressPacket(final Parcel source) {

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

    public static final Parcelable.Creator<SetProgressPacket> CREATOR = new Parcelable.Creator<SetProgressPacket>() {
        @Override
        public SetProgressPacket createFromParcel(final Parcel source) {
            return new SetProgressPacket(source);
        }

        @Override
        public SetProgressPacket[] newArray(final int size) {
            return new SetProgressPacket[size];
        }
    };
}
