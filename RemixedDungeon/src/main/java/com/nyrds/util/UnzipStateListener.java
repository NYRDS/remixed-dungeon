package com.nyrds.util;

public interface UnzipStateListener {
	void UnzipComplete(Boolean result);
	void UnzipProgress(Integer unpacked);
}
