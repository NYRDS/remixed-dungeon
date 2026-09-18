---
--- Ooze: buffLevel damage per tick, washed off in water (was actors/buffs/Ooze.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

local CharSprite = luajava.bindClass("com.watabou.pixeldungeon.sprites.CharSprite")

return buff.init{
    desc  = function ()
        return {
            icon          = 8, -- BuffIndicator.OOZE
            name          = "OozeBuff_Name",
            info          = "OozeBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target

        if target:isAlive() then
            target:damage(buff:level(), buff)
            if not target:isAlive() and target == RPD.Dungeon.hero then
                RPD.Dungeon:fail(RPD.JavaUtils:format(
                    RPD.ResultDescriptions:getDescription(RPD.ResultReason.OOZE), { RPD.Dungeon.depth }))
                RPD.GLog:n(RPD.JavaUtils:format(RPD.textById("Ooze_Death"), { buff:name() }), {})
            end
            buff:spend(1) -- Actor.TICK
        end

        if RPD.Dungeon.level.water[target:getPos() + 1] then
            local lvl = buff:level()
            if lvl <= 0 then
                buff:detach()
            end
            buff:level(lvl - 1)
        end
    end
}
