local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java YogsHeart: being hurt draws every mob on the level to it; its defence
-- spawns a Larva next to it; every turn it heals a random mob (pets excepted)
-- with the potion-of-healing cleanse.
local function beckonAll(self)
    local mobs = RPD.Dungeon.level:getMobs()
    for i = 1, #mobs do
        mobs[i]:beckon(self:getPos())
    end
end

return mob.init{
    damage = function(self, dmg, src)
        beckonAll(self)
    end,
    defenceProc = function(self, enemy, dmg)
        RPD.CharUtils:spawnOnNextCell(self, "Larva",
                math.floor(10 * RPD.GameLoop:getDifficultyFactor()))
        return dmg
    end,
    act = function(self)
        local mobs = RPD.Dungeon.level:getMobs()
        if #mobs == 0 then
            return
        end
        local other = mobs[math.random(#mobs)]
        if other ~= nil and other:isAlive() and not other:isPet() then
            -- java called PotionOfHealing.heal(other, 0.2) - same flow inlined
            -- (that potion class can't be bound from lua: its clinit builds a
            -- potion instance and boots NPE); heal src is the heart, not a
            -- pseudo-potion
            other:heal(math.floor(other:ht() * 0.2), self)
            other:detachBuff("Poison")
            other:detachBuff("Cripple")
            other:detachBuff("Weakness")
            other:detachBuff("Bleeding")
        end
    end
}
