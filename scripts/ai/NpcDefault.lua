local RPD = require "scripts/lib/commonClasses"
local ai = require "scripts/lib/ai"

return ai.init({
    act = function(self, ai, me)
        local pos = me:getPos()

        RPD.ItemUtils:throwItemAway(pos)
        me:getSprite():turnTo( pos, RPD.Dungeon.hero:getPos() );
        me:spend(1)
    end,

    gotDamage = function(self, me, src, dmg)
    end,

    status = function(self, ai, me)
        return RPD.format("Mob_StaPassiveStatus", me:getName())
    end
})