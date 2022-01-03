package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.Fleeing;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.Darkness;
import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.actors.mobs.Boss;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Shadow;
import com.watabou.pixeldungeon.actors.mobs.WalkingType;
import com.watabou.pixeldungeon.actors.mobs.Wraith;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.Callback;

import org.jetbrains.annotations.NotNull;

/**
 * Created by DeadDie on 13.02.2016
 */
public class ShadowLord extends Boss implements IZapper {

	@Packable
	private boolean levelCreated         = false;

	@Packable
	private int cooldown                 = -1;

	public ShadowLord() {
		hp(ht(260));
		baseDefenseSkill = 40;
		baseAttackSkill  = 30;
		dmgMin = 30;
		dmgMax = 40;
		dr = 40;

		exp = 60;

		collect(new ScrollOfWeaponUpgrade());

		walkingType = WalkingType.ABSOLUTE;
	}

	private void spawnShadow() {
		int cell = level().getSolidCellNextTo(getPos());

		if (level().cellValid(cell)) {
			Mob mob = new Shadow();

			mob.setState(MobAi.getStateByClass(Wandering.class));

			WandOfBlink.appear(mob, cell);
		}
	}

	private void spawnWraith() {
		for (int i = 0; i < 4; i++) {
			int cell = level().getEmptyCellNextTo(getPos());

			if (level().cellValid(cell)) {
				Wraith.spawnAt(cell);
			}
		}
	}

	private void twistLevel() {

		if(!isAlive()) {
			return;
		}

		Level level = level();

		if(!levelCreated)
		{
			LevelTools.makeEmptyLevel(level, false);
			LevelTools.buildShadowLordMaze(level, 6);
			levelCreated = true;
		}


		int cell = level.getRandomLevelObjectPosition(LevelObjectsFactory.PEDESTAL);
		if (level.cellValid(cell)) {
			if (Actor.findChar(cell) == null) {
				Mob mob = Crystal.makeShadowLordCrystal();
				mob.setPos(cell);
				level.spawnMob(mob);

				final CharSprite sprite = mob.getSprite();

				sprite.alpha( 0 );
				GameScene.addToMobLayer(new AlphaTweener(sprite, 1, 0.4f));


				sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
				Sample.INSTANCE.play( Assets.SND_TELEPORT );

				int x, y;
				x = level.cellX(cell);
				y = level.cellY(cell);

				level.fillAreaWith(Darkness.class, x - 2, y - 2, 5, 5, 1);
			} else {
				damage(ht() / 9, this);
			}
		}
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return level().distance(getPos(), enemy.getPos()) < 4 && Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	protected void fx(int cell, Callback callback) {
		MagicMissile.purpleLight(getSprite().getParent(), getPos(), cell, callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
		getSprite().setVisible(false);
	}

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src) {
		super.damage(dmg, src);
		if (src != this) {
			if (dmg > 0 && cooldown < 0) {
				setState(MobAi.getStateByClass(Fleeing.class));

				Char jumpFrom = this;

				if (src instanceof Char) {
					jumpFrom = (Char)src;
				}
				Char finalJumpFrom = jumpFrom;

				CharUtils.blinkAway(this,
							(level, cell) -> level.distance(cell, finalJumpFrom.getPos())>3 && Actor.findChar(cell) == null);

				twistLevel();
				cooldown = 10;
			}
		}
	}

	@Override
    public boolean act() {
		cooldown--;

		if (getState() instanceof Fleeing) {
			if (cooldown < 0) {
				setState(MobAi.getStateByClass(Wandering.class));
				if (Math.random() < 0.7) {
					spawnWraith();
				} else {
					spawnShadow();
				}

                yell(StringsManager.getVar(R.string.ShadowLord_Intro));
			}
		}

		if (level().blobAmountAt(Darkness.class, getPos()) > 0 && hp() < ht()) {
			heal((ht() - hp()) / 4, level().blobs.get(Darkness.class));
		}

		if (level().blobAmountAt(Foliage.class, getPos()) > 0) {
			getSprite().emitter().burst(Speck.factory(Speck.BONE), 1);
			damage(1, this);
		}

		return super.act();
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		super.die(cause);
        yell(StringsManager.getVar(R.string.ShadowLord_Death));
		LevelTools.makeEmptyLevel(level(), false);
		level().unseal();
		Badges.validateBossSlain(Badges.Badge.SHADOW_LORD_SLAIN);
	}
}
