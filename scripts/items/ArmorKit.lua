---
--- ArmorKit: turns a suit of armor into the wearer's class armor
--- (was items/ArmorKit.java).
---
local RPD     = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"
local itemLib = require "scripts/lib/item"

local AC_APPLY = "ArmorKit_ACAplly" -- sic: legacy action string
local TIME_TO_UPGRADE = 2

return itemLib.init{
    desc = function()
        return {
            image     = 86, -- ItemSpriteSheet.KIT
            imageFile = "items.png",
            name      = "ArmorKit_Name",
            info      = "ArmorKit_Info",
            price     = 0,
        }
    end,

    actions = function(self, item)
        return { AC_APPLY }
    end,

    execute = function(self, item, chr, action)
        if action == AC_APPLY then
            itemSelector.selectItem(function(armor, selector)
                if armor == nil then
                    return
                end

                local owner = item:getOwner()

                item:detach(owner:getBelongings().backpack)

                owner:getSprite():centerEmitter():start(
                    RPD.Sfx.Speck:factory(RPD.Sfx.Speck.KIT), 0.05, 10)

                RPD.glogw(RPD.format("ArmorKit_Upgraded", armor:name()))

                local classArmor = RPD.ClassArmor:upgrade(owner, armor)
                if owner:getItemFromSlot(RPD.Slot.ARMOR) == armor then
                    owner:getBelongings():setItemForSlot(classArmor, RPD.Slot.ARMOR)
                    owner:updateSprite()
                else
                    armor:detach(owner:getBelongings().backpack)
                    owner:collect(classArmor)
                end

                owner:doOperate(TIME_TO_UPGRADE)

                RPD.playSound("snd_evoke")
            end, RPD.BackpackMode.ARMOR, RPD.textById("ArmorKit_SelectArmor"), chr)
        end
    end,
}
