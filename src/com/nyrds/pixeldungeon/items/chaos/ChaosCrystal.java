package com.nyrds.pixeldungeon.items.chaos;

import java.util.ArrayList;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import android.annotation.SuppressLint;

public class ChaosCrystal extends Artifact implements IChaosItem{
	
	private static final String IDENTETIFY_LEVEL_KEY = "identetifyLevel";
	private static final String CHARGE_KEY = "charge";
	
	public static final float TIME_TO_USE = 1;
	
	public static final String AC_USE = Game.getVar(R.string.ChaosCrystal_Use);
	public static final String AC_FUSE = Game.getVar(R.string.ChaosCrystal_Fuse);
	private static final String TXT_SELECT_FOR_FUSE = Game.getVar(R.string.ChaosCrystal_SelectForFuse);
	
	private static final int CHAOS_CRYSTALL_IMAGE = 9;
	private static final float TIME_TO_FUSE = 10;
	
	
	private int identetifyLevel = 0;
	private int charge          = 0;
	
	@SuppressWarnings("rawtypes")
	private static Class[] blobs = {
		ConfusionGas.class,
		Fire.class,
		ParalyticGas.class,
		Regrowth.class,
		ToxicGas.class,
	};
	
	public ChaosCrystal() {
		imageFile = "items/artifacts.png";
		image = CHAOS_CRYSTALL_IMAGE;
	}

	@Override
	public boolean isIdentified() {
		return identetifyLevel == 2;
	}

	@Override
	public Glowing glowing() {
		return new Glowing( (int) (Math.random() * 0xffffff) );
	}
	
	protected CellSelector.Listener chaosMark = new CellSelector.Listener() {

		@SuppressWarnings("unchecked")
		@Override
		public void onSelect(Integer cell) {
			if (cell != null) {
				charge = 0;
				CellEmitter.center( cell ).burst( PurpleParticle.BURST, Random.IntRange( 10, 20 ) );
				Sample.INSTANCE.play( Assets.SND_CRYSTAL );
				GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
				GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
			}
			getCurUser().spendAndNext(TIME_TO_USE);
		}

		@Override
		public String prompt() {
			return Game.getVar(R.string.ChaosCrystal_Prompt);
		}
	};
	
	private final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				if(item instanceof Scroll) {
					fuseScroll ((Scroll) item );
				}
			}
		}
	};
	
	private void fuse(Hero hero) {
		
		GameScene.selectItem( itemSelector, WndBag.Mode.FUSEABLE, TXT_SELECT_FOR_FUSE );
		hero.getSprite().operate( hero.pos );
	}
	
	protected void fuseScroll(Scroll scroll) {
		
		scroll.detach(getCurUser().belongings.backpack);
		detach(getCurUser().belongings.backpack );
		getCurUser().getSprite().operate( getCurUser().pos );
		getCurUser().spend( TIME_TO_FUSE);
		getCurUser().busy();
		
		getCurUser().collect(new ScrollOfWeaponUpgrade());
	}

	@Override
	public void execute( final Hero ch, String action ) {
		setCurUser(ch);
		
		if (action.equals( AC_USE )) {
			GameScene.selectCell(chaosMark);
		} else if(action.equals( AC_FUSE)){
			fuse(ch);
		} else {
			
			super.execute( ch, action );
		}
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if(charge > 0 && identetifyLevel > 0)	{
			actions.add( AC_USE  );
		}
		
		if(charge >= 50 && identetifyLevel > 1) {
			actions.add( AC_FUSE );
		}
		return actions;
	}
	
	@Override
	public Item identify() {
		identetifyLevel++;
		return this;
	};
	
	@Override
	public String name() {
		switch(identetifyLevel) {
			default:
				return super.name();
			case 1:
				return Game.getVar(R.string.ChaosCrystal_Name_1);
			case 2:
				return Game.getVar(R.string.ChaosCrystal_Name_2);
		}
	}
	
	@Override
	public String info() {
		switch(identetifyLevel) {
			default:
				return super.info();
			case 1:
				return Game.getVar(R.string.ChaosCrystal_Info_1);
			case 2:
				return Game.getVar(R.string.ChaosCrystal_Info_2);
		}
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	public String getText() {
		if(identetifyLevel > 0) {
			return String.format("%d/100", charge);
		} else {
			return null;
		}
	}
	
	@Override
	public int getColor() {
		return 0xe0a0a0;
	}

	@Override
	public void ownerTakesDamage(int damage) {
		if(damage > 0) {
			charge++;
			if(charge > 100) {
				charge = 100;
			}
		}
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		
		bundle.put(CHARGE_KEY, charge);
		bundle.put(IDENTETIFY_LEVEL_KEY, identetifyLevel);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		
		charge = bundle.getInt(CHARGE_KEY);
		identetifyLevel = bundle.getInt(IDENTETIFY_LEVEL_KEY); 
		
	}
}
