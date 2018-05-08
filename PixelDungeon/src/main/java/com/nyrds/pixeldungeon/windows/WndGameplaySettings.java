
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.windows.Selector;
import com.watabou.pixeldungeon.windows.WndSettingsCommon;

public class WndGameplaySettings extends WndSettingsCommon {

    private Selector moveTimeoutSelector;

    public WndGameplaySettings() {
		super();

		VBox menuItems = new VBox();

        menuItems.add(new MenuCheckBox("Realtime!",PixelDungeon.realtime()) {
            @Override
            protected void onClick() {
                super.onClick();
                PixelDungeon.realtime(checked());
                moveTimeoutSelector.enable(!checked());
            }
        });

        menuItems.add(moveTimeoutSelector=createMoveTimeoutSelector());

        menuItems.setRect(0,0,WIDTH,menuItems.childsHeight());
		add(menuItems);

		resize(WIDTH, (int) menuItems.childsHeight());
	}

    private Selector createMoveTimeoutSelector() {

        return new Selector( WIDTH, BTN_HEIGHT, moveTimeoutText(), new Selector.PlusMinusDefault() {

            private int selectedTimeout = PixelDungeon.limitTimeoutIndex(PixelDungeon.moveTimeout());

            private void update(Selector s) {
                PixelDungeon.moveTimeout(selectedTimeout);
                s.setText(moveTimeoutText());
            }

            @Override
            public void onPlus(Selector s) {
                selectedTimeout = PixelDungeon.limitTimeoutIndex(selectedTimeout+1);
                update(s);
            }

            @Override
            public void onMinus(Selector s) {
                selectedTimeout = PixelDungeon.limitTimeoutIndex(selectedTimeout-1);
                update(s);
            }

            @Override
            public void onDefault(Selector s) {
            }
        });
    }

    private String moveTimeoutText() {
        return String.format(Game.getVar(R.string.WndSettings_moveTimeout),Double.toString(PixelDungeon.getMoveTimeout()/1000));
    }
}