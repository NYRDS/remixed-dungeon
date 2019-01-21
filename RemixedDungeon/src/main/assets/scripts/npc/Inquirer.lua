--
-- User: mike
-- Date: 02.01.2018
-- Time: 00:30
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local npc

local dialog = function(index)
    if index == 0 then
        local surveys = luajava.bindClass("com.nyrds.pixeldungeon.support.PollfishSurveys")
        surveys:init()
        surveys:showSurvey()
        return
    end

    if index == 1 then
        RPD.showStoryWindow("Inquirer_privacyPolicy")
        return
    end

    if index == 2 then
        npc:say("Inquirer_bye")
        return
    end
end


return mob.init({
    interact = function(self, chr)
        npc =self
        RPD.chooseOption( dialog,
                "Inquirer_title",
                "Inquirer_text",
                "Inquirer_yes",
                "Inquirer_show_privacy",
                "Inquirer_no"
        )
    end
})
