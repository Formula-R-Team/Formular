package io.github.formular_team.formular.server.net.serverbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.net.Packet;

public class BeginRacePacket implements Packet {


    public BeginRacePacket(final Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }

    public static final Creator<BeginRacePacket> CREATOR = new Creator<BeginRacePacket>() {
        @Override
        public BeginRacePacket createFromParcel(final Parcel source) {
            return new BeginRacePacket(source);
        }

        @Override
        public BeginRacePacket[] newArray(final int size) {
            return new BeginRacePacket[size];
        }
    };
}
