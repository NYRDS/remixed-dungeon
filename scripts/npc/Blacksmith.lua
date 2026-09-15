--[[
    mob lua migration batch 16c-3: replaces java Blacksmith + WndBlacksmith
    (placed by CavesLevel d12+ BLACKSMITH rooms; quest state in lib/quest game
    storage under "blacksmith" - spawned/alternative recorded by
    QuestBridge.trySpawn from the CavesLevel shim, given/completed/reforged
    advance here. Reforge flow = two upgradeable picks + verify + confirm;
    the java effect bundle is inlined in reforge() below)
]]

local RPD          = require "scripts/lib/commonClasses"
local quest        = require "scripts/lib/quest"
local mob          = require "scripts/lib/mob"
local itemSelector = require "scripts/lib/itemSelector"

local QUEST = "blacksmith"

local function qstate()
    return quest.state(QUEST) or {}
end

-- java Blacksmith.verify: six gates, nil return = ok to reforge
local function verify(item1, item2)
    if item1:equals(item2) then
        return RPD.textById("Blacksmith_Verify1")
    end
    if item1:getEntityKind() ~= item2:getEntityKind() then
        return RPD.textById("Blacksmith_Verify2")
    end
    if not item1:isIdentified() or not item2:isIdentified() then
        return RPD.textById("Blacksmith_Verify3")
    end
    if item1.cursed or item2.cursed then
        return RPD.textById("Blacksmith_Verify4")
    end
    if item1:level() < 0 or item2:level() < 0 then
        return RPD.textById("Blacksmith_Verify5")
    end
    if not item1:isUpgradable() or not item2:isUpgradable() then
        return RPD.textById("Blacksmith_Verify6")
    end
    return nil
end

-- java Blacksmith.upgrade: higher-level item gains +1, lower is consumed
local function reforge(item1, item2)
    local first, second
    if item2:level() > item1:level() then
        first, second = item2, item1
    else
        first, second = item1, item2
    end

    local hero = RPD.Dungeon.hero

    RPD.playSound("snd_evoke")

    local speck = RPD.Sfx.Speck:factory(RPD.Sfx.Speck.UP)
    hero:getSprite():emitter():start(speck, 0.2, 3)
    RPD.ItemUtils:evoke(hero)

    if first:isEquipped(hero) then
        first:doUnequip(hero, true)
    end
    first:upgrade()
    RPD.glog(RPD.textById("Blacksmith_LooksBetter"), first:name())
    hero:spendAndNext(2)
    RPD.Badges:validateItemLevelAcquired(first)

    if second:isEquipped(hero) then
        second:doUnequip(hero, false)
    end
    second:detach(hero:getBelongings().backpack)
end

local function showMessage(text)
    local wnd = luajava.newInstance(RPD.Objects.Ui.WndMessage, text)
    RPD.GameScene:show(wnd)
end

-- WndBlacksmith port: two sequential picks, live verify, confirm dialog
local function reforgeWindow(self, chr)
    local first

    local function pickSecond()
        itemSelector.selectUpgradeableItem(function(second)
            if not second then
                return
            end

            local fail = verify(first, second)
            if fail then
                showMessage(fail)
                return
            end

            RPD.chooseOption(function(index)
                if index ~= 0 then
                    return
                end

                reforge(first, second)

                local st = qstate()
                st.reforged = true
                quest.state(QUEST, st)

                RPD.Journal:remove(RPD.textById("Journal_Troll"))
            end,
            self:getName(),
            RPD.textById("WndBlacksmith_Prompt"),
            RPD.textById("WndBlacksmith_Reforge"))
        end, RPD.textById("WndBlacksmith_Select"))
    end

    itemSelector.selectUpgradeableItem(function(item)
        if not item then
            return
        end
        first = item
        pickSecond()
    end, RPD.textById("WndBlacksmith_Select"))
end

-- verify/reforge exported for /debug/lua_eval probing (not engine hooks)
local M = mob.init({
    interact = function(self, chr)
        self:getSprite():turnTo(self:getPos(), chr:getPos())

        local st = qstate()

        if not st.given then

            RPD.showQuestWindow(self, st.alternative and "Blacksmith_Blood1" or "Blacksmith_Gold1")

            st.given = true
            st.completed = false
            quest.state(QUEST, st)

            local pick = RPD.item("RemixedPickaxe")
            chr:collectAnimated(pick)

            RPD.Journal:add(RPD.textById("Journal_Troll"))

        elseif not st.completed then
            local pick = chr:getBelongings():getEquipableItemPartialMatch("Pickaxe")
            if not pick or not pick:valid() then
                RPD.showQuestWindow(self, "Blacksmith_Txt2")
                return true
            end

            if st.alternative then
                if not pick:getBoolean("bloodStained") then
                    RPD.showQuestWindow(self, "Blacksmith_Txt4")
                else
                    pick:removeItem()
                    self:collect(pick)

                    RPD.showQuestWindow(self, "Blacksmith_Completed")

                    st.completed = true
                    st.reforged = false
                    quest.state(QUEST, st)
                end
            else
                local gold = chr:getBelongings():getItem("DarkGold")
                if not gold or gold:quantity() < 15 then
                    RPD.showQuestWindow(self, "Blacksmith_Txt3")
                else
                    pick:removeItem()
                    self:collect(pick)

                    gold:detachAll(chr:getBelongings().backpack)

                    RPD.showQuestWindow(self, "Blacksmith_Completed")

                    st.completed = true
                    st.reforged = false
                    quest.state(QUEST, st)
                end
            end

        elseif not st.reforged then
            reforgeWindow(self, chr)
        else
            RPD.showQuestWindow(self, "Blacksmith_GetLost")
        end
        return true
    end
})

M.verify = verify
M.reforge = reforge

return M
