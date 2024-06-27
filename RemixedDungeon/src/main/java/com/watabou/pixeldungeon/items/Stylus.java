
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Stylus extends Item {
	
	private static final float TIME_TO_INSCRIBE = 2;
	
	private static final String AC_INSCRIBE = "Stylus_ACInscribe";
	
	{
		image = ItemSpriteSheet.STYLUS;
		
		stackable = true;
	}
	
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_INSCRIBE );
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals(AC_INSCRIBE)) {

            GameScene.selectItem(chr,
                    (item, selector) -> {
                        if (item != null) {
                            if(item instanceof Armor){
                                inscribeArmor ( (Armor)item );
                            }
                            if(item instanceof BlankScroll){
                                inscribeScroll( (BlankScroll)item );
                            }
                        }
                    },
                    WndBag.Mode.INSCRIBABLE, StringsManager.getVar(R.string.Stylus_SelectArmor));
			
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
	
	private void inscribeEffect(){
		Char owner = getOwner();
		
		detach( owner.getBelongings().backpack );

		owner.doOperate(TIME_TO_INSCRIBE );
		owner.getSprite().centerEmitter().start( PurpleParticle.BURST, 0.05f, 10 );
		Sample.INSTANCE.play( Assets.SND_BURNING );
	}
	
	private void inscribeArmor ( Armor armor ) {

		Class<? extends Armor.Glyph> oldGlyphClass = armor.glyph != null ? armor.glyph.getClass() : null;
		Armor.Glyph glyph = Armor.Glyph.random();
		while (glyph.getClass() == oldGlyphClass) {
			glyph = Armor.Glyph.random();
		}

        GLog.w(StringsManager.getVar(R.string.Stylus_Inscribed), glyph.name(), armor.name() );
		
		armor.inscribe( glyph );
		
		inscribeEffect();
	}
	
	private void inscribeScroll (@NotNull BlankScroll scroll){
		
		scroll.detach( getOwner().getBelongings().backpack );
		
		Scroll inscribedScroll = Scroll.createRandomScroll();
		getOwner().collect(inscribedScroll);
		getOwner().itemPickedUp(inscribedScroll);

		inscribeEffect();
	}
	
	@Override
	public int price() {
		return 50 * quantity();
	}

}
