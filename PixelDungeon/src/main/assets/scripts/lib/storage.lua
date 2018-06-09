--
-- User: mike
-- Date: 12.11.2017
-- Time: 20:16
-- This file is part of Remixed Pixel Dungeon.
--

local RPD = require "scripts/lib/commonClasses"

local seprent = require "scripts/lib/serpent"

local gameData  = {}
local levelData = {}

local gameStorage = {}

gameStorage.serializeGameData = function()
    return seprent.dump(gameData)
end

gameStorage.deserializeGameData = function(str)
    local res, _gameData = seprent.load(str)
    gameData = _gameData or {}
end

gameStorage.serializeLevelData = function()
    --RPD.glog("serealize")
    return seprent.dump(levelData)
end

gameStorage.deserializeLevelData = function(str)
    --RPD.glog("deserealize")
    local res, _levelData = seprent.load(str)
    levelData = _levelData or {}
end

gameStorage.resetLevelData = function()
    levelData = {}
    --RPD.glog("reset level data")
end

gameStorage.put = function(k,v)
    levelData[k] = v
    --RPD.glog("put: %s -> %s", tostring(k), tostring(seprent.dump({v})))
end

gameStorage.get = function(k)
    --RPD.glog("get: %s -> %s", tostring(k), tostring(seprent.dump({levelData[k]})))
    return levelData[k]
end

gameStorage.gamePut = function(k,v)
    gameData[k] = v
end

gameStorage.gameGet = function(k)
    return gameData[k]
end

return gameStorage