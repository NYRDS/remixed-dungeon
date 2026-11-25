---
--- Dewdrop: a drop of morning water (was items/Dewdrop.java). Fills the
--- DewVial first, else heals on pickup; never bags. Evaporates in fire.
---
local RPD  = require "scripts/lib/commonClasses"
local item = require "scripts/lib/item"

return item.init{
    desc = function()
        return {
            image     = 81, -- ItemSpriteSheet.DEWDROP
            imageFile = "items.png",
            name      = "Dewdrop_Name",
            info      = "Dewdrop_Info",
            stackable = true,
            price     = 0,
        }
    end,

    -- replaces Item.doPickUp entirely: collected or drunk, not bagged
    doPickUp = function(self, item, hero)
        local collected = false

        local vial = hero:getBelongings():getItem("DewVial")

        if vial and not vial:itemBooleanHook("isFull", true) then
            vial:itemHook("collectDew", item)
            collected = true
        end

        if not collected and hero:hp() < hero:ht() then
            -- java (depth-1)/5 is integer division: truncates toward zero,
            -- lua floor would give -1 on the town (depth 0)
            local q = (RPD.Dungeon.depth - 1) / 5
            if q < 0 then q = math.ceil(q) else q = math.floor(q) end
            local value = 1 + q

            value = value + RPD.CharUtils:dewBonusSum(hero)

            local effect = math.min(hero:ht() - hero:hp(), value * item:quantity())
            if effect > 0 then
                hero:heal(effect, item)
                hero:showStatus_a(RPD.CharSprite.POSITIVE, "%+dHP", effect)
                collected = true
            end
        end

        if collected then
            RPD.playSound("snd_dewdrop")
            hero:spendAndNext(1) -- TIME_TO_PICK_UP
        end

        return collected
    end,

    -- grass dew is picked up silently
    announcePickUp = function(self, item)
        return false
    end,

    burn = function(self, item, cell)
        return nil
    end,
}
