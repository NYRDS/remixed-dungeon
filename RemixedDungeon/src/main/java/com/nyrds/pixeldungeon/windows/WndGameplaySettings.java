
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.windows.Selector;
import com.watabou.pixeldungeon.windows.WndMenuCommon;

public class WndGameplaySettings extends WndMenuCommon {

    private Selector moveTimeoutSelector;

    @Override
    protected void createItems() {
        menuItems.add(new MenuCheckBox("Realtime!", GamePreferences.realtime()) {
            @Override
            protected void onClick() {
                super.onClick();
                GamePreferences.realtime(checked());
                moveTimeoutSelector.enable(!checked());
            }
        });

        menuItems.add(moveTimeoutSelector=createMoveTimeoutSelector());
        moveTimeoutSelector.enable(!GamePreferences.realtime());
	}

    private Selector createMoveTimeoutSelector() {

        return new Selector( WIDTH, BUTTON_HEIGHT, moveTimeoutText(), new Selector.PlusMinusDefault() {

            private int selectedTimeout = GamePreferences.limitTimeoutIndex(GamePreferences.moveTimeout());

            private void update(Selector s) {
                GamePreferences.moveTimeout(selectedTimeout);
                s.setText(moveTimeoutText());
            }

            @Override
            public void onPlus(Selector s) {
                selectedTimeout = GamePreferences.limitTimeoutIndex(selectedTimeout+1);
                update(s);
            }

            @Override
            public void onMinus(Selector s) {
                selectedTimeout = GamePreferences.limitTimeoutIndex(selectedTimeout-1);
                update(s);
            }

            @Override
            public void onDefault(Selector s) {
            }
        });
    }

    private String moveTimeoutText() {
        return String.format(StringsManager.getVar(R.string.WndSettings_moveTimeout), GamePreferences.getMoveTimeout() / 1000);
    }
}