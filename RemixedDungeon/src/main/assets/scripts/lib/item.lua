--
-- User: mike
-- Date: 28.05.2018
-- Time: 22:35
-- This file is part of Remixed Pixel Dungeon.
--

local serpent = require "scripts/lib/serpent"
local RPD = require "scripts/lib/commonClasses"
local util = require "scripts/lib/util"

local item = {}

item.__index = item

function item.actions(self, item, hero)
    return {}
end

function item.execute(self, item, hero, action)
end

function item.burn(self, item, cell)
    return item
end

function item.freeze(self, item, cell)
    return item
end

function item.poison(self, item, cell)
    return item
end

function item.act(self, item)
    item:deactivateActor()
end

function item.onThrow(self, item, cell, thrower)
    item:dropTo(cell)
end

function item.cellSelected(self, item, action, cell)
end

function item.activate(self, item, hero)
end

function item.deactivate(self, item, hero)
end

function item.saveData(self)
    return serpent.dump(self.data or {})
end

function item.loadData(self, _, str)
    local _,data = serpent.load(str)
    self.data = data or {}
end

function item.storeData(self, data)
    self.data = data or {}
end

function item.restoreData(self)
    return self.data or {}
end

function item.defaultDesc()
    return {
        image         = 14,
        imageFile     = "items/food.png",
        name          = "smth",
        info          = "smth",
        stackable     = false,
        upgradable    = false,
        identified    = true,
        defaultAction = "Item_ACThrow",
        price         = 0,
        equipable     = ""
    }
end

function item.itemDesc(self,thisItem)
    local ret = item.defaultDesc(thisItem)
    local own = self:desc(thisItem)

    if own.isArtifact then
        own.equipable = "artifact"
    end

    self.data = own.data or {}

    for k,v in pairs(own) do
        ret[k] = v
    end

    return ret
end

function item.desc(self, item)
    return item.defaultDesc()
end

item.init = function(desc)
    setmetatable(desc, item)
    return desc
end

function item.makeGlowing(color, period)
    return luajava.newInstance('com.watabou.pixeldungeon.sprites.Glowing', color, period)
end

return item