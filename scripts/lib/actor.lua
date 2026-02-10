--
-- User: mike
-- Date: 11.11.2017
-- Time: 20:54
-- This file is part of Remixed Pixel Dungeon.
--

local actor = {}

actor.__index = actor

actor.init = function(desc)
    local ret = {}

    for k,v in pairs(desc) do
       ret[k] = v
    end

    setmetatable(ret, actor)

    return ret
end

actor.act = function() return true end


actor.actionTime = function()
    return  1
end

actor.activate = function() end

return actor
