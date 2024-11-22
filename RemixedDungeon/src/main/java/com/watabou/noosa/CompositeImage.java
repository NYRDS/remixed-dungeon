package com.watabou.noosa;

import com.nyrds.platform.gl.NoosaScript;

import java.util.ArrayList;
import java.util.List;

public class CompositeImage extends Image {

    private final ArrayList<Image> mLayers = new ArrayList<>();

    public CompositeImage() {
        super();
    }

    public CompositeImage(List<Image> imgs) {
        this();
        if (imgs.isEmpty()) {
            return;
        }
        copy(imgs.get(0));
        for (int i = 1; i < imgs.size(); ++i) {
            addLayer(imgs.get(i));
        }
    }


    public CompositeImage(Object tx) {
        this();
        texture(tx);
    }

    public void addLayer(Image img) {
        mLayers.add(img);
    }

    @Override
    public void draw() {
        super.draw();

        NoosaScript script = NoosaScript.get();

        for (Image img : mLayers) {
            img.texture.bind();
            img.updateVerticesBuffer();
            script.drawQuad(img.getVerticesBuffer());
        }
    }
}
