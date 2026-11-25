
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.Weapon;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * Weapon balance choice for the Weightstone data kind (was Weightstone.WndBalance
 * inner class). The apply half stays java: it writes Weapon.Imbue, which lua
 * cannot assign. stone is the consuming weightstone, detached here.
 */
public class WndBalance extends Window {

	private static final int WIDTH         = 120;
	private static final int BUTTON_WIDTH  = WIDTH - GAP * 2;

	private static final float TIME_TO_APPLY = 2;

	private final Item stone;

	public WndBalance( @NotNull Weapon weapon, @NotNull Item stone ) {
		super();
		this.stone = stone;

		IconTitle titlebar = new IconTitle( weapon );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		Text tfMesage = PixelScene.createMultiline( Utils.format(R.string.Weightstone_WndChoice, weapon.name() ), GuiProperties.regularFontSize());
		tfMesage.maxWidth(WIDTH - GAP * 2);
		tfMesage.setX(GAP);
		tfMesage.setY(titlebar.bottom() + GAP);
		add( tfMesage );

		float pos = tfMesage.getY() + tfMesage.height();

		if (weapon.imbue != Weapon.Imbue.SPEED) {
			RedButton btnSpeed = new RedButton(R.string.Weightstone_WndSpeed) {
				@Override
				protected void onClick() {
					hide();
					apply( weapon, true );
				}
			};
			btnSpeed.setRect(GAP, pos + GAP, BUTTON_WIDTH, BUTTON_HEIGHT );
			add( btnSpeed );

			pos = btnSpeed.bottom();
		}

		if (weapon.imbue != Weapon.Imbue.ACCURACY) {
			RedButton btnAccuracy = new RedButton(R.string.Weightstone_WndAccuracy) {
				@Override
				protected void onClick() {
					hide();
					apply( weapon, false );
				}
			};
			btnAccuracy.setRect(GAP, pos + GAP, BUTTON_WIDTH, BUTTON_HEIGHT );
			add( btnAccuracy );

			pos = btnAccuracy.bottom();
		}

		RedButton btnCancel = new RedButton(R.string.Weightstone_WndCancel) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		btnCancel.setRect(GAP, pos + GAP, BUTTON_WIDTH, BUTTON_HEIGHT );
		add( btnCancel );

		resize( WIDTH, (int)btnCancel.bottom() + GAP);
	}

	private void apply( Weapon weapon, boolean forSpeed ) {
		Char owner = stone.getOwner();

		stone.detach( owner.getBelongings().backpack );

		if (forSpeed) {
			weapon.imbue = Weapon.Imbue.SPEED;
			GLog.p(StringsManager.getVar(R.string.Weightstone_Fast), weapon.name() );
		} else {
			weapon.imbue = Weapon.Imbue.ACCURACY;
			GLog.p(StringsManager.getVar(R.string.Weightstone_Accurate), weapon.name() );
		}

		owner.doOperate(TIME_TO_APPLY);
		Sample.INSTANCE.play( Assets.SND_MISS );
	}

	protected void onSelect( int index ) {}
}
