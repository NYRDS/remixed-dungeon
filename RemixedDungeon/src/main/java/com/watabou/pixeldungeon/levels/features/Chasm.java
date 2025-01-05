
package com.watabou.pixeldungeon.levels.features;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Cripple;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Chasm implements Doom {

	public static boolean jumpConfirmed = false;
	
	public static void heroJump( final Hero hero ) {
		GameScene.show(
				new WndChasmJump(hero)
		);
	}

	public static void charFall(int pos, Char chr) {
		if (chr instanceof Hero) {
			heroFall(pos, (Hero)chr);
			return;
		}

		if(chr instanceof Mob) {
			mobFall((Mob)chr);
		}
	}

	public static void heroLand(Hero hero) {

		CharSprite heroSprite = hero.getSprite();

		heroSprite.setVisible(true);
		heroSprite.alpha(1f);

		heroSprite.burst( heroSprite.blood(), 10 );
		Camera.main.shake( 4, 0.2f );
		
		Buff.prolong( hero, Cripple.class, Cripple.DURATION );
		hero.damage( Random.IntRange( hero.ht() / 3, hero.ht() / 2 ), new Chasm() );
	}

	private static void mobFall( Mob mob ) {
		mob.die(new Chasm());

		if(Dungeon.hero.myMove()) {
			Badges.validateBadge(Badges.Badge.THIS_IS_SPARTA);
		}

		mob.getSprite().fall();
	}

	private static void heroFall(int pos, Hero hero) {

		jumpConfirmed = false;

		Sample.INSTANCE.play( Assets.SND_FALLING );
		//hero.releasePets();

		if (hero.isAlive()) {
			hero.clearActions();

			if (Dungeon.level instanceof RegularLevel) {
				Room room = ((RegularLevel)Dungeon.level).room( pos );
				InterlevelScene.fallIntoPit = room != null && room.type == Room.Type.WEAK_FLOOR;
			} else {
				InterlevelScene.fallIntoPit = false;
			}
			InterlevelScene.Do(InterlevelScene.Mode.FALL);
		} else {
			hero.getSprite().setVisible(false);
		}
	}

	@Override
	public void onHeroDeath() {
		Badges.validateDeathFromFalling();

		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.FALL), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Chasm_Info));
	}

	@Override
	public String getEntityKind() {
		return getClass().getSimpleName();
	}

	@Override
	public String name() {
		return getEntityKind();
	}

}
