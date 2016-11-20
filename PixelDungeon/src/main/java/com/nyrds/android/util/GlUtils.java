package com.nyrds.android.util;

import android.opengl.GLES20;

/**
 * Created by mike on 24.08.2016.
 */
public class GlUtils {
	static private Boolean npotMipmaps;

	static public boolean isNpotMipmapsSupported() {
		if(npotMipmaps!=null) {
			return npotMipmaps;
		}

		String exts = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		npotMipmaps = exts.indexOf("GL_ARB_texture_non_power_of_two") > 0 || exts.indexOf("GL_OES_texture_npot") > 0;

		return npotMipmaps;
	}
}
