---
--- Kusarigama: data weapon kind (was weapon/melee/Kusarigama.java / guts / common).
---
local RPD      = require "scripts/lib/commonClasses"
local itemLib  = require "scripts/lib/item"
local weapon   = require "scripts/lib/weapon"

return weapon.init{
    desc = function()
        return {
            tier      = 3,
            acu       = 2.0,
            dly       = 1.0,
            image     = 0,
            range     = 2,
            puller    = true,
            chainAttack = true,
            imageFile = "items/kusarigama.png",
        }
    end,

    anim = function(self, item)
        return "sword"
    end,
    postAttack = function(self, item, tgt)
        -- 10% chance to dizzy the victim (was Kusarigama.postAttack)
        if RPD.Random:Float(1) < 0.1 then
            RPD.affectBuff(tgt, "Vertigo", 3)
        end
    end,
}
