---
--- Poison: DoT ticks scale with remaining duration (was actors/buffs/Poison.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 3, -- BuffIndicator.POISON
            name          = "PoisonBuff_Name",
            info          = "PoisonBuff_Info",
        }
    end,

    charAct = function(self, buff)
        local timeLeft = buff:cooldown()
        buff.target:damage(math.floor(timeLeft / 3) + 1, buff)
    end,

    act = function(self, buff)
        buff:detach()
    end,

    onHeroDeath = function(self, buff)
        RPD.Badges:validateDeathFromPoison()
        RPD.Dungeon:fail(RPD.JavaUtils:format(
            RPD.ResultDescriptions:getDescription(RPD.ResultReason.POISON), { RPD.Dungeon.depth }))
        RPD.glogn(RPD.textById("Poison_Death"))
    end,

    attachVisual = function(self, buff)
        RPD.Sfx.CellEmitter:center(buff.target:getPos()):burst(RPD.Sfx.PoisonParticle.SPLASH, 5)
        local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")
        buff.target:showStatus(CharSprite.NEGATIVE, RPD.textById("Char_StaPoisoned"))
    end
}
