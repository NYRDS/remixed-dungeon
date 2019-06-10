package com.nyrds.pixeldungeon.mobs.npc;

import com.watabou.pixeldungeon.actors.Char;

public class InquirerNPC extends ImmortalNPC {

    public InquirerNPC() {
//        if (PollfishSurveys.consented()) {
//            PollfishSurveys.init();
//        }
    }


    public static void reward() {
    }

    @Override
    public boolean interact(final Char hero) {
/*
        if (!PollfishSurveys.consented()) {
        }

        getSprite().turnTo(getPos(), hero.getPos());

        if (!Util.isConnectedToInternet()) {
            GameScene.show(new WndQuest(this, Game.getVar(R.string.ServiceManNPC_NoConnection)));
            return true;
        }

        GameScene.show(new WndOptions("Inquirer_title",
                "Inquirer_text",
                "Inquirer_yes",
                "Inquirer_show_privacy",
                "Inquirer_no"){
            @Override
            protected void onSelect(int index) {
                switch (index) {
                    case 0:
                        PollfishSurveys.showSurvey();
                        break;
                    case 1:
                        GameScene.show(new WndStory("Inquirer_privacyPolicy"));
                        break;
                    case 2:
                        say("Inquirer_bye");
                        break;
                }
            }
        });
*/
        return true;

    }
}
