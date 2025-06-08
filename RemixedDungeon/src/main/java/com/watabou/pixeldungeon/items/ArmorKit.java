
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ArmorKit extends Item {
	
	private static final float TIME_TO_UPGRADE = 2;
	
	private static final String AC_APPLY = "ArmorKit_ACAplly";
	
	{
        name = StringsManager.getVar(R.string.ArmorKit_Name);
		image = ItemSpriteSheet.KIT;
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_APPLY );
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals(AC_APPLY)) {
            GameScene.selectItem(chr, itemSelector, WndBag.Mode.ARMOR, StringsManager.getVar(R.string.ArmorKit_SelectArmor));
		} else {
			super._execute(chr, action );
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	private void upgrade( Armor armor ) {
		Char owner = getOwner();

		detach( owner.getBelongings().backpack );

		owner.getSprite().centerEmitter().start( Speck.factory( Speck.KIT ), 0.05f, 10 );

        GLog.w(StringsManager.getVar(R.string.ArmorKit_Upgraded), armor.name() );
		
		Armor classArmor = ClassArmor.upgrade( owner, armor );
		if (owner.getItemFromSlot(Belongings.Slot.ARMOR) == armor) {
			owner.getBelongings().setItemForSlot(classArmor, Belongings.Slot.ARMOR);
			owner.updateSprite();
		} else {
			armor.detach( owner.getBelongings().backpack );
			owner.collect(classArmor);
		}

		owner.doOperate(TIME_TO_UPGRADE);

		Sample.INSTANCE.play( Assets.SND_EVOKE );
	}
	
	private final WndBag.Listener itemSelector = (item, selector) -> {
		if (item != null) {
			ArmorKit.this.upgrade( (Armor)item );
		}
	};
}
