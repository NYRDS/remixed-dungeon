--[[
    mob lua migration batch 16c-3: replaces java Shopkeeper, shared by the
    TownShopkeeper and ImpShopkeeper kinds (their jsons point scriptFile
    here; ImpShopkeeper's first-sight greeting is kind-gated in act below).
    Backpack fills lazily at first interact; per-mob bagSold state rides
    restoreData. AzuterronNPC.lua reuses interact for its completed phase.
]]

local RPD = require "scripts/lib/commonClasses"
local mob = require "scripts/lib/mob"

local function treasury()
    return RPD.Treasury:getLevelTreasury()
end

local function countFood(backpack)
    local ret = 0
    local n = backpack.items:size()
    for i = 0, n - 1 do
        local it = backpack.items:get(i)
        if RPD.ItemUtils:isFood(it) then
            ret = ret + it:quantity()
        end
    end
    return ret
end

-- java Shopkeeper.collect: treasury may substitute the item, and a bag the
-- hero already owns never gets stocked
local function collectStock(self, item)
    item = treasury():check(item)
    return self:collect(item)
end

local function stockFood(self)
    if not RPD.ModdingBase:inRemixed() or RPD.GameLoop:getDifficulty() >= 2 then
        return
    end

    local backpack = self:getBelongings().backpack
    if countFood(backpack) >= 3 then
        return
    end

    local foodSupply = RPD.item("OverpricedRation")
    foodSupply:quantity(5)
    collectStock(self, foodSupply)
end

local function stockBag(self, data)
    if (data.bagSold or "") ~= "" then
        return
    end

    local bag = RPD.Badges:getNotBroughtBag()
    if not bag or not bag:valid() then
        return
    end

    if self:getBelongings():getItem(bag:getEntityKind()) ~= nil then
        return
    end

    local heroCopy = RPD.Dungeon.hero:getItem(bag:getEntityKind())
    if heroCopy and heroCopy:valid() then
        return
    end

    if collectStock(self, bag) then
        data.bagSold = bag:getEntityKind()
    end
end

-- java interact fill loop: keep ~2 slots free for trades, cap attempts
local function stockRandom(self)
    local backpack = self:getBelongings().backpack

    local attempts = 0
    while backpack.items:size() < backpack:getSize() + 2 and attempts < 100 do
        attempts = attempts + 1

        local item = treasury():random()

        if item:getEntityKind() ~= "Gold" and not item.cursed then
            local supply = self:getItem(item:getEntityKind())

            local duplicate = false
            if supply and supply:valid() then
                if not item.stackable then
                    duplicate = true
                elseif supply:price() > 100 then
                    duplicate = true
                end
            end

            if not duplicate then
                collectStock(self, item)
            end
        end
    end
end

return mob.init({
    act = function(self)
        -- ImpShopkeeper greets the hero once per run (java ImpShopkeeper.act)
        if self:getEntityKind() ~= "ImpShopkeeper" then
            return
        end

        local data = mob.restoreData(self)
        if data.seenBefore then
            return
        end

        if RPD.CharUtils:isVisible(self) then
            data.seenBefore = true
            mob.storeData(self, data)
            self:say(RPD.textById("ImpShopkeeper_Greetings"))
        end
    end,

    interact = function(self, chr)
        local data = mob.restoreData(self)

        stockFood(self)
        stockBag(self, data)
        stockRandom(self)

        if data.bagSold then
            mob.storeData(self, data)
        end

        RPD.showTradeWindow(self, chr)
        return true
    end,

    buyMode = function()
        return RPD.BackpackMode.FOR_BUY
    end,

    sellMode = function()
        return RPD.BackpackMode.FOR_SALE
    end
})
