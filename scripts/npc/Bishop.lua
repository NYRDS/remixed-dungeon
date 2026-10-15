--[[
    Created by mike.
    DateTime: 19.01.19 21:24
    This file is part of pixel-dungeon-remix
]]

local RPD = require "scripts/lib/commonClasses"

local mob = require"scripts/lib/mob"

local quest = require"scripts/lib/quest"

local npc
local client

local lesserPrice
local greatPrice
local removeCursePrice

local questName = "BishopUndeadQuest"
local targetKills = 15
local undeadKinds = {"Skeleton","Zombie","Wraith","Undead","EnslavedSoul","ExplodingSkull","DeathKnight","DreadKnight","JarOfSouls"}

local undeadSlain = function()
    local state = quest.state(questName)
    if not state then
        return 0
    end
    local total = 0
    for _, kind in pairs(undeadKinds) do
        total = total + ((state.kills or {})[kind] or 0)
    end
    return total
end

local questOfferDialog = function(index)
    if index == 0 then
        quest.give(questName, client, {kills = undeadKinds})
        npc:say("Bishop_quest_given")
    else
        npc:say("Bishop_bye")
    end
end

local dialog = function(index)
    if index == 0 then
        if client:gold() >= lesserPrice then
            client:spendGold(lesserPrice)
            client:getSprite():emitter():burst( RPD.Sfx.ShaftParticle.FACTORY, 2 );
            RPD.affectBuff(client,RPD.Buffs.Blessed,100)
            return
        end
        npc:say("Bishop_no_money")
    end

    if index == 1 then
        if client:gold() >= greatPrice then
            client:spendGold(greatPrice)
            client:getSprite():emitter():burst( RPD.Sfx.ShaftParticle.FACTORY, 5 );
            RPD.affectBuff(client,RPD.Buffs.Blessed,500)
            RPD.affectBuff(client,RPD.Buffs.Blessed,500)
            return
        end
        npc:say("Bishop_no_money")
    end

    if index == 2 then
        if client:gold() >= removeCursePrice then
            client:spendGold(removeCursePrice)
            -- RPD.item() swaps forbidden items for gold (NO_SCROLLS challenge), and gold has no uncurse
            local scroll = RPD.ItemFactory:itemByName("ScrollOfRemoveCurse")
            scroll:uncurse(client:getBelongings())
            RPD.glogp("ScrollOfRemoveCurse_Proced")
            client:getSprite():emitter():start(RPD.Sfx.ShadowParticle.UP, 0.05, 10);
            return
        end

        npc:say("Bishop_no_money")
    end

    if index == 3 then
        npc:say("Bishop_bye")
    end
end


return mob.init({
    interact = function(self, chr)
        client = chr
        npc = self

        if not RPD.checkBadge("PRIEST_QUEST_COMPLETED") then
            if not quest.isGiven(questName) then
                RPD.chooseOption( questOfferDialog,
                        "Bishop_quest_title",
                        "Bishop_quest_text",
                        "Bishop_quest_accept",
                        "Bishop_quest_not_now"
                )
                return
            end

            if not quest.isCompleted(questName) then
                local slain = undeadSlain()

                if slain >= targetKills then
                    quest.complete(questName)
                    RPD.Badges:displayBadge(RPD.Badges.Badge:valueOf("PRIEST_QUEST_COMPLETED"))

                    RPD.affectBuff(chr, RPD.Buffs.Blessed, 500)
                    chr:heal(chr:ht(), self)
                    chr:collectAnimated(RPD.item("Gold", 300))

                    RPD.showQuestWindow(self, "Bishop_quest_done")
                else
                    RPD.showQuestWindow(self, RPD.textById("Bishop_quest_progress"):format(slain, targetKills))
                end
                return
            end
        end

        local priceFactor = RPD.RemixedDungeon:getDifficultyFactor() * math.pow( 1.05, (client:lvl()-1))

        lesserPrice      = math.floor(100 * priceFactor)
        greatPrice       = math.floor(500 * priceFactor)
        removeCursePrice = math.floor(200 * priceFactor)

        RPD.chooseOption( dialog,
                "Bishop_title",
                "Bishop_text",
                RPD.textById("Bishop_lesser_bless"):format(lesserPrice),
                RPD.textById("Bishop_great_bless"):format(greatPrice),
                RPD.textById("Bishop_remove_curse"):format(removeCursePrice),
                "Bishop_not_interested"
        )
    end,
    die = function(self, cause)
        local hero = RPD.Dungeon.hero
        hero:ht(math.max(hero:ht()/2,1))
        hero:damage(hero:ht(), self)

        hero:getSprite():emitter():burst( RPD.Sfx.ShadowParticle.CURSE, 6 )

        RPD.playSound( "snd_cursed" )
    end,

    spawn = function(me, level)
        RPD.setAi(me,"NpcDefault")
    end
})
