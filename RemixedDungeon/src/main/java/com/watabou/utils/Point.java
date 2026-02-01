

package com.watabou.utils;

import org.jetbrains.annotations.NotNull;

public class Point {

	public int x;
	public int y;
	
	public Point() {
	}
	
	public Point( int x, int y ) {
		this.x = x;
		this.y = y;
	}
	
	public Point( Point p ) {
		this.x = p.x;
		this.y = p.y;
	}
	
	public void set(int x, int y ) {
		this.x = x;
		this.y = y;
	}
	
	public Point set( Point p ) {
		x = p.x;
		y = p.y;
		return this;
	}
	
	@NotNull
	@Override
	public Point clone() {
		return new Point( this );
	}
	
	public Point scale( float f ) {
		this.x *= f;
		this.y *= f;
		return this;
	}
	
	public Point offset( int dx, int dy ) {
		x += dx;
		y += dy;
		return this;
	}
	
	public Point offset( Point d ) {
		x += d.x;
		y += d.y;
		return this;
	}
	
	@Override
	public boolean equals( Object obj ) {
		if (obj instanceof Point) {
			Point p = (Point)obj;
			return p.x == x && p.y == y;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return x*0xffff + y;
	}
}
