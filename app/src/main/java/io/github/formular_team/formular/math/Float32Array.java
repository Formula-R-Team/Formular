/*
 * Copyright 2009-2011 Sönke Sothmann, Steffen Schäfer and others
 * Copyright 2015 Tony Houghton, h@realh.co.uk
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

import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * The typed array that holds float (32-bit IEEE floating point) as its element.
 * 
 * @author h@realh.co.uk
 */
public final class Float32Array extends TypeArray {
	public static final int BYTES_PER_ELEMENT = 4;

	private FloatBuffer floatBuffer;

	/**
	 * Lighting requires arrays of unknown length.
	 */
	private boolean resizable = false;

	protected Float32Array() {
		this.buffer = null;
		this.floatBuffer = null;
		this.resizable = true;
	}

	/**
	 * @param capacity	In bytes.
	 */
	protected Float32Array(final int capacity) {
		super(capacity);
		this.createTypedBuffer();
	}

	@Override
	protected void createTypedBuffer() {
		this.floatBuffer = this.getBuffer().asFloatBuffer();
	}

	@Override
	public FloatBuffer getTypedBuffer() {
		return this.floatBuffer;
	}

	public static Float32Array createArray()
	{
		return new Float32Array();
	}

    /**
	 * Create a new {@link java.nio.ByteBuffer} with enough bytes to hold length
	 * elements of this typed array.
	 * 
	 * @param length
	 */
	public static Float32Array create(final int length) {
		return new Float32Array(length * BYTES_PER_ELEMENT);
	}
	
	/**
	 * Create a copy of array.
	 * 
	 * @param array
	 */
	public static Float32Array create(final TypeArray array) {
		final Float32Array result = create(array.getLength());
		result.set(array);
		return result;
	}

	/**
	 * Create an array .
	 *
	 * @param array
	 */
	public static Float32Array create(final float[] array) {
		final Float32Array result = create(array.length);
		result.floatBuffer.put(array);
		return result;
	}

	/**
	 * Returns the element at the given numeric index.
	 *
	 * @param index
	 */
	public float get(final int index) {
		return this.floatBuffer.get(index);
	}

	/**
	 * Sets the element at the given numeric index to the given value.
	 *
	 * @param index
	 * @param value
	 */
	public void set(final int index, final float value) {
		if (this.resizable)
		{
			if (this.floatBuffer == null || index >= this.floatBuffer.capacity())
			{
				final float[] val = new float[this.getLength()];
				for(int i = 0, len = this.getLength(); i < len; i++)
					val[i] = this.floatBuffer.get(i);

				// Usually floats are added in groups of 3, so add spare capacity
				this.createBuffer((index + 4) * 12);
				this.createTypedBuffer();
				this.floatBuffer.limit(index + 1);
				this.floatBuffer.put(val);
			}
			else if (index >= this.floatBuffer.limit())
			{
				this.floatBuffer.limit(index + 1);
			}
		}
		this.floatBuffer.put(index, value);
	}

	public void set(final Float32Array array) {
        super.set(array);
    }

	public void set(final Float32Array array, final int offset) {
        super.set(array, offset * BYTES_PER_ELEMENT);
    }

    public void set(final TypeArray array, final int offset) {
        super.set(array, offset * BYTES_PER_ELEMENT);
    }

	@Override
	public int getLength()
	{
		return this.floatBuffer == null ? 0 : this.floatBuffer.limit();
	}

	public String toString() {
		final float[] val = new float[this.getLength()];
		for(int i = 0, len = this.getLength(); i < len; i++)
			val[i] = this.floatBuffer.get(i);
		return Arrays.toString(val);
	}
}