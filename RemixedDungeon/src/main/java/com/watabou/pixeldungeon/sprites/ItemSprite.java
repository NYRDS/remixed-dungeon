
package com.watabou.pixeldungeon.sprites;

import android.graphics.Bitmap;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.nyrds.util.ModError;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.val;

public class ItemSprite extends MovieClip {

	public static final int SIZE = 16;

	private static final float DROP_INTERVAL = 0.4f;

	protected TextureFilm film;

	protected Heap heap;

	@Nullable
	private Glowing glowing;

	private Image overlay;

	private float   phase;
	private boolean glowUp;

	private float dropInterval;

	public ItemSprite() {
		this(Assets.ITEMS, ItemSpriteSheet.SMTH, null);
	}

	public ItemSprite(@NotNull Item item) {
		super();
		view(item);
	}

	public ItemSprite(String file, int imageIndex, Glowing glowing) {
		super();
		view(file, imageIndex, glowing);
	}

	public ItemSprite(Heap heap) {
		super();
		setIsometricShift(true);
		link(heap);
	}

	private void updateTexture(String file) {
		texture(file);
		film = TextureCache.getFilm(texture, SIZE, SIZE);
	}

	protected void originToCenter() {
		setOrigin(scale.x * SIZE / 2, scale.y * SIZE / 2);
	}

	public void link() {
		link(heap);
	}

	public void link(@NotNull Heap heap) {
		this.heap = heap;
		float scale = heap.scale();
		setScaleXY(scale, scale);
		if(heap.type == Heap.Type.HEAP) {
			view(heap.peek());
		} else {
			view(heap.imageFile(), heap.image(), heap.glowing());
		}
		place(heap.pos);
	}

	@Override
	public void kill() {
		super.kill();
		heap = null;
	}

	@Override
	public void revive() {
		super.revive();

		speed.set(0);
		acc.set(0);
		dropInterval = 0;

		heap = null;
	}


	protected PointF worldToCamera(int cell) {
		final int csize = DungeonTilemap.SIZE;

		return new PointF(
				Dungeon.level.cellX(cell) * csize + (csize - SIZE * scale.x) * 0.5f,
				Dungeon.level.cellY(cell) * csize + (csize - SIZE * scale.y) * 0.5f
		);
	}

	public void place(int p) {
		point(worldToCamera(p));
	}

	public void drop() {
		if (heap != null && heap.isEmpty()) {
			return;
		}

		dropInterval = DROP_INTERVAL;

		speed.set(0, -100);
		acc.set(0, -speed.y / DROP_INTERVAL * 2);

		if (getVisible() && heap != null && heap.peek() instanceof Gold) {
			CellEmitter.center(heap.pos).burst(Speck.factory(Speck.COIN), 5);
			Sample.INSTANCE.play(Assets.SND_GOLD, 1, 1, Random.Float(0.9f, 1.1f));
		}
	}

	public void drop(int from) {
		if (heap!= null && heap.pos == from) {
			drop();
		} else {
			float px = getX();
			float py = getY();
			drop();
			place(from);
			speed.offset((px - getX()) / DROP_INTERVAL, (py - getY()) / DROP_INTERVAL);
		}
	}

	public ItemSprite view(@NotNull Item item) {

		if(item.overlayIndex()>=0) {
			overlay = new Image(item.overlayFile(),16,item.overlayIndex());
		} else {
			overlay = null;
		}

		val customImage = item.getCustomImage();
		if(customImage!= null) {
			if ((this.glowing = item.glowing()) == null) {
				resetColor();
			}
			copy(customImage);
			return this;
		}

		return view(item.imageFile(), item.image(), item.glowing());
	}

	public ItemSprite view(String file, int image, Glowing glowing) {
		updateTexture(file);
		try {
			frame(film.get(image));
		}catch (Exception e) {
			throw new ModError("Something wrong with "+file+" frame: "+ image);
		}
		if ((this.glowing = glowing) == null) {
			resetColor();
		}
		return this;
	}

	@Override
	public void update() {
		super.update();

		// Visibility
		setVisible(heap == null || Dungeon.isCellVisible(heap.pos));

		// Dropping
		final float elapsed = GameLoop.elapsed;
		if (dropInterval > 0 && (dropInterval -= elapsed) <= 0) {

			speed.set(0);
			acc.set(0);
			place(heap.pos);

			if (Dungeon.level.water[heap.pos]) {
				GameScene.ripple(heap.pos);
			}
		}

		if (getVisible()) {
			if (glowing != null && glowing != Glowing.NO_GLOWING) {
				if (glowUp && (phase += elapsed) > glowing.period) {
					glowUp = false;
					phase = glowing.period;
				} else if (!glowUp && (phase -= elapsed) < 0) {
					glowUp = true;
					phase = 0;
				}

				float value = phase / glowing.period * 0.6f;

				rm = gm = bm = 1 - value;
				ra = glowing.red * value;
				ga = glowing.green * value;
				ba = glowing.blue * value;
			}
		}
	}

	@Override
	public void draw() {
		super.draw();

		if(overlay != null) {
			NoosaScript script = NoosaScript.get();

			overlay.texture.bind();
			overlay.updateVerticesBuffer();
			script.drawQuad(overlay.getVerticesBuffer());
		}
	}

	public static int pick(int index, int x, int y) {
		Bitmap bmp = TextureCache.get(Assets.ITEMS).bitmap;
		int rows = bmp.getWidth() / SIZE;
		int row = index / rows;
		int col = index % rows;
		return bmp.getPixel(col * SIZE + x, row * SIZE + y);
	}
}
