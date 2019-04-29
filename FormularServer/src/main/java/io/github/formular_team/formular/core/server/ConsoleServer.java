package io.github.formular_team.formular.core.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

import io.github.formular_team.formular.core.SimpleGameModel;

public final class ConsoleServer {
    public static void main(final String[] args) throws IOException {
        final EndpointController controller = EndpointController.create(SimpleServer.open(new InetSocketAddress(Endpoint.DEFAULT_PORT), new SimpleGameModel(), 30));
        controller.start();
        for (final Scanner scanner = new Scanner(System.in); !"stop".equals(scanner.nextLine()); );
        controller.stop();
    }
}
