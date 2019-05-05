package io.github.formular_team.formular;

import android.content.SharedPreferences;

import java.util.UUID;

import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.color.Color;

public class AppPreferences {
    public static Color getColor(final SharedPreferences preferences, final String key, final Color defaultColor) {
        if (preferences.contains(key)) {
            return Color.hex(preferences.getInt(key, defaultColor.getHex()));
        }
        return defaultColor;
    }

    public static User getUser(final SharedPreferences preferences) {
        final UUID uuid;
        if (preferences.contains("user.uuid.most") &&
            preferences.contains("user.uuid.least")) {
            uuid = new UUID(
                preferences.getLong("user.uuid.most", 0),
                preferences.getLong("user.uuid.least", 0)
            );
        } else {
            uuid = UUID.randomUUID();
            preferences.edit()
                .putLong("user.uuid.most", uuid.getMostSignificantBits())
                .putLong("user.uuid.least", uuid.getLeastSignificantBits())
                .apply();
        }
        final String name = preferences.getString("user.name", "Player");
        final Color color = AppPreferences.getColor(preferences, "user.color", Color.color(0.9F, 0.2F, 0.1F));
        return User.create(uuid, name, color);
    }
}
