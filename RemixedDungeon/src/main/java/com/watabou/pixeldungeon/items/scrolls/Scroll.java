
package com.watabou.pixeldungeon.items.scrolls;

import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

import lombok.SneakyThrows;

public abstract class Scroll extends Item implements UnknownItem {

	protected static final float TIME_TO_READ	= 1f;
	
	private static final Class<?>[] scrolls = {
		ScrollOfIdentify.class, 
		ScrollOfMagicMapping.class, 
		ScrollOfRecharging.class, 
		ScrollOfRemoveCurse.class, 
		ScrollOfTeleportation.class, 
		ScrollOfUpgrade.class, 
		ScrollOfChallenge.class,
		ScrollOfTerror.class,
		ScrollOfLullaby.class,
		ScrollOfWeaponUpgrade.class,
		ScrollOfPsionicBlast.class,
		ScrollOfMirrorImage.class,
		ScrollOfDomination.class,
		ScrollOfSummoning.class,
		ScrollOfCurse.class
	};

	private static final Class<?>[] inscribableScrolls = {
		ScrollOfIdentify.class, 
		ScrollOfMagicMapping.class, 
		ScrollOfRecharging.class, 
		ScrollOfRemoveCurse.class, 
		ScrollOfTeleportation.class, 
		ScrollOfUpgrade.class, 
		ScrollOfChallenge.class,
		ScrollOfTerror.class,
		ScrollOfLullaby.class,
		ScrollOfPsionicBlast.class,
		ScrollOfMirrorImage.class,
		ScrollOfDomination.class,
		ScrollOfSummoning.class,
		ScrollOfCurse.class
	};

	private static final Integer[] images = {
		ItemSpriteSheet.SCROLL_KAUNAN, 
		ItemSpriteSheet.SCROLL_SOWILO, 
		ItemSpriteSheet.SCROLL_LAGUZ, 
		ItemSpriteSheet.SCROLL_YNGVI, 
		ItemSpriteSheet.SCROLL_GYFU, 
		ItemSpriteSheet.SCROLL_RAIDO, 
		ItemSpriteSheet.SCROLL_ISAZ, 
		ItemSpriteSheet.SCROLL_MANNAZ, 
		ItemSpriteSheet.SCROLL_NAUDIZ, 
		ItemSpriteSheet.SCROLL_BERKANAN, 
		ItemSpriteSheet.SCROLL_ODAL, 
		ItemSpriteSheet.SCROLL_TIWAZ,
		ItemSpriteSheet.SCROLL_ANSUZ,
		ItemSpriteSheet.SCROLL_IWAZ,
		ItemSpriteSheet.SCROLL_ALGIZ,
		ItemSpriteSheet.SCROLL_DAGAZ};
	
	private static ItemStatusHandler<Scroll> handler;

	private String rune;
	
	@SuppressWarnings("unchecked")
	public static void initLabels() {
		handler = new ItemStatusHandler<>((Class<? extends Scroll>[]) scrolls, images);
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>((Class<? extends Scroll>[]) scrolls, images, bundle);
	}
	
	public Scroll() {
		stackable     = true;
		setDefaultAction(CommonActions.AC_READ);
		
		if (this instanceof BlankScroll){
			return;
		}

		image = handler.index( this );
		rune  = StringsManager.getVars(R.array.Scroll_Runes)[ItemStatusHandler.indexByImage(image,images)];
	}

	@NotNull
	@SneakyThrows
	static public Scroll createRandomScroll(){
		return (Scroll) Random.element(inscribableScrolls).newInstance();
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( CommonActions.AC_READ );
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( CommonActions.AC_READ )) {
			if (chr.hasBuff( Blindness.class )) {
                GLog.w(StringsManager.getVar(R.string.Scroll_Blinded));
			} else {
				detach( chr.getBelongings().backpack );
				doRead(chr);
			}
			chr.readyAndIdle();
		} else {
			super._execute(chr, action );
		}
	}
	
	abstract protected void doRead(@NotNull Char reader);
	
	public boolean isKnown() {
		return handler.isKnown( this );
	}
	
	public void setKnown() {
		if (!isKnown()) {
			handler.know( this );
		}
		
		Badges.validateAllScrollsIdentified();
	}
	
	@Override
	public Item identify() {
		setKnown();
		return super.identify();
	}
	
	@Override
	public String name() {
        return isKnown() ? name : Utils.format(R.string.Scroll_Name, rune);
	}
	
	@Override
	public String info() {
        return isKnown() ? desc() : Utils.format(R.string.Scroll_Info, rune);
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	public static HashSet<Class<? extends Scroll>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Scroll>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == scrolls.length;
	}
	
	@Override
	public int price() {
		return 15 * quantity();
	}
	
	@Override
	public Item burn(int cell){
		return null;
	}

	@Override
	public String bag() {
		return ScrollHolder.class.getSimpleName();
	}
}
