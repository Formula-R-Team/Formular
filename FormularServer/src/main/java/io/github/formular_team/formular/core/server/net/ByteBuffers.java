package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.formular_team.formular.core.Checkpoint;
import io.github.formular_team.formular.core.course.Course;
import io.github.formular_team.formular.core.course.CourseMetadata;
import io.github.formular_team.formular.core.course.track.Track;
import io.github.formular_team.formular.core.User;
import io.github.formular_team.formular.core.color.Color;
import io.github.formular_team.formular.core.math.curve.Path;
import io.github.formular_team.formular.core.math.PathVisitor;
import io.github.formular_team.formular.core.math.curve.Shape;
import io.github.formular_team.formular.core.math.Vector2;
import io.github.formular_team.formular.core.race.RaceConfiguration;

@SuppressWarnings({ "UnusedReturnValue", "WeakerAccess", "unused" })
public final class ByteBuffers {
    private ByteBuffers() {}

    public static ByteBuffer putBoolean(final ByteBuffer buf, final boolean value) {
        return buf.put((byte) (value ? 1 : 0));
    }

    public static boolean getBoolean(final ByteBuffer buf) {
        return buf.get() != 0;
    }

    public static ByteBuffer putUnsigned(final ByteBuffer buf, final int value) {
        return buf.put((byte) value);
    }

    public static int getUnsigned(final ByteBuffer buf) {
        return buf.get() & 0xFF;
    }

    public static ByteBuffer putUnsignedShort(final ByteBuffer buf, final int value) {
        return buf.putShort((short) value);
    }

    public static int getUnsignedShort(final ByteBuffer buf) {
        return buf.getShort() & 0xFFFFFF;
    }

    public static ByteBuffer putEnum(final ByteBuffer buf, final Enum<?> value) {
        // TODO: profile me
        final int count = value.getClass().getEnumConstants().length;
        final int ordinal = value.ordinal();
        if (count > (1 << Short.SIZE)) {
            buf.putInt(ordinal);
        } else if (count > (1 << Byte.SIZE)) {
            ByteBuffers.putUnsignedShort(buf, ordinal);
        } else {
            ByteBuffers.putUnsigned(buf, ordinal);
        }
        return buf;
    }

    public static <E extends Enum<E>> E getEnum(final ByteBuffer buf, final Class<E> type) {
        final E[] constants = type.getEnumConstants();
        final int count = constants.length;
        final int ordinal;
        if (count > (1 << Short.SIZE)) {
            ordinal = buf.getInt();
            if (ordinal < 0) {
                throw new RuntimeException("Bad ordinal");
            }
        } else if (count > (1 << Byte.SIZE)) {
            ordinal = ByteBuffers.getUnsignedShort(buf);
        } else {
            ordinal = ByteBuffers.getUnsigned(buf);
        }
        if (ordinal < constants.length) {
            return constants[ordinal];
        }
        throw new RuntimeException("Bad ordinal");
    }

    public static ByteBuffer putChars(final ByteBuffer buf, final String value) {
        for (int i = 0; i < value.length(); i++) {
            buf.putChar(value.charAt(i));
        }
        return buf;
    }

    public static String getChars(final ByteBuffer buf, final int length) {
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = buf.getChar();
        }
        return new String(chars);
    }

    public static ByteBuffer putString(final ByteBuffer buf, final String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return putByteArray(buf, bytes);
    }

    public static String getString(final ByteBuffer buf) {
        final byte[] bytes = getByteArray(buf);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static ByteBuffer putByteArray(final ByteBuffer buf, final byte[] value) {
        buf.putInt(value.length);
        buf.put(value);
        return buf;
    }

    public static byte[] getByteArray(final ByteBuffer buf) {
        final int length = buf.getInt();
        final byte[] value = new byte[length];
        buf.get(value);
        return value;
    }

    public static ByteBuffer putUuid(final ByteBuffer buf, final UUID uuid) {
        return buf.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
    }

    public static UUID getUuid(final ByteBuffer buf) {
        return new UUID(buf.getLong(), buf.getLong());
    }

    public static ByteBuffer putVector2(final ByteBuffer buf, final Vector2 value) {
        return buf.putFloat(value.getX()).putFloat(value.getY());
    }

    public static Vector2 getVector2(final ByteBuffer buf) {
        return new Vector2(buf.getFloat(), buf.getFloat());
    }

    public static ByteBuffer putColor(final ByteBuffer buf, final Color color) {
        return buf.putInt(color.getHex());
    }

    public static Color getColor(final ByteBuffer buf) {
        return Color.hex(buf.getInt());
    }

    public static ByteBuffer putUser(final ByteBuffer buf, final User user) {
        ByteBuffers.putUuid(buf, user.getUuid());
        ByteBuffers.putString(buf, user.getName());
        ByteBuffers.putColor(buf, user.getColor());
        return buf;
    }

    public static User getUser(final ByteBuffer buf) {
        final UUID uuid = ByteBuffers.getUuid(buf);
        final String name = ByteBuffers.getString(buf);
        final Color color = ByteBuffers.getColor(buf);
        return User.create(uuid, name, color);
    }

    public static ByteBuffer putRaceConfiguration(final ByteBuffer buf, final RaceConfiguration configuration) {
        ByteBuffers.putUnsigned(buf, configuration.getLapCount());
        ByteBuffers.putUnsigned(buf, configuration.getRacerCap());
        final int flags = (configuration.isTimeTrial()   ? 0b001 : 0) |
                          (configuration.hasCpus()       ? 0b010 : 0) |
                          (configuration.hasSpectators() ? 0b100 : 0);
        ByteBuffers.putUnsigned(buf, flags);
        return buf;
    }

    public static RaceConfiguration getRaceConfiguration(final ByteBuffer buf) {
        final int lapCount = ByteBuffers.getUnsigned(buf);
        final int racerCap = ByteBuffers.getUnsigned(buf);
        final int flags = ByteBuffers.getUnsigned(buf);
        return RaceConfiguration.builder()
            .setLapCount(lapCount)
            .setRacerCap(racerCap)
            .setTimeTrial((flags  & 0b001) != 0)
            .setCpus((flags       & 0b010) != 0)
            .setSpectators((flags & 0b100) != 0)
            .build();
    }

    public static ByteBuffer putCourse(final ByteBuffer buf, final Course course) {
        ByteBuffers.putCourseMetadata(buf, course.getMetadata());
        ByteBuffers.putTrack(buf, course.getTrack());
        buf.putFloat(course.getWorldScale());
        return buf;
    }

    public static Course getCourse(final ByteBuffer buf) {
        return Course.builder()
            .setMetadata(ByteBuffers.getCourseMetadata(buf))
            .setTrack(ByteBuffers.getTrack(buf))
            .setWorldScale(buf.getFloat())
            .build();
    }

    public static ByteBuffer putCourseMetadata(final ByteBuffer buf, final CourseMetadata metadata) {
        ByteBuffers.putUser(buf, metadata.getCreator());
        buf.putLong(metadata.getCreationDate());
        ByteBuffers.putString(buf, metadata.getName());
        return buf;
    }

    public static CourseMetadata getCourseMetadata(final ByteBuffer buf) {
        return CourseMetadata.create(
            ByteBuffers.getUser(buf),
            buf.getLong(),
            ByteBuffers.getString(buf)
        );
    }

    public static ByteBuffer putTrack(final ByteBuffer buf, final Track track) {
        ByteBuffers.putPath(buf, track.getRoadPath());
        buf.putFloat(track.getRoadWidth());
        final List<? extends Checkpoint> checkpoints = track.getCheckpoints();
        ByteBuffers.putUnsignedShort(buf, checkpoints.size());
        for (final Checkpoint checkpoint : checkpoints) {
            ByteBuffers.putVector2(buf, checkpoint.getP1());
            ByteBuffers.putVector2(buf, checkpoint.getP2());
            ByteBuffers.putUnsignedShort(buf, checkpoint.getIndex());
            buf.putFloat(checkpoint.getPosition());
            ByteBuffers.putBoolean(buf, checkpoint.isRequired());
        }
        return buf;
    }

    public static Track getTrack(final ByteBuffer buf) {
        final Path roadPath = ByteBuffers.getPath(buf);
        final float roadWidth = buf.getFloat();
        final int checkpointCount = ByteBuffers.getUnsignedShort(buf);
        final List<Checkpoint> checkpoints = new ArrayList<>(checkpointCount);
        for (int n = 0; n < checkpointCount; n++) {
            final Vector2 p1 = ByteBuffers.getVector2(buf);
            final Vector2 p2 = ByteBuffers.getVector2(buf);
            final int index = ByteBuffers.getUnsignedShort(buf);
            final float position = buf.getFloat();
            final boolean required = ByteBuffers.getBoolean(buf);
            checkpoints.add(new Checkpoint(p1, p2, index, position, required));
        }
        return Track.builder()
            .setRoadPath(roadPath)
            .setRoadWidth(roadWidth)
            .setRoadShape(new Shape()) // TODO: road shape
            .setCheckpoints(checkpoints)
            .build();
    }

    private static final int END_OF_PATH     = 0,
                             ACTION_MOVE_TO  = 1,
                             ACTION_LINE_TO  = 2,
                             ACTION_CURVE_TO = 3,
                             ACTION_CLOSE    = 4;

    public static ByteBuffer putPath(final ByteBuffer buf, final Path path) {
        path.visit(new PathVisitor() {
            @Override
            public void moveTo(final float x, final float y) {
                ByteBuffers.putUnsigned(buf, ACTION_MOVE_TO).putFloat(x).putFloat(y);
            }

            @Override
            public void lineTo(final float x, final float y) {
                ByteBuffers.putUnsigned(buf, ACTION_LINE_TO).putFloat(x).putFloat(y);
            }

            @Override
            public void bezierCurveTo(final float x1, final float y1, final float x2, final float y2, final float x3, final float y3) {
                ByteBuffers.putUnsigned(buf, ACTION_CURVE_TO)
                    .putFloat(x1).putFloat(y1)
                    .putFloat(x2).putFloat(y2)
                    .putFloat(x3).putFloat(y3);
            }

            @Override
            public void closePath() {
                ByteBuffers.putUnsigned(buf, ACTION_CLOSE);
            }
        });
        ByteBuffers.putUnsigned(buf, END_OF_PATH);
        return buf;
    }

    public static Path getPath(final ByteBuffer buf) {
        final Path path = new Path();
        while (true) {
            switch (ByteBuffers.getUnsigned(buf)) {
            case END_OF_PATH:
                return path;
            case ACTION_MOVE_TO:
                path.moveTo(buf.getFloat(), buf.getFloat());
                break;
            case ACTION_LINE_TO:
                path.lineTo(buf.getFloat(), buf.getFloat());
                break;
            case ACTION_CURVE_TO:
                path.bezierCurveTo(buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat(), buf.getFloat());
                break;
            case ACTION_CLOSE:
               path.closePath();
              break;
            default:
                throw new RuntimeException("Bad action");
            }
        }
    }
}
