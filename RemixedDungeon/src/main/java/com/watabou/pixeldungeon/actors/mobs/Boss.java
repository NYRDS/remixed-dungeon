package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.game.ModQuirks;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mobs.common.ShadowLord;
import com.nyrds.platform.audio.Music;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class Boss extends Mob {

	private static final String BATTLE_MUSIC = "battleMusic";
	private static final String BATTLE_MUSIC_FALLBACK = "battleMusicFallback";

	@Nullable
	private String battleMusic;

	public Boss() {
		addResistance(Death.class);
		addResistance(ScrollOfPsionicBlast.class);
		if(ModQuirks.mobLeveling) {
			lvl(Random.NormalIntRange(1, (int) (2 * RemixedDungeon.getDifficultyFactor())));
		}
		maxLvl = 50;
		isBoss = true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}

	@Override
	public boolean act() {
		if (state instanceof Hunting) {
			if (battleMusic != null) {
				Music.INSTANCE.play(battleMusic, true);
			}
		}
		return super.act();
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		GameScene.playLevelMusic();
		GameScene.bossSlain();

		level().unseal();
		super.die(cause);
	}

	@Override
	protected void setupCharData() {
		super.setupCharData();
		battleMusic = getClassDef().optString(BATTLE_MUSIC, ModdingMode.NO_FILE);
		if(ModdingMode.isSoundExists(battleMusic)) {
			return;
		}

		battleMusic = getClassDef().optString(BATTLE_MUSIC_FALLBACK, null);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		if(!(this instanceof ShadowLord)) {
			if (getBelongings().getItem(SkeletonKey.class) == null) { //FIXUP for old saves
				collect(new SkeletonKey());
			}
		}
	}

	@Override
	public boolean isBoss() {
		return true;
	}
}
