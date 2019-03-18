package io.github.formular_team.formular.server.net.serverbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.net.Packet;

public class AddRacePacket implements Packet {


    public AddRacePacket(final Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }

    public static final Creator<AddRacePacket> CREATOR = new Creator<AddRacePacket>() {
        @Override
        public AddRacePacket createFromParcel(final Parcel source) {
            return new AddRacePacket(source);
        }

        @Override
        public AddRacePacket[] newArray(final int size) {
            return new AddRacePacket[size];
        }
    };
}
