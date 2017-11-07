--
-- User: mike
-- Date: 07.11.2017
-- Time: 21:27
-- This file is part of Remixed Pixel Dungeon.
--

local trap = {}

trap.__index = trap

trap.init = function(trigger)
    local ret = {}
    ret.data = ""
    ret.__trigger = trigger

    setmetatable(ret, trap)

    return ret
end

trap.setData = function(self, data)
    print("trap.setData:", data)
    self.data = data
end

trap.trigger = function(self, cell, char)
    print("trap.trigger:",cell, char, self.data)
    self.__trigger(cell, char, self.data)
end

return trap
