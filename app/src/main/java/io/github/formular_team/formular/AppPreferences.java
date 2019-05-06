package io.github.formular_team.formular;

import android.content.SharedPreferences;

import java.util.UUID;

import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.color.Color;

public class AppPreferences {
    private static final String USER_UUID_MOST = "user.uuid.most";

    private static final String USER_UUID_LEAST = "user.uuid.least";

    public static Color getColor(final SharedPreferences preferences, final String key, final Color defaultColor) {
        if (preferences.contains(key)) {
            return Color.hex(preferences.getInt(key, defaultColor.getHex()));
        }
        return defaultColor;
    }

    public static User getUser(final SharedPreferences preferences) {
        final UUID uuid;
        if (preferences.contains(USER_UUID_MOST) &&
            preferences.contains(USER_UUID_LEAST)) {
            uuid = new UUID(
                preferences.getLong(USER_UUID_MOST, 0),
                preferences.getLong(USER_UUID_LEAST, 0)
            );
        } else {
            uuid = UUID.randomUUID();
            preferences.edit()
                .putLong(USER_UUID_MOST, uuid.getMostSignificantBits())
                .putLong(USER_UUID_LEAST, uuid.getLeastSignificantBits())
                .apply();
        }
        final String name = preferences.getString("user.name", "Player");
        final Color color = AppPreferences.getColor(preferences, "user.color", Color.color(0.9F, 0.2F, 0.1F));
        return User.create(uuid, name, color);
    }
}
