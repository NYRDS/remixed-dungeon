local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java SpiderMind (batch 10): harmless buff-bot - its zap buffs the first
-- friendly mob in sight (3 turns) instead of hurting the enemy, and it
-- retreats while hunting (aiState Kite + kiteNeverApproach) keeping its
-- ranged line.
return mob.init{
    stats = function(self)
        mob.restoreData(self).kiteNeverApproach = true
    end,

    zapProc = function(self, enemy, dmg)
        local level = RPD.Dungeon.level
        local buffs = {"Speed", "Barkskin", "Blessed", "Health", "Armor", "ManaShield", "Fury"}
        local mobs = level:getCopyOfMobsArray()

        for i = 1, #mobs do
            local chr = mobs[i]
            if chr ~= self then
                local chrPos = chr:getPos()
                -- luaj arrays are 1-based: fieldOfView[pos + 1] == java [pos]
                if level.fieldOfView[chrPos + 1] and chr:friendly(self) then
                    RPD.Buffs.Buff:prolong(chr, buffs[math.random(#buffs)], 3)
                    if RPD.Dungeon:isCellVisible(chrPos) then
                        RPD.Sfx.CellEmitter:get(chrPos):start(RPD.Sfx.ShaftParticle.FACTORY, 0.2, 3)
                    end
                    break
                end
            end
        end
        return 0
    end,

}
