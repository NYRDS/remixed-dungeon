--
-- User: mike
-- Date: 12.11.2017
-- Time: 20:16
-- This file is part of Remixed Pixel Dungeon.
--

local seprent = require "scripts/lib/serpent"

local gameData  = {}
local levelData = {}

local gameStorage = {}

local res

gameStorage.serializeGameData = function()
    return seprent.dump(gameData)
end

gameStorage.deserializeGameData = function(str)
    res, gameData = seprent.load(str)
    gameData = gameData or {}
end

gameStorage.serializeLevelData = function()
    return seprent.dump(levelData)
end

gameStorage.deserializeLevelData = function(str)
    res, levelData = seprent.load(str)
    levelData = levelData or {}
end


gameStorage.put = function(k,v)
    levelData[k] = v
end

gameStorage.get = function(k)
    return levelData[k]
end

gameStorage.gamePut = function(k,v)
    gameData[k] = v
end

gameStorage.gameGet = function(k)
    return gameData[k]
end

return gameStorage