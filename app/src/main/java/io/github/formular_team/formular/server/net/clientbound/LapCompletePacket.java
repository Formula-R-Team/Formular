package io.github.formular_team.formular.server.net.clientbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.GameClient;
import io.github.formular_team.formular.server.net.Packet;
import io.github.formular_team.formular.server.net.PacketHandler;

public class LapCompletePacket implements Packet {
    private int number;

    public LapCompletePacket(final Parcel source) {
        source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(number);
    }

    public static class Handler implements PacketHandler<GameClient> {
        @Override
        public void handle(final GameClient game) {

        }
    }

    public static final Creator<LapCompletePacket> CREATOR = new Creator<LapCompletePacket>() {
        @Override
        public LapCompletePacket createFromParcel(final Parcel source) {
            return new LapCompletePacket(source);
        }

        @Override
        public LapCompletePacket[] newArray(final int size) {
            return new LapCompletePacket[size];
        }
    };
}
