---
--- Torch: strap oil and light the dark (was items/Torch.java).
--- Burns away in fire instead of morphing, like the java class did.
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image         = 84, -- ItemSpriteSheet.TORCH
            imageFile     = "items.png",
            name          = "Torch_Name",
            info          = "Torch_Info2",
            stackable     = true,
            price         = 10,
            defaultAction = "Torch_ACLight",
        }
    end,

    actions = function(self, item)
        return { "Torch_ACLight" }
    end,

    execute = function(self, item, chr, action)
        if action == "Torch_ACLight" then
            chr:doOperate(1) -- TIME_TO_LIGHT
            item:detach(chr:getBelongings().backpack)
            RPD.affectBuff(chr, "Light", 250)
            chr:getSprite():centerEmitter():start(RPD.Sfx.FlameParticle.FACTORY, 0.2, 3)
        end
    end,

    burn = function(self, item, cell)
        return nil
    end,
}
