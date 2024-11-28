
package com.watabou.noosa;

import com.nyrds.pixeldungeon.windows.IPlaceable;
import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gl.NoosaScript;
import com.nyrds.util.ModError;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Quad;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;

public class Image extends Visual implements IPlaceable {

    public SmartTexture texture;
    protected RectF frame;

    public boolean flipHorizontal;
    public boolean flipVertical;

    protected final float[] vertices;
    private final FloatBuffer verticesBuffer;

    protected boolean dirty;

    private static final RectF defaultFrame = new RectF(0, 0, 1, 1);


    public Image() {
        super(0, 0, 0, 0);

        vertices = new float[16];
        verticesBuffer = Quad.create();
    }

    public Image(Image src) {
        this();
        copy(src);
    }

    public Image(Object tx) {
        this();
        texture(tx);
    }

    public Image(Object tx, int left, int top, int width, int height) {
        this(tx);
        frame(texture.uvRect(left, top, left + width, top + height));
    }

    public Image(Object tx, int cellSize, int index) {
        this(tx);

        TextureFilm film = TextureCache.getFilm(tx, cellSize, cellSize);

        RectF frame = film.get(index);
        if (frame == null) {
            throw new ModError(Utils.format("bad index %d for image %s", index, String.valueOf(TextureCache.getKey(TextureCache.get(tx)))));
        }
        frame(frame);
    }

    public void texture(Object tx) {
        texture = tx instanceof SmartTexture ? (SmartTexture) tx : TextureCache.get(tx);
        frame(defaultFrame);
    }

    public void frame(@NotNull RectF frame) {
        this.frame = frame;

        if(frame.top < 0 || frame.top > 1 || frame.left < 0 || frame.left > 1 || frame.bottom < 0 || frame.bottom > 1 || frame.right < 0 || frame.right > 1
            || frame.top > frame.bottom || frame.left > frame.right) {
            throw new ModError("frame out of bounds");
        }

        setWidth(frame.width() * texture.width);
        setHeight(frame.height() * texture.height);

        updateFrame();
        updateVertices();
    }

    public void frame(int left, int top, int width, int height) {
        frame(texture.uvRect(left, top, left + width, top + height));

        if(frame.top < 0 || frame.top > 1 || frame.left < 0 || frame.left > 1 || frame.bottom < 0 || frame.bottom > 1 || frame.right < 0 || frame.right > 1
                || frame.top > frame.bottom || frame.left > frame.right) {
            throw new ModError("frame out of bounds");
        }

    }

    public RectF frame() {
        return new RectF(frame);
    }

    public void copy(Image other) {
        texture = other.texture;
        frame = new RectF(other.frame);

        setWidth(other.width);
        setHeight(other.height);

        updateFrame();
        updateVertices();
    }

    protected void updateFrame() {

        if (flipHorizontal) {
            vertices[2] = frame.right;
            vertices[6] = frame.left;
            vertices[10] = frame.left;
            vertices[14] = frame.right;
        } else {
            vertices[2] = frame.left;
            vertices[6] = frame.right;
            vertices[10] = frame.right;
            vertices[14] = frame.left;
        }

        if (flipVertical) {
            vertices[3] = frame.bottom;
            vertices[7] = frame.bottom;
            vertices[11] = frame.top;
            vertices[15] = frame.top;
        } else {
            vertices[3] = frame.top;
            vertices[7] = frame.top;
            vertices[11] = frame.bottom;
            vertices[15] = frame.bottom;
        }

        dirty = true;
    }

    protected void updateVertices() {

        vertices[0] = 0;
        vertices[1] = 0;

        vertices[4] = width;
        vertices[5] = 0;

        vertices[8] = width;
        vertices[9] = height;

        vertices[12] = 0;
        vertices[13] = height;

        dirty = true;
    }

    public void updateVerticesBuffer() {
        if (dirty) {
            verticesBuffer.position(0);
            verticesBuffer.put(vertices);
            dirty = false;
        }
    }

    @Override
    public void draw() {

        super.draw();

        NoosaScript script = NoosaScript.get();

        texture.bind();

        script.camera(camera());

        script.uModel.valueM4(matrix);
        script.lighting(
                rm, gm, bm, am,
                ra, ga, ba, aa);

        //updateFrame();
        updateVerticesBuffer();
/*
        if(verticesBuffer.get(2) < 0 || verticesBuffer.get(2) > 1 || verticesBuffer.get(6) < 0 || verticesBuffer.get(6) > 1 || verticesBuffer.get(3) < 0 || verticesBuffer.get(3) > 1 || verticesBuffer.get(11) < 0 || verticesBuffer.get(11) > 1) {
            PUtil.slog("vertices", String.format("left: %f, top: %f, right: %f, bottom: %f", verticesBuffer.get(2), verticesBuffer.get(3), verticesBuffer.get(6), verticesBuffer.get(11)));
            PUtil.slog("frame", String.format("left: %f, top: %f, right: %f, bottom: %f", frame.left, frame.top, frame.right, frame.bottom));
            PUtil.slog("texture", String.format("width: %d, height: %d", texture.width, texture.height));
            return;
            //throw new AssertionError();
        }
*/
        script.drawQuad(verticesBuffer);
    }

    public float bottom() {
        return super.bottom();
    }

    public FloatBuffer getVerticesBuffer() {
        return verticesBuffer;
    }

    @Override
    public IPlaceable shadowOf() {
        return super.shadowOf();
    }
}
