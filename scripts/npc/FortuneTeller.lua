--[[
    mob lua migration batch 16b: replaces java FortuneTellerNPC + WndFortuneTeller
]]

local RPD = require "scripts/lib/commonClasses"
local itemSelector = require "scripts/lib/itemSelector"

local mob = require "scripts/lib/mob"

return mob.init({
    interact = function(self, chr)
        local price  = RPD.CharUtils:goldPrice(chr, 50)
        local unknown = RPD.CharUtils:countUnidentified(chr)

        local function picked(item)
            if not item then
                return
            end
            RPD.CharUtils:identifyItem(chr, item)
            chr:spendGold(price)
        end

        local function dialog(index)
            if index == 0 then
                if chr:gold() < price then
                    return
                end
                if unknown > 0 then
                    itemSelector.selectUnidentifiedItem(picked, "ScrollOfIdentify_InvTitle")
                else
                    RPD.showQuestWindow(self, "WndFortuneTeller_No_Item")
                end
                return
            end

            if index == 1 then
                local allCost = price * unknown
                if chr:gold() < allCost then
                    return
                end
                chr:spendGold(allCost)
                chr:getBelongings():identify()
                return
            end
        end

        local text = RPD.textById("WndFortuneTeller_Instruction"):format(price)

        if unknown > 0 then
            RPD.chooseOption(dialog,
                    self:getName(),
                    text,
                    RPD.textById("Wnd_Button_Yes") .. " (" .. price .. ")",
                    RPD.textById("WndFortuneTeller_IdentifyAll") .. " ( " .. price * unknown .. " )",
                    "Wnd_Button_No"
            )
        else
            RPD.chooseOption(dialog,
                    self:getName(),
                    text,
                    RPD.textById("Wnd_Button_Yes") .. " (" .. price .. ")",
                    "Wnd_Button_No"
            )
        end
    end
})
