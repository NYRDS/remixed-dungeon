local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

-- java Piranha: breeds in flooded vaults, suffocates the moment it leaves
-- water, always carries a raw fish, every death (land suffocation included)
-- counts toward the piranhas badge. Stats scale with the pool depth (json
-- holds the depth-1 defaults); never returns to its spawn cell (java
-- reset()==true - not expressible, accepted delta).
return mob.init{
    stats = function(self)
        local d = RPD.Dungeon.depth
        local ht = 10 + d * 5
        self:ht(ht)
        self:hp(ht)
        self:STR(13)
        self:setBaseDefenseSkill(10 + d * 2)
        self:setBaseAttackSkill(20 + d * 2)
        self:setDr(d)
        self:setDmgMin(d)
        self:setDmgMax(4 + d * 2)
        -- ctor-only carry: one RawFish, like every java piranha
        local data = mob.restoreData(self)
        if data.carried then
            return
        end
        if not self:getBelongings():isBackpackEmpty() then
            -- pre-migration saves already carry their fish
            data.carried = true
            return
        end
        data.carried = true
        self:collect(RPD.item("RawFish"))
    end,

    act = function(self)
        if not RPD.Dungeon.level.water[self:getPos() + 1] then
            self:die()
        end
    end,

    die = function(self, cause)
        RPD.Statistics.piranhasKilled = RPD.Statistics.piranhasKilled + 1
        RPD.Badges:validatePiranhasKilled()
        return false
    end,
}
