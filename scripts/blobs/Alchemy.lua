---
--- The alchemy pot: bubbles while there is anything to brew; affectCell is
--- the transmute flow - 3+ seeds brew a random potion (1/count chance) or
--- the alchemy brew of one of the seeds (weighted by quantity). Was
--- Alchemy.java. The pot object (scripts/objects/pot.lua) calls affectCell
--- directly - the flow needs no blob state.
---
local RPD  = require "scripts/lib/commonClasses"
local blob = require "scripts/lib/blobbase"

return blob.init{
    desc = function()
        return {
            mode = "persist",
        }
    end,

    after = function(self, pot, cells)
        for i = 1, cells.length do
            if RPD.Dungeon:isCellVisible(cells[i]) then
                RPD.Journal:add(RPD.Journal.Feature.ALCHEMY:desc())
                return
            end
        end
    end,

    affectCell = function(self, cell)
        local level = RPD.Dungeon.level
        local heap = level:getHeap(cell)
        if heap == nil then
            return
        end

        RPD.Sfx.CellEmitter:get(heap.pos):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.BUBBLE), 3)
        RPD.Sfx.Splash:at(heap.pos, 0xFFFFFF, 3)

        local chances = RPD.ItemUtils:heapSeedChances(heap)
        if chances == nil then
            return
        end

        local count = 0
        for i = 1, chances.length do
            count = count + chances[i]
        end

        -- SEEDS_TO_POTION
        if count < 3 then
            return
        end

        RPD.Sfx.CellEmitter:get(heap.pos):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.WOOL), 6)
        RPD.playSound("snd_puff")

        local result
        local treasury = RPD.Treasury:getLevelTreasury()

        if RPD.Random:Int(count) == 0 then
            RPD.Sfx.CellEmitter:center(heap.pos):burst(RPD.Sfx.Speck:factory(RPD.Sfx.Speck.EVOKE), 3)
            heap:destroy()
            result = treasury:random(RPD.Treasury.Category.POTION)
        else
            -- the weighted seed pick (Random.chances walk)
            local roll = RPD.Random:Float(count)
            local index = chances.length
            local acc = 0
            for i = 1, chances.length do
                acc = acc + chances[i]
                if roll <= acc then
                    index = i
                    break
                end
            end
            -- java lists index 0-based (unlike the lua-side arrays)
            local itemKind = heap.items:get(index - 1).alchemyKind

            heap:destroy()

            if itemKind == nil then
                result = treasury:random(RPD.Treasury.Category.POTION)
            else
                result = RPD.ItemFactory:itemByName(itemKind)
            end
        end

        RPD.Statistics.potionsCooked = RPD.Statistics.potionsCooked + 1
        RPD.Badges:validatePotionsCooked()

        -- caveman: the pot is a non-passable object - a heap dropped on its
        -- cell can't be walked onto or clicked (the pot menu wins), so the
        -- brewed potion lands next to the pot instead
        local dropCell = level:getEmptyCellNextTo(cell)
        if not level:cellValid(dropCell) then
            dropCell = cell
        end
        level:animatedDrop(result, dropCell)
    end,
}
