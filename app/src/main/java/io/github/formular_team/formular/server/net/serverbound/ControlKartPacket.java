package io.github.formular_team.formular.server.net.serverbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.net.Packet;

public class ControlKartPacket implements Packet {


    public ControlKartPacket(final Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }

    public static final Creator<ControlKartPacket> CREATOR = new Creator<ControlKartPacket>() {
        @Override
        public ControlKartPacket createFromParcel(final Parcel source) {
            return new ControlKartPacket(source);
        }

        @Override
        public ControlKartPacket[] newArray(final int size) {
            return new ControlKartPacket[size];
        }
    };
}
