package io.github.formular_team.formular.server.net.serverbound;

import android.os.Parcel;

import io.github.formular_team.formular.server.net.Packet;

public class AddCoursePacket implements Packet {


    public AddCoursePacket(final Parcel source) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {

    }

    public static final Creator<AddCoursePacket> CREATOR = new Creator<AddCoursePacket>() {
        @Override
        public AddCoursePacket createFromParcel(final Parcel source) {
            return new AddCoursePacket(source);
        }

        @Override
        public AddCoursePacket[] newArray(final int size) {
            return new AddCoursePacket[size];
        }
    };
}
