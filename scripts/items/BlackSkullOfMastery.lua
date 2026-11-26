---
--- BlackSkullOfMastery: the necromancer mastery tome - reading it offers
--- the LICH way (was necropolis/BlackSkullOfMastery.java). The completion
--- (detach, fx, subclass set, LICH penalty) is MasteryItem.choose, driven
--- by the java WndChooseWay; "remain necromancer" swaps it for a BlackSkull.
---
local RPD     = require "scripts/lib/commonClasses"
local itemLib = require "scripts/lib/item"

local AC_NECROMANCY = "Necromancer_ACSpecial"

return itemLib.init{
    desc = function()
        return {
            image      = 19,
            imageFile  = "items/artifacts.png",
            name       = "BlackSkullOfMastery_Name",
            info       = "BlackSkullOfMastery_Info",
            stackable  = false,
            upgradable = false,
            identified = true,
            price      = 0,
            equipable  = RPD.Slots.artifact,
        }
    end,

    onPickUp = function(self, item, chr)
        if chr:getHeroClass():name() == "NECROMANCER" then
            RPD.Badges:validateMastery(chr:getHeroClass())
        end
    end,

    actions = function(self, item, hero)
        return { AC_NECROMANCY }
    end,

    execute = function(self, item, chr, action)
        if action ~= AC_NECROMANCY then
            return
        end

        if chr:getHeroClass():name() == "NECROMANCER" then
            local wnd = luajava.newInstance(RPD.Objects.Ui.WndChooseWay,
                chr, item, chr:getSubClassByName("LICH"))
            RPD.GameScene:show(wnd)
        else
            RPD.glogw("Error: How did you get this item?! You're not supposed to be able to obtain it!!")
        end
    end,
}
