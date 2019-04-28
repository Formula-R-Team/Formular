package io.github.formular_team.formular.core.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import io.github.formular_team.formular.core.SimpleGameModel;

public class ServerMain {
    public static void main(final String[] args) throws IOException {
        EndpointController.create(SimpleServer.open(new InetSocketAddress("localhost", Endpoint.DEFAULT_PORT), new SimpleGameModel(), 20)).start();
    }
}
