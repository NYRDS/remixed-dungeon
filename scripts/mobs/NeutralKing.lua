--
-- caveman: NeutralKing. spawns when chess ends in stalemate (tie).
-- stands neutral + passive -> hero may pass. first blood (hero or anything)
-- breaks the truce: king turns hostile and fights back. King.java boss untouched.
--

local RPD     = require "scripts/lib/commonClasses"
local mob     = require "scripts/lib/mob"

local Fraction = luajava.bindClass("com.watabou.pixeldungeon.actors.mobs.Fraction")

return mob.init({
    -- caveman: hurt while neutral -> turn hostile, hunt the attacker.
    damage = function(self, dmg, src)
        if self:fraction():name() == "NEUTRAL" then
            self:setFraction(Fraction.DUNGEON)
            RPD.setAi(self, "Hunting")
            RPD.glog("NeutralKing_Hostile")
        end
    end
})
