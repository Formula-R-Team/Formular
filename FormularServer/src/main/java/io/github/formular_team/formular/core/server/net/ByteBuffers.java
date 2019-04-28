package io.github.formular_team.formular.core.server.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.github.formular_team.formular.core.math.Vector2;

public final class ByteBuffers {
    private ByteBuffers() {}

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

    public static ByteBuffer putString(final ByteBuffer buf, final String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return putByteArray(buf, bytes);
    }

    public static String getString(final ByteBuffer buf) {
        final byte[] bytes = getByteArray(buf);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static ByteBuffer putVector2(final ByteBuffer buf, final Vector2 value) {
        return buf.putFloat(value.getX()).putFloat(value.getY());
    }

    public static Vector2 getVector2(final ByteBuffer buf) {
        return new Vector2(buf.getFloat(), buf.getFloat());
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
}
