package com.watabou.noosa.particles;

public class DummyEmitter extends Emitter {

    public DummyEmitter() {
    }

    @Override
    public void update() {
        kill();
    }

    @Override
    protected void emit(int index) {
    }

    @Override
    public void draw() {
    }
}
