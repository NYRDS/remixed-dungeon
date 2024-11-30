package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;

import org.jetbrains.annotations.NotNull;

public abstract class ImmortalNPC extends NPC {


	public ImmortalNPC() {
		movable = false;
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
	
	@Override
	public String defenseVerb() {
        return StringsManager.getVar(R.string.Ghost_Defense);
    }
	
	@Override
	public float speed() {
		return 0.5f;
	}
	

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
	}
	
	@Override
	public boolean add(Buff buff ) {
        return false;
    }

	@Override
	public boolean reset() {
		return true;
	}
}
