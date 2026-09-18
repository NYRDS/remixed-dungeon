---
--- Bleeding: decaying buffLevel damage per tick (was actors/buffs/Bleeding.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 26, -- BuffIndicator.BLEEDING
            name          = "BleedingBuff_Name",
            info          = "BleedingBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target
        if not target:isAlive() then
            buff:detach()
            return
        end

        local lvl = buff:level()
        lvl = RPD.Random:Int(math.floor(lvl / 2), lvl)
        buff:level(lvl)

        if lvl <= 0 then
            buff:detach()
            return
        end

        target:damage(lvl, buff)

        local sprite = target:getSprite()
        if sprite:getVisible() then
            RPD.Sfx.Splash:at(sprite:center(), -math.pi / 2, math.pi / 6, sprite:blood(),
                math.floor(math.min(10 * lvl / target:ht(), 10)))
        end

        if target == RPD.Dungeon.hero and not target:isAlive() then
            RPD.Dungeon:fail(RPD.JavaUtils:format(
                RPD.ResultDescriptions:getDescription(RPD.ResultReason.BLEEDING), { RPD.Dungeon.depth }))
            RPD.glogn(RPD.textById("Bleeding_Death"))
        end

        buff:spend(1) -- Actor.TICK
    end,

    attachVisual = function(self, buff)
        buff.target:showStatus(CharSprite.NEGATIVE, RPD.textById("Char_StaBleeding"))
    end
}
