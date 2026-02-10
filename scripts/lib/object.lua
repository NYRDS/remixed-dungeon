---
--- This file is part of Remixed Pixel Dungeon.
--- Created by mike.
--- DateTime: 9/25/21 7:35 PM
---


local serpent = require "scripts/lib/serpent"
local RPD = require "scripts/lib/commonClasses"
local util = require "scripts/lib/util"

local object = {}

object.__index = object

function object.actions(self, object, hero)
    return {}
end

function object.burn(self, object, cell)
    return object
end

function object.freeze(self, object, cell)
    return object
end

function object.interact(self, object, char)
    return object
end

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
        image         = 0,
        imageFile     = "objects/levelObjects.png",
        name          = "smth",
        info          = "smth",
    }
end

function object.objectDesc(self,thisobject)
    local ret = object.defaultDesc(thisobject)
    local own = self:desc(thisobject)

    if own.isArtifact then
        own.equipable = "artifact"
    end

    self.data = own.data or {}

    for k,v in pairs(own) do
        ret[k] = v
    end

    return ret
end

function object.desc(self, object)
    return object.defaultDesc()
end

object.init = function(desc)
    setmetatable(desc, object)
    return desc
end

return object