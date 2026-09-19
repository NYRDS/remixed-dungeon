---
--- ConcreteBlock, data-served; required STR rides the "data" field (default 10).
---

local RPD = require "scripts/lib/commonClasses"

local object = require "scripts/lib/object"


return object.init{

    init = function(self, object, level, data, json)
        local st = self:restoreData()
        st.str = tonumber(data) or 10
        self:storeData(st)
    end,

    nonPassable = function(self, object, ch)
        return ch:valid()
    end,

    affectLevelObjects = function(self, object)
        return true
    end,

    pushable = function(self, object, hero)
        return hero:effectiveSTR() >= self:restoreData().str
    end,

    info = function(self, object, level)
        return RPD.textById("ConcreteBlock_Description"):format(self:restoreData().str)
    end,

    name = function(self, object, level)
        return RPD.textById("ConcreteBlock_Name")
    end
}
