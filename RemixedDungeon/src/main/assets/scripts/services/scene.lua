---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by mike.
--- DateTime: 5/2/20 12:24 AM
---

local RPD = require "scripts/lib/commonClasses"

local gameScene = require "scripts.userServices.gameScene"

local GameControl = luajava.bindClass("com.nyrds.pixeldungeon.utils.GameControl")

local autoTestAi = require "scripts.userServices.autoTestAi"

local levels = RPD.DungeonGenerator:getLevelsList()
local levelsSize = levels:size()
local currentLevel = 0
local framesOnLevel = 0
local framesOnScene = 0
local prevScene


local service = {}

local function noneMode(self, scene)

end

local function stdModeOnStep(self, scene)
    if scene == "GameScene" then
        gameScene.onStep()
    end
end

local function levelsTestModeOnStep(self, scene)

    if scene ~= prevScene then
        prevScene = scene
        framesOnScene = 0
    else
        framesOnScene = framesOnScene + 1
    end

    if scene == "GameScene" then

        framesOnLevel = framesOnLevel + 1

        local hero = RPD.Dungeon.hero

        hero:ht(10000)
        hero:hp(hero:ht())

        if hero:myMove() then
           --RPD.glog("myMove")
            autoTestAi.step()
        end

        if framesOnLevel > 10000 then
            currentLevel = currentLevel + 1

            if currentLevel < levelsSize then
                framesOnLevel = 0

                local nextLevelId = levels:get(currentLevel)
                RPD.glog("trying level: %s", nextLevelId)
                GameControl:changeLevel(nextLevelId)
            else
                service.onStep = stdModeOnStep
                GameControl:titleScene()
            end
        end
    end

    if scene == "TitleScene" and framesOnScene > 2 then
        levels = RPD.DungeonGenerator:getLevelsList()
        local classes = {"WARRIOR","MAGE","ROGUE","HUNTRESS","ELF","NECROMANCER","GNOLL"}
        GameControl:startNewGame(classes[math.random(1, #classes)], 2, true)
    end
end

local modes = {}
modes["std"] = stdModeOnStep
modes["levelsTest"] = levelsTestModeOnStep

service.onStep = stdModeOnStep

service.setMode = function(self, mode)
    service.onStep = modes[mode] or noneMode
end

service.selectCell = function(self)
    gameScene.selectCell()
end

return service