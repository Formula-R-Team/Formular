/*
 * Copyright 2009-2011 Sönke Sothmann, Steffen Schäfer and others
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.formular_team.formular.math;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wraps Java's ByteBuffer for Parallax3D's API.
 *
 * @author h@realh.co.uk
 */
public abstract class TypeArray  {

    protected ByteBuffer buffer;

	protected TypeArray(final int capacity) {
        this.createBuffer(capacity);
	}

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public Buffer getTypedBuffer() {
        return this.buffer;
    }

    /**
     * Support creation of empty Float32Buffers which can be resized dynamically.
     */
    protected TypeArray() {
        this.buffer = null;
    }

    protected void createBuffer(final int capacity) {
        this.buffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    /**
     * Set multiple values, reading input values from the array.
     *
     * @param array
    */
    public final void set(final TypeArray array) {
        final ByteBuffer b = array.buffer;
        final int l = b.limit();
        b.rewind();
        if (l > this.buffer.capacity()) {
            this.createBuffer(l);
            this.createTypedBuffer();
        } else {
            this.buffer.rewind();
        }
        this.buffer.put(b);
	}

    /**
     * Set multiple values, reading input values from the array.
     *
     * @param array
     * @param offset indicates the index in the current array where values are
     * 				written, in subclass' element size.
     */
    public void set(final TypeArray array, final int offset) {
        if (this.getClass() != array.getClass()) {
            throw new Error("Type mismatch for array copy");
        }
        final ByteBuffer b = array.buffer;
        final int l = b.limit() + offset;
        b.rewind();
        if (l > this.buffer.capacity()) {
            final ByteBuffer old = this.buffer;
            this.createBuffer(l);
            old.limit(offset);
            this.buffer.put(old);
            this.createTypedBuffer();
        } else {
            this.buffer.position(offset);
        }
        this.buffer.put(b);
    }

    /**
     * Gets the length of this array in bytes.
     */
    public int getByteLength() {
        return this.buffer.limit();
    }

    /**
     * Gets the length of this array in elements of subclass' type.
     */
    public abstract int getLength();

    /**
     * Disused reverse() method elided.
     */

    protected abstract void createTypedBuffer();
}