package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndBag;

import java.util.ArrayList;

public abstract class KindOfBow extends MeleeWeapon {

	{
		imageFile = "items/ranged.png";
	}

	protected static final String AC_CHOOSE_ARROWS   = "KindOfBow_ChooseArrows";

	private Class <? extends Arrow> arrowType = Arrow.class;
	
	public KindOfBow(int tier, float acu, float dly ) {
		super(tier, acu, dly);
	}
	
	public void useArrowType(Arrow arrow){
		arrowType = arrow.getClass();
	}
	
	public Class <? extends Arrow> arrowType() {
		return arrowType;
	}
	
	@Override
	public String info() {
		return Game.getVar(R.string.Bow_Info1) + desc();
	}
	
	@Override
	protected int max() {
		return min();
	}
	
	public double acuFactor() {
		return 1;
	}

	public double dlyFactor() {
		return 1;
	}
	
	public double dmgFactor() {
		return 1;
	}

	public void onMiss() {}

	@Override
	public boolean goodForMelee() {
		return false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		if(hero.getBelongings().getItem(Arrow.class)!=null) {
			actions.add(AC_CHOOSE_ARROWS);
		}
		return super.actions(hero);
	}



	@Override
	public void execute(Hero hero, String action) {
		if(AC_CHOOSE_ARROWS.equals(action)) {
			GameScene.selectItem(item -> {
						if (item != null) {
							if(item instanceof Arrow){
								useArrowType((Arrow)item);
							}
						}
					},
					WndBag.Mode.ARROWS,
					Game.getVar(R.string.KindOfBow_SelectArrowKind) );
			return;
		}
		super.execute(hero, action);
	}
}
