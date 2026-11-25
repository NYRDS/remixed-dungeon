---
--- RingOfThorns: reflects a random share of melee damage back at the attacker;
--- never upgradable, spawns at level +1, identifies itself on pickup
--- (was rings/RingOfThorns.java).
---
local RPD  = require "scripts/lib/commonClasses"
local ring = require "scripts/lib/ring"

return ring.init{
    desc = function()
        return {
            buff       = "Thorns",
            price      = 80,
            upgradable = false,
        }
    end,
    random = function(self, item)
        -- was level(+1)
        item:level(1)
        return true
    end,
    doPickUp = function(self, item, chr)
        item:identify()
        RPD.Badges:validateRingOfThorns()
        RPD.Badges:validateItemLevelAcquired(item)
        return nil -- fall through to the default collect, as the java did
    end,
    defenceProc = function(self, item, buff, defender, enemy, damage)
        local dmg = RPD.Random:IntRange(0, damage)
        if dmg > 0 then
            enemy:damage(dmg, buff)
        end
        return damage
    end,
}
