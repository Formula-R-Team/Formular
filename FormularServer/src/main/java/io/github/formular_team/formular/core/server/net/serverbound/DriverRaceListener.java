package io.github.formular_team.formular.core.server.net.serverbound;

import io.github.formular_team.formular.core.Driver;
import io.github.formular_team.formular.core.race.RaceListener;
import io.github.formular_team.formular.core.server.net.Connection;
import io.github.formular_team.formular.core.server.net.clientbound.RaceFinishPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetCountPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetLapPacket;
import io.github.formular_team.formular.core.server.net.clientbound.SetPositionPacket;

public class DriverRaceListener implements RaceListener {
    private final Connection conn;

    private final Driver driver;

    public DriverRaceListener(final Connection conn, final Driver driver) {
        this.conn = conn;
        this.driver = driver;
    }

    @Override
    public void onCountDown(final int count) {
        this.conn.send(new SetCountPacket(count));
    }

    @Override
    public void onPositionChange(final Driver driver, final int position) {
        if (this.driver.equals(driver)) {
            this.conn.send(new SetPositionPacket(position));
        }
    }

    @Override
    public void onLapChange(final Driver driver, final int lap) {
        if (this.driver.equals(driver)) {
            this.conn.send(new SetLapPacket(lap));
        }
    }

    @Override
    public void onFinish(final Driver driver) {
        if (this.driver.equals(driver)) {
            this.conn.send(new RaceFinishPacket());
        }
    }
}
