---
--- Shared wrapper for the well water kinds (mode "well"): the affect()
--- plumbing - hero first, then the heap swap shape - is common, the kinds
--- override affectHero/affectItem. The default hooks are copied into the
--- kind table (LuaScript.hasMethod reads raw keys).
--- self = the per-kind script table, water = the ScriptBlob instance.
---
--- desc(self) -> { mode = "well", tileDesc }
--- affectHero(self, water, hero) -> true when the water was consumed
--- affectItem(self, water, item) -> the transmuted item, or nil (the heap
---                                  is tossed away, the water stays)
---

local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

local well = {}
well.__index = well
setmetatable(well, {__index = blob})

well.affect = function(self, water)
    local pos = water:pos()
    local hero = RPD.Dungeon.hero

    if pos == hero:getPos() and self:affectHero(water, hero) then
        return true
    end

    local heap = RPD.Dungeon.level:getHeap(pos)
    if heap ~= nil then
        local oldItem = heap:peek()
        local newItem = self:affectItem(water, oldItem)
        if newItem ~= nil then
            if newItem ~= oldItem then
                if oldItem:quantity() > 1 then
                    oldItem:quantity(oldItem:quantity() - 1)
                    heap:drop(newItem)
                else
                    heap:replace(oldItem, newItem)
                end
            end
            if heap.sprite then
                heap.sprite:link(heap)
            end
            return true
        else
            RPD.ItemUtils:throwItemAway(pos)
            return false
        end
    end

    return false
end

well.affectHero = function(self, water, hero)
    return false
end

well.affectItem = function(self, water, item)
    return nil
end

-- the swirl marking an identified item / a read mind (identifies nothing
-- itself); no-op without a scene (headless)
well.identifyAt = function(self, water)
    if water.emitter then
        local effect = luajava.newInstance("com.watabou.pixeldungeon.effects.Identification",
            RPD.DungeonTilemap:tileCenterToWorld(water:pos()))
        water.emitter:getParent():add(effect)
    end
end

well.init = function(desc)
    for key, value in pairs(well) do
        if desc[key] == nil and key ~= "init" then
            desc[key] = value
        end
    end
    setmetatable(desc, well)
    return desc
end

return well
