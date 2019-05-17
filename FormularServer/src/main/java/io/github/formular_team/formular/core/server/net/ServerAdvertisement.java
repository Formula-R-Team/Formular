package io.github.formular_team.formular.core.server.net;

import java.net.InetSocketAddress;
import java.util.Objects;

public final class ServerAdvertisement {
    private final InetSocketAddress address;

    public ServerAdvertisement(final Builder builder) {
        this.address = Objects.requireNonNull(builder.address, "address");
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public static AddressBuilder builder() {
        return new Builder();
    }

    public interface AddressBuilder {
        Builder setAddress(final InetSocketAddress address);
    }

    public static final class Builder implements AddressBuilder {
        private InetSocketAddress address;

        private Builder() {}

        @Override
        public Builder setAddress(final InetSocketAddress address) {
            this.address = address;
            return this;
        }

        public ServerAdvertisement build() {
            return new ServerAdvertisement(this);
        }
    }
}
