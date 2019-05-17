package io.github.formular_team.formular.core.server;

import java.util.Scanner;

import io.github.formular_team.formular.core.server.net.LanAdvertiser;

public final class LanConsole {
    public static void main(final String[] args) {
        final Thread thread = new Thread(LanAdvertiser.createSubscriber(a -> System.out.println(a.getAddress())));
        thread.start();
        for (final Scanner scanner = new Scanner(System.in); !"stop".equals(scanner.nextLine()); );
        thread.interrupt();
        try {
            thread.join();
        } catch (final InterruptedException ignored) {}
    }
}
