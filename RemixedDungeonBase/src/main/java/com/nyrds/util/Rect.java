package com.nyrds.util;

import com.nyrds.Packable;

public class Rect {

	@Packable
	public int left;
	@Packable
	public int top;
	@Packable
	public int right;
	@Packable
	public int bottom;
	
	public Rect() {
		this( 0, 0, 0, 0 );
	}
	
	public Rect( Rect rect ) {
		this( rect.left, rect.top, rect.right, rect.bottom );
	}
	
	public Rect( int left, int top, int right, int bottom ) {
		this.left	= left;
		this.top	= top;
		this.right	= right;
		this.bottom	= bottom;
	}
	
	public int width() {
		return right - left;
	}
	
	public int height() {
		return bottom - top;
	}
	
	public int square() {
		return (right - left) * (bottom - top);
	}
	
	public Rect set( int left, int top, int right, int bottom ) {
		this.left	= left;
		this.top	= top;
		this.right	= right;
		this.bottom	= bottom;
		return this;
	}
	
	public Rect set( Rect rect ) {
		return set( rect.left, rect.top, rect.right, rect.bottom );
	}
	
	public boolean isEmpty() {
		return right <= left || bottom <= top;
	}
	
	public Rect setEmpty() {
		left = right = top = bottom = 0;
		return this;
	}
	
	public Rect intersect( Rect other ) {
		Rect result = new Rect();
		result.left		= Math.max( left, other.left );
		result.right	= Math.min( right, other.right );
		result.top		= Math.max( top, other.top );
		result.bottom	= Math.min( bottom, other.bottom );
		return result;
	}
	
	public Rect union( int x, int y ) {
		if (isEmpty()) {
			return set( x, y, x + 1, y + 1 );
		} else {
			if (x < left) {
				left = x;
			} else if (x >= right) {
				right = x + 1;
			}
			if (y < top) {
				top = y;
			} else if (y >= bottom) {
				bottom = y + 1;
			}
			return this;
		}
	}
	
	public Rect union( Point p ) {
		return union( p.x, p.y );
	}
	
	public boolean inside( Point p ) {
		return p.x >= left && p.x < right && p.y >= top && p.y < bottom;
	}
	
	public Rect shrink( int d ) {
		return new Rect( left + d, top + d, right - d, bottom - d );
	}
	
	public Rect shrink() {
		return shrink( 1 );
	}
	
}
