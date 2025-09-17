
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Fraction;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Locale;

public abstract class NPC extends Mob {
	protected NPC() {
		hp(ht(1));
		expForKill = 0;
		carcassChance= 0f;

		setState(MobAi.getStateByClass(Passive.class));
		
		fraction = Fraction.NEUTRAL;
	}

	@Override
	public boolean act() {

		int pos = getPos();

		ItemUtils.throwItemAway(pos);

		LevelObject levelObject = level().getTopLevelObject(pos);
		if (levelObject != null) {
			int newPos = level().getEmptyCellNextTo(pos);
			if (level().cellValid(newPos) && newPos != pos) {
				WandOfBlink.appear(this, newPos);
			}
		}

		getSprite().turnTo( pos, Dungeon.hero.getPos() );

		// Call parent act method to preserve existing behavior
		return super.act();
	}

	@Override
	public boolean friendly(@NotNull Char chr) {
		if(fraction.belongsTo(Fraction.NEUTRAL)) {
			return true;
		} else {
			return super.friendly(chr);
		}
	}

	@Override
	public void beckon( int cell ) {
	}
	
	@Override
	public boolean interact(final Char hero){
		swapPosition(hero);
		return true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}

	public void fromJson(JSONObject mobDesc) {
		super.fromJson(mobDesc);

		setState(mobDesc.optString("aiState","Passive").toUpperCase(Locale.ROOT));
	}

	public void sayRandomPhrase(int ...phrases) {
		int index = Random.Int(0, phrases.length);
        say(StringsManager.getVar(phrases[index]));
	}

	public boolean exchangeItem(@NotNull Char hero , String itemClass, String rewardClass) {
		Item item = hero.getItem(itemClass);

		if(!item.valid()) {
			return false;
		}

		item.removeItemFrom(hero);

		Item reward = ItemFactory.itemByName(rewardClass);
		hero.collectAnimated(reward);
		return true;
	}
}
