package io.github.formular_team.formular.server.net.serverbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.net.Packet;

public class EnterRacePacket implements Packet {


    public EnterRacePacket(final Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }

    public static final Creator<EnterRacePacket> CREATOR = new Creator<EnterRacePacket>() {
        @Override
        public EnterRacePacket createFromParcel(final Parcel source) {
            return new EnterRacePacket(source);
        }

        @Override
        public EnterRacePacket[] newArray(final int size) {
            return new EnterRacePacket[size];
        }
    };
}
