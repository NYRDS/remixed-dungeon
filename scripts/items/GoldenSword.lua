---
--- GoldenSword: data weapon kind (was weapon/melee/GoldenSword.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            acu       = 1.1,
            dly       = 0.8,
            image     = 5,
            enchatable = false,
            imageFile = "items/swords.png",
        }
    end,

    anim = function(self, item)
        return "sword"
    end,
    attackProc = function(self, item, attacker, defender, damage)
        -- gold proc: 1 in 10 hits spills the victim's purse (was
        -- GoldenSword.attackProc; the kind proc replaces the enchant path
        -- entirely, unenchantable anyway)
        if RPD.Random:Int(10) == 1 then
            local price = math.min(item:price() / 10, 500)
            local gold = RPD.item("Gold", price)
            gold:doDrop(defender)
        end
        item:usedForHit()
        return damage
    end,
    glowing = function(self, item)
        return itemLib.makeGlowing(0xFFFF66, 1) -- was Glowing(0xFFFF66) = period 1
    end,
}
