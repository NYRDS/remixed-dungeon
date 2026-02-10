--
-- User: mike
-- Date: 28.05.2018
-- Time: 22:35
-- This file is part of Remixed Pixel Dungeon.
--

local serpent = require "scripts/lib/serpent"
local RPD = require "scripts/lib/commonClasses"
local util = require "scripts/lib/util"

local object = {}

object.__index = object

function object.saveData(self)
    return serpent.dump(self.data or {})
end

function object.loadData(self, _, str)
    local _,data = serpent.load(str)
    self.data = data or {}
end

function object.storeData(self, data)
    self.data = data or {}
end

function object.restoreData(self)
    return self.data or {}
end

function object.defaultDesc()
    return {
    }
end

function object.objectDesc(self, thisItem)
    local ret = object.defaultDesc(thisItem)
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

function object.desc(self, item)
    return item.defaultDesc()
end

object.init = function(desc)
    setmetatable(desc, object)
    return desc
end

return object