---
--- Weightstone: balances a weapon for speed or accuracy
--- (was items/Weightstone.java). The balance window and the imbue write
--- stay java (windows.WndBalance) - Weapon.Imbue is not assignable from lua.
---
local RPD     = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local itemLib = require "scripts/lib/item"

local AC_APPLY = "Weightstone_ACApply"

return itemLib.init{
    desc = function()
        return {
            image     = 123, -- ItemSpriteSheet.WEIGHT
            imageFile = "items.png",
            name      = "Weightstone_Name",
            info      = "Weightstone_Info",
            stackable = true,
            price     = 40,
        }
    end,

    actions = function(self, item)
        return { AC_APPLY }
    end,

    execute = function(self, item, chr, action)
        if action == AC_APPLY then
            itemSelector.selectItem(function(weapon, selector)
                if weapon == nil then
                    return
                end
                local wnd = luajava.newInstance(
                    RPD.Objects.Ui.WndBalance, weapon, item)
                RPD.GameScene:show(wnd)
            end, RPD.BackpackMode.WEAPON, RPD.textById("Weightstone_Select"), chr)
        end
    end,
}
