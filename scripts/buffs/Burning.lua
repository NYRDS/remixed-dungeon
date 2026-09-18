---
--- Burning: fire DoT, scorches carried items, seeds Fire on flammable
--- cells, doused by water; java entry points: CharUtils.ignite (attach +
--- fresh duration), Char attach-damage kind gate
--- (was actors/buffs/Burning.java)
---
local RPD  = require "scripts/lib/commonClasses"
local buff = require "scripts/lib/buff"

return buff.init{
    desc  = function ()
        return {
            icon          = 2, -- BuffIndicator.FIRE
            name          = "BurningBuff_Name",
            info          = "BurningBuff_Info",
        }
    end,

    act = function(self, buff)
        local target = buff.target
        local level = RPD.Dungeon.level

        if target:isAlive() then
            if target:getEntityKind() == "Hero" then
                RPD.Buffs.Buff:prolong(target, "Light", 1.01)
            end

            local bonusDamage = math.floor(RPD.Dungeon.depth / 2)
            target:damage(RPD.Random:Int(1 + bonusDamage, 5 + bonusDamage), buff)

            RPD.CharUtils:burnCarriedItems(buff)
        else
            buff:detach()
        end

        if level.flammable[target:getPos() + 1] then
            RPD.placeBlob(RPD.Blobs.Fire, target:getPos(), 4)
        end

        buff:spend(1) -- Actor.TICK
        self.data.left = (self.data.left or 0) - 1

        -- douse in water unless flying; else burn out or roll for a shake-off
        if level.water[target:getPos() + 1] and not target:isFlying() then
            buff:detach()
            return
        end

        if self.data.left <= 0
            or math.random() > (2 + target:hp() / target:ht()) / 3 then
            buff:detach()
        end
    end,

    -- java entry: CharUtils.ignite passes factor-scaled duration
    reignite = function(self, buff, left)
        self.data.left = left
    end,

    onHeroDeath = function(self, buff)
        RPD.Badges:validateDeathFromFire()
        RPD.Dungeon:fail(RPD.JavaUtils:format(
            RPD.ResultDescriptions:getDescription(RPD.ResultReason.BURNING), { RPD.Dungeon.depth }))
        RPD.glogn(RPD.textById("Burning_Death"))
    end,

    charSpriteStatus = function(self, buff)
        return "BURNING"
    end
}
