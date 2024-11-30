package com.watabou.glwrap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Quad {

	// 0---1
	// | \ |
	// 3---2
	//private static final short[] VALUES = {0, 1, 2, 0, 2, 3};
	
	//public static final int SIZE = VALUES.length;
	public static final int SIZE = 6;
	
	private static ShortBuffer indices;
	private static int indexSize = 0;
	
	public static FloatBuffer create() {
		return ByteBuffer.
			allocateDirect( 16 * Float.SIZE / 8 ).
			order( ByteOrder.nativeOrder() ).
			asFloatBuffer();
	}
	
	public static FloatBuffer createSet( int size ) {
		return ByteBuffer.
			allocateDirect( size * 16 * Float.SIZE / 8 ).
			order( ByteOrder.nativeOrder() ).
			asFloatBuffer();
	}
	
	public static ShortBuffer getIndices( int size ) {
		
		if (size > indexSize) {
			indexSize = size;
			indices = ByteBuffer.
				allocateDirect( size * SIZE * Short.SIZE / 8 ).
				order( ByteOrder.nativeOrder() ).
				asShortBuffer();
			
			short[] values = new short[size * 6];
			int pos = 0;
			int limit = size * 4;
			for (int ofs=0; ofs < limit; ofs += 4) {
				values[pos++] = (short)(ofs + 0);
				values[pos++] = (short)(ofs + 1);
				values[pos++] = (short)(ofs + 2);
				values[pos++] = (short)(ofs + 0);
				values[pos++] = (short)(ofs + 2);
				values[pos++] = (short)(ofs + 3);
			}
			
			indices.put( values );
			indices.position( 0 );
		}
		
		return indices;
	}
	
	public static void fill( float[] v, 
		float x1, float x2, float y1, float y2, 
		float u1, float u2, float v1, float v2 ) {
		
		v[0] = x1;
		v[1] = y1;
		v[2] = u1;
		v[3] = v1;
		
		v[4] = x2;
		v[5] = y1;
		v[6] = u2;
		v[7] = v1;
		
		v[8] = x2;
		v[9] = y2;
		v[10]= u2;
		v[11]= v2;
		
		v[12]= x1;
		v[13]= y2;
		v[14]= u1;
		v[15]= v2;
	}
	
	public static void fillXY( float[] v, float x1, float x2, float y1, float y2 ) {
		
		v[0] = x1;
		v[1] = y1;
		
		v[4] = x2;
		v[5] = y1;
		
		v[8] = x2;
		v[9] = y2;
		
		v[12]= x1;
		v[13]= y2;
	}
	
	public static void fillUV( float[] v, float u1, float u2, float v1, float v2 ) {
		
		v[2] = u1;
		v[3] = v1;
		
		v[6] = u2;
		v[7] = v1;
		
		v[10]= u2;
		v[11]= v2;
		
		v[14]= u1;
		v[15]= v2;
	}
}
