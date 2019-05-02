---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 25.03.19 0:06
---

local buff = require "scripts/lib/buff"


return buff.init{
    desc  = function ()
        return {
            icon          = 45,
            name          = "DefenceMastery_Name",
            info          = "DefenceMastery_Info",
        }
    end,
    act = function(self,buff)
        buff:detach()
    end,
    drBonus = function(self,buff)
        return 10
    end
}