package io.github.formular_team.formular.core.server.net;

import java.util.Objects;

import io.github.formular_team.formular.core.User;

public class UserContext extends ServerContext {
    private final User user;

    public UserContext(final ServerContext parent, final User user) {
        super(parent, parent.getServer());
        this.user = Objects.requireNonNull(user);
    }

    public User getUser() {
        return this.user;
    }
}
