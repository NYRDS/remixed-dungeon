
package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.support.AdsRewardVideo;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;

public class WndMovieTheatre extends WndQuest{

	private final AdsRewardVideo rewardVideo = new AdsRewardVideo();

	public WndMovieTheatre(final ServiceManNPC npc, int filmsSeen, int limit) {

		super(npc, Utils.format(StringsManager.getVar(R.string.WndMovieTheatre_Instruction), ServiceManNPC.getReward().quantity()) + "\n\n" + Utils.format(StringsManager.getVar(R.string.WndMovieTheatre_Instruction_2), filmsSeen, limit));

		float y = height + 2*GAP;

		RedButton btnYes = new RedButton(StringsManager.getVar(R.string.WndMovieTheatre_Watch)) {
			@Override
			protected void onClick() {
				hide();
				rewardVideo.show(ServiceManNPC.getReward());
			}
		};

		btnYes.setRect( 0, y + GAP, STD_WIDTH, BUTTON_HEIGHT);
		add( btnYes );

		RedButton btnNo = new RedButton(StringsManager.getVar(R.string.WndMovieTheatre_No)) {
			@Override
			protected void onClick() {
				npc.say(StringsManager.getVar(R.string.WndMovieTheatre_Bye));
				hide();
			}
		};
		btnNo.setRect( 0, btnYes.bottom(), STD_WIDTH, BUTTON_HEIGHT );
		add( btnNo );
		
		resize( STD_WIDTH, (int)btnNo.bottom() );
	}



}
