package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import java.nio.Buffer;

/**
 * WebGL has no client-side vertex/index arrays: draw data must live in GL
 * buffer objects. The game's render scripts keep their quad data in plain
 * direct NIO buffers (works on GLES via client arrays) - this helper uploads
 * those into GL buffers so the scripts can draw with offsets.
 */
public class GlBuffers {

	/**
	 * Binds {@code target} to a lazily created buffer and uploads {@code data}
	 * (position..limit) into it. Returns the buffer handle.
	 */
	public static int upload(int handle, int target, Buffer data, int bytesPerElement, boolean dynamic) {
		if (handle == 0) {
			java.nio.IntBuffer out = java.nio.ByteBuffer.allocateDirect(4)
					.order(java.nio.ByteOrder.nativeOrder()).asIntBuffer();
			Gdx.gl20.glGenBuffers(1, out);
			handle = out.get(0);
		}
		Gdx.gl20.glBindBuffer(target, handle);
		Gdx.gl20.glBufferData(target, data.remaining() * bytesPerElement, data,
				dynamic ? Gdx.gl20.GL_DYNAMIC_DRAW : Gdx.gl20.GL_STATIC_DRAW);
		return handle;
	}
}
