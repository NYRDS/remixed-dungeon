package com.nyrds.pixeldungeon.items.chaos;

import java.util.ArrayList;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.blobs.Freezing;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.blobs.Web;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

import android.annotation.SuppressLint;

public class ChaosCrystal extends Artifact implements IChaosItem{
	
	public static final float TIME_TO_USE = 1;
	public static final String AC_USE = Game.getVar(R.string.ChaosCrystal_Use);
	public static final String AC_FUSE = Game.getVar(R.string.ChaosCrystal_Fuse);

	private static final int CHAOS_CRYSTALL_IMAGE = 9;
	
	private int identetifyLevel = 0;
	private int charge          = 100;
	
	@SuppressWarnings("rawtypes")
	private static Class[] blobs = {
		ConfusionGas.class,
		Fire.class,
		ParalyticGas.class,
		Regrowth.class,
		ToxicGas.class,
		Web.class
	};
	
	public ChaosCrystal() {
		imageFile = "items/artifacts.png";
		image = CHAOS_CRYSTALL_IMAGE;
		unique = true;
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
				GameScene.add(Blob.seed(cell, charge, Random.element(blobs)));
			}
			getCurUser().spendAndNext(TIME_TO_USE);
		}

		@Override
		public String prompt() {
			return Game.getVar(R.string.ChaosCrystal_Prompt);
		}
	};
	
	@Override
	public void execute( final Hero ch, String action ) {
		setCurUser(ch);
		
		if (action.equals( AC_USE )) {
			GameScene.selectCell(chaosMark);
		} else if(action.equals( AC_FUSE)){
			ch.spendAndNext(TIME_TO_USE);
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
		return String.format("%d/100", charge);
	}
	
	@Override
	public int getColor() {
		return 0x7f7f7f;
	}

	@Override
	public void ownerTakesDamage(int damage) {
		charge++;
	};
}
