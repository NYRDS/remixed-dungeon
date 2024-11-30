
package com.watabou.pixeldungeon.sprites;

import static com.watabou.pixeldungeon.sprites.ModernHeroSpriteDef.HERO_MODERN_SPRITES_DESC_HERO_JSON;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Group;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.actors.hero.Hero;

import org.jetbrains.annotations.NotNull;

public class DummySprite extends HeroSpriteDef {

	public static final DummySprite instance = new DummySprite();
	public static final Group dummyGroup = new Group();

	public DummySprite() {
		super(HERO_MODERN_SPRITES_DESC_HERO_JSON,0);
		
		texture("hero/empty.png");
		
		TextureFilm frames = TextureCache.getFilm( texture, 1, 1 );

		idle = new Animation( 1, true );
		idle.frames( frames, 0);

		attack = new Animation(1, false);
		attack.frames(frames, 0);

		zap = new Animation(1, false);
		zap.frames(frames, 0);

		run = new Animation( 1, true );
		run.frames( frames, 0);
		
		die = new Animation( 1, false );
		die.frames( frames, 0 );

		play( idle );
	}

	@Override
	public void add(State state) {
	}

	@Override
	public void remove(State state) {
	}

	@Override
	public void removeAllStates() {
	}

	@Override
	public int blood() {
		return 0;
	}

	@Override
	public boolean getVisible() {
		return false;
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public Group getParent() {
		return dummyGroup;
	}

	@Override
	public String[] getLayersDesc() {
		return new String[0];
	}

	@Override
	public @NotNull String getDeathEffect() {
		return "";
	}

	@Override
	public void heroUpdated(Hero hero) {

	}
}
