--
-- User: mike
-- Date: 26.05.2018
-- Time: 21:32
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local item = require "scripts/lib/item"

return item.init{
    desc  = function ()
        return {
            image     = 1,
            imageFile = "items/mastery_items.png",
            name      = "TenguLiver_Name",
            info      = "TenguLiver_Info",
            defaultAction = "Food_ACEat",
            price         = 0
        }
    end,

    actions = function() return {RPD.Actions.eat} end,

    execute = function(self, item, hero, action)
        if action == RPD.Actions.eat then
            local wnd = luajava.newInstance(RPD.Objects.Ui.WndChooseWay, hero, item, hero:getSubClassByName("GUARDIAN"), hero:getSubClassByName("WITCHDOCTOR") )
            RPD.GameScene:show(wnd)
        end
    end,

    onPickUp = function(self, item, chr)
        RPD.Badges:validateMastery(chr:getHeroClass())
    end
}
