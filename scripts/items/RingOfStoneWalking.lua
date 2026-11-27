---
--- RingOfStoneWalking: always-known doom ring; being immured in stone kills
--- the hero (was rings/RingOfStoneWalking.java).
---
local RPD  = require "scripts/lib/commonClasses"
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff       = "StoneWalking",
            price      = 80,
            identified = true,
            upgradable = false,
            image      = 133,
        }
    end,
    icon = function(self, item, buff)
        return RPD.BuffIndicator.STONEBLOOD
    end,
    name = function(self, item)
        return RPD.textById("StoneBloodBuff_Name")
    end,
    info = function(self, item)
        return RPD.textById("StoneBloodBuff_Info")
    end,
    onHeroDeath = function(self, item, buff)
        RPD.Badges:validateDeathInStone()
        RPD.Dungeon:fail(RPD.ResultDescriptions:getDescription(RPD.ResultReason.IMMURED, RPD.Dungeon.depth))
        RPD.GLog:n(RPD.textById("RingOfStoneWalking_ImmuredInStone"), {})
    end,
}
