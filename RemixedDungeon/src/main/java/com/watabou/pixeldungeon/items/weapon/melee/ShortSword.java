
package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings.Slot;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.items.weapon.missiles.Boomerang;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ShortSword extends MeleeWeapon {

	public static final String AC_REFORGE = "ShortSword_ACReforge";
	
	private static final float TIME_TO_REFORGE	= 2f;

	{
		image = ItemSpriteSheet.SHORT_SWORD;
	}
	
	public ShortSword() {
		super( 1, 1f, 1f );
		animation_class = SWORD_ATTACK;
		
		setSTR(11);
		MAX = 12;
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (level() > 0) {
			actions.add( AC_REFORGE );
		}
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals(AC_REFORGE)) {
			chr.getBelongings().setSelectedItem(this);
            GameScene.selectItem(chr, itemSelector, WndBag.Mode.WEAPON, StringsManager.getVar(R.string.ShortSword_Select));
		} else {
			super._execute(chr, action );
		}
	}
	
	@Override
	public String desc() {
        return StringsManager.getVar(R.string.ShortSword_Info);
    }
	
	private final WndBag.Listener itemSelector = (item, selector) -> {
		if (item != null) {
			selector.getBelongings().removeItem(this);
			Sample.INSTANCE.play( Assets.SND_EVOKE );
			ScrollOfUpgrade.upgrade( selector );
			ItemUtils.evoke( selector );

            GLog.w(StringsManager.getVar(R.string.ShortSword_Reforged), item.name() );

			((MeleeWeapon)item).safeUpgrade();
			selector.spend( TIME_TO_REFORGE );

			Badges.validateItemLevelAcquired( item );
		}
		selector.updateSprite();
		QuickSlot.refresh(selector);
	};
}
