
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.gltextures.Gradient;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Quad;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.nio.FloatBuffer;

public class SurfaceScene extends PixelScene {
	
	private static final int WIDTH	= 80;
	private static final int HEIGHT	= 112;
	
	private static final int NSTARS		= 100;
	private static final int NCLOUDS	= 5;
	
	private Camera viewport;
	@Override
	public void create() {
		
		super.create();
		
		Music.INSTANCE.play( Assets.HAPPY, true );
		Music.INSTANCE.volume( 1f );
		
		uiCamera.setVisible(false);
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.reversed = true;
		archs.setSize( w, h );
		add( archs );
		
		float vx = align( (w - WIDTH) / 2 );
		float vy = align( (h - HEIGHT) / 2 );
		
		Point s = Camera.main.cameraToScreen( vx, vy );
		viewport = new Camera( s.x, s.y, WIDTH, HEIGHT, defaultZoom );
		Camera.add( viewport );
		
		Group window = new Group();
		window.camera = viewport;
		add( window );
		
		boolean dayTime = !Dungeon.nightMode;
		
		Sky sky = new Sky( dayTime );
		sky.setScaleXY( WIDTH, HEIGHT );
		window.add( sky );
		
		if (!dayTime) {
			for (int i=0; i < NSTARS; i++) {
				float size = Random.Float();
				ColorBlock star = new ColorBlock( size, size, 0xFFFFFFFF );
				star.setX(Random.Float( WIDTH ) - size / 2);
				star.setY(Random.Float( HEIGHT ) - size / 2);
				star.am = size * (1 - star.getY() / HEIGHT);
				window.add( star );
			}
		}
		
		float range = HEIGHT * 2 / 3;
		for (int i=0; i < NCLOUDS; i++) {
			Cloud cloud = new Cloud( (NCLOUDS - 1 - i) * (range / NCLOUDS) + Random.Float( range / NCLOUDS ), dayTime );
			window.add( cloud );
		}
		
		int nPatches = (int)(sky.width() / GrassPatch.WIDTH + 1);
		
		for (int i=0; i < nPatches * 4; i++) {
			GrassPatch patch = new GrassPatch( (i - 0.75f) * GrassPatch.WIDTH / 4, HEIGHT + 1, dayTime );
			patch.brightness( dayTime ? 0.7f : 0.4f );
			window.add( patch );
		}
		
		Avatar a = new Avatar(Dungeon.hero.getHeroClass());
		a.setX(PixelScene.align( (WIDTH - a.width) / 2 ));
		a.setY(HEIGHT - a.height + 1);
		window.add( a );
		
		final Pet pet = new Pet();
		pet.rm = pet.gm = pet.bm = 1.2f;
		pet.setX(WIDTH / 2 + 2);
		pet.setY(HEIGHT - pet.height);
		window.add( pet );
		
		if (dayTime) {
			a.brightness( 1.2f );
			pet.brightness( 1.2f );
		}
		
		window.add( new TouchArea( sky ) {
			protected void onClick( Touch touch ) {
				pet.jump();
			}
		} );
		
		for (int i=0; i < nPatches; i++) {
			GrassPatch patch = new GrassPatch( (i - 0.5f) * GrassPatch.WIDTH, HEIGHT, dayTime );
			patch.brightness( dayTime ? 1.0f : 0.8f );
			window.add( patch );
		}
		
		Image frame = new Image( Assets.SURFACE );
		if (!dayTime) {
			frame.hardlight( 0xDDEEFF );
		}
		frame.frame( 0, 0, 88, 125 );
		frame.setX(vx - 4);
		frame.setY(vy - 9);
		add( frame );

        RedButton gameOver = new RedButton(R.string.SurfaceScene_GameOver) {
			protected void onClick() {
				GameLoop.switchScene( TitleScene.class );
			}
		};
		gameOver.setSize( WIDTH - 10, 20 );
		gameOver.setPos( 5 + frame.getX() + 4, frame.getY() + frame.height + 4 );
		add( gameOver );
		
		Badges.validateHappyEnd();
		
		Dungeon.gameOver();
		
		fadeIn();
	}
	
	@Override
	public void destroy() {
		Camera.remove( viewport );
		super.destroy();
	}
	
	@Override
	protected void onBackPressed() {
	}
	
	private static class Sky extends Visual {
		
		private static final int[] day		= {0xFF4488FF, 0xFFCCEEFF};
		private static final int[] night	= {0xFF001155, 0xFF335980};
		
		private final SmartTexture texture;
		private final FloatBuffer verticesBuffer;
		
		public Sky( boolean dayTime ) {
			super( 0, 0, 1, 1 );

			texture = TextureCache.getOrCreate( Sky.class, () -> new Gradient( dayTime ? day : night ));
			
			float[] vertices = new float[16];
			verticesBuffer = Quad.create();
			
			vertices[2]		= 0.25f;
			vertices[6]		= 0.25f;
			vertices[10]	= 0.75f;
			vertices[14]	= 0.75f;
			
			vertices[3]		= 0;
			vertices[7]		= 1;
			vertices[11]	= 1;
			vertices[15]	= 0;
			
			
			vertices[0] 	= 0;
			vertices[1] 	= 0;
			
			vertices[4] 	= 1;
			vertices[5] 	= 0;
			
			vertices[8] 	= 1;
			vertices[9] 	= 1;
			
			vertices[12]	= 0;
			vertices[13]	= 1;
			
			verticesBuffer.position( 0 );
			verticesBuffer.put( vertices );
		}
		
		@Override
		public void draw() {
			
			super.draw();

			NoosaScript script = NoosaScript.get();
			
			texture.bind();
			
			script.camera( camera() );
			
			script.uModel.valueM4( matrix );
			script.lighting( 
				rm, gm, bm, am, 
				ra, ga, ba, aa );
			
			script.drawQuad( verticesBuffer );
		}
	}
	
	private static class Cloud extends Image {
		
		private static int lastIndex = -1;
		
		public Cloud( float y, boolean dayTime ) {
			super( Assets.SURFACE );
			
			int index;
			do {
				index = Random.Int( 3 );
			} while (index == lastIndex);
			
			switch (index) {
			case 0:
				frame( 88, 0, 49, 20 );
				break;
			case 1:
				frame( 88, 20, 49, 22 );
				break;
			case 2:
				frame( 88, 42, 50, 18 );
				break;
			}
			
			lastIndex = index;
			
			this.setY(y);
			
			setScale( 1 - y / HEIGHT );
			setX(Random.Float( WIDTH + width() ) - width());
			speed.x = scale.x * (dayTime ? +8 : -8);
			
			if (dayTime) {
				tint( 0xCCEEFF, 1 - scale.y );
			} else {
				rm = gm = bm = +3.0f;
				ra = ga = ba = -2.1f;
			}
		}
		
		@Override
		public void update() {
			super.update();
			if (speed.x > 0 && getX() > WIDTH) {
				setX(-width());
			} else if (speed.x < 0 && getX() < -width()) {
				setX(WIDTH);
			}
		}
	}

	private static class Avatar extends Image {
		
		private static final int WIDTH	= 24;
		private static final int HEIGHT	= 28;
		
		public Avatar( HeroClass cl ) {
			super( Assets.AVATARS );
			frame( TextureCache.getFilm( texture, WIDTH, HEIGHT ).get( cl.classIndex()) );
		}
	}
	
	private static class Pet extends MovieClip implements MovieClip.Listener {

		private final Animation idle;
		private final Animation jump;

		public Pet() {
			super(Assets.PET);

			TextureFilm frames = TextureCache.getFilm(texture, 16, 16);

			idle = new Animation(2, true);
			idle.frames(frames, 0, 0, 0, 0, 0, 0, 1);

			jump = new Animation(10, false);
			jump.frames(frames, 2, 3, 4, 5, 6);

			listener = this;

			play(idle);
		}

		public void jump() {
			play(jump);
		}

		@Override
		public void onComplete(Animation anim) {
			if (anim == jump) {
				play(idle);
			}
		}
	}

	private static class GrassPatch extends Image {

		public static final int WIDTH  = 16;
		public static final int HEIGHT = 14;

		private final float tx;
		private final float ty;

		private double a = Random.Float(5);
		private double angle;

		private final boolean forward;

		public GrassPatch(float tx, float ty, boolean forward) {

			super(Assets.SURFACE);

			frame(88 + Random.Int(4) * WIDTH, 60, WIDTH, HEIGHT);

			this.tx = tx;
			this.ty = ty;

			this.forward = forward;
		}

		@Override
		public void update() {
			super.update();
			a += Random.Float(GameLoop.elapsed * 5);
			angle = (2 + Math.cos(a)) * (forward ? +0.2 : -0.2);

			setScaleY ((float) Math.cos(angle));

			setX(tx + (float) Math.tan(angle) * width);
			setY(ty - scale.y * height);
		}

		@Override
		protected void updateMatrix() {
			super.updateMatrix();
			Matrix.skewX(matrix, (float) (angle / Matrix.G2RAD));
		}
	}
}
