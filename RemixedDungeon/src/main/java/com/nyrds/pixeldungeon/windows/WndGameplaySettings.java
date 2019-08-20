
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.RemixedDungeon;
import com.watabou.pixeldungeon.windows.Selector;
import com.watabou.pixeldungeon.windows.WndMenuCommon;

public class WndGameplaySettings extends WndMenuCommon {

    private Selector moveTimeoutSelector;

    @Override
    protected void createItems() {
        menuItems.add(new MenuCheckBox("Realtime!",RemixedDungeon.realtime()) {
            @Override
            protected void onClick() {
                super.onClick();
                RemixedDungeon.realtime(checked());
                moveTimeoutSelector.enable(!checked());
            }
        });

        menuItems.add(moveTimeoutSelector=createMoveTimeoutSelector());
	}

    private Selector createMoveTimeoutSelector() {

        return new Selector( WIDTH, BUTTON_HEIGHT, moveTimeoutText(), new Selector.PlusMinusDefault() {

            private int selectedTimeout = RemixedDungeon.limitTimeoutIndex(RemixedDungeon.moveTimeout());

            private void update(Selector s) {
                RemixedDungeon.moveTimeout(selectedTimeout);
                s.setText(moveTimeoutText());
            }

            @Override
            public void onPlus(Selector s) {
                selectedTimeout = RemixedDungeon.limitTimeoutIndex(selectedTimeout+1);
                update(s);
            }

            @Override
            public void onMinus(Selector s) {
                selectedTimeout = RemixedDungeon.limitTimeoutIndex(selectedTimeout-1);
                update(s);
            }

            @Override
            public void onDefault(Selector s) {
            }
        });
    }

    private String moveTimeoutText() {
        return String.format(Game.getVar(R.string.WndSettings_moveTimeout),Double.toString(RemixedDungeon.getMoveTimeout()/1000));
    }
}