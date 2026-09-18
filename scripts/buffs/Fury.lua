---
--- Fury: doubled attack damage while below 40% hp (was actors/buffs/Fury.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 18, -- BuffIndicator.FURY
            name          = "FuryBuff_Name",
            info          = "FuryBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target
        if target:hp() > target:ht() * 0.4 then
            buff:detach()
        end
        buff:spend(10) -- Actor.TICK * 10
    end,

    attackProc = function(self, buff, defender, damage)
        return damage * 2
    end,

    attachVisual = function(self, buff)
        local target = buff.target
        RPD.GLog:w(RPD.JavaUtils:format(RPD.textById("Brute_Enraged"), { target:getName() }), {})
        target:showStatus(CharSprite.NEGATIVE, RPD.textById("Brute_StaEnraged"))
    end
}
