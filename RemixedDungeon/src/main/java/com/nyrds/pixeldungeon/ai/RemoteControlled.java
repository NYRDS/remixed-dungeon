package com.nyrds.pixeldungeon.ai;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

/**
 * Remote-control state: the char is driven from outside (debug HTTP API).
 * No autonomy at all - executes the injected curAction if present,
 * idles otherwise. Reverts to the pre-remote AI after remoteRevertAfter
 * idle world turns (watchdog, 0 = never).
 * Spec: docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md
 */
public class RemoteControlled extends MobAi implements AiState {

	@Override
	public void act(@NotNull Char me) {
		if (!(me instanceof Mob)) {
			// caveman: state is mob-only, but stay safe and cheap
			me.spend(Char.TICK);
			return;
		}
		Mob mob = (Mob) me;

		CharAction action = me.getCurAction();
		if (action == null) {
			// idle: count quiet world turns, revert after the limit
			if (mob.remoteRevertAfter > 0 && ++mob.remoteIdleTurns >= mob.remoteRevertAfter) {
				GLog.i("%s reverts from remote control", mob.getEntityKind());
				mob.revertRemoteControl();
				return;
			}
			me.spend(Char.TICK);
			return;
		}

		// active command - it is activity, watchdog reset
		mob.remoteIdleTurns = 0;
		action.act(me);
		// Mob.act handles the no-time-spent case (retries / spends TICK)
	}

	@Override
	public String status(Char me) {
		return "remote controlled";
	}

	@Override
	public void gotDamage(Char me, NamedEntityKind src, int dmg) {
		// caveman: no revenge, no chase. the driver decides everything.
	}
}
