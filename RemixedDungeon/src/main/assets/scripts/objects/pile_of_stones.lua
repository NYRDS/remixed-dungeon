---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 9/25/21 7:41 PM
---

local RPD = require "scripts/lib/commonClasses"

local object = require "scripts/lib/object"


return object.init{

    nonPassable = function(self, object)
        return true
    end,

    losBlocker = function(self, object)
        return true
    end,

    ignoreIsometricShift = function(self, object)
        return true
    end
}