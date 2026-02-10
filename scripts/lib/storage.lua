--
-- User: mike
-- Date: 12.11.2017
-- Time: 20:16
-- This file is part of Remixed Pixel Dungeon.
--
local RPD = require "scripts/lib/commonClasses"
local serpent = require "scripts/lib/serpent"

local gameData  = {}
local levelData = {}
local modData   = {}


local gameStorage = {}

gameStorage.serializeGameData = function()
    --RPD.debug("serialize game")
    return serpent.dump(gameData)
end

gameStorage.deserializeGameData = function(str)
    --RPD.debug("deserialize game")
    local res, _gameData = serpent.load(str)
    gameData = _gameData or {}
end

gameStorage.serializeLevelData = function()
    --RPD.debug("serialize")
    return serpent.dump(levelData)
end

gameStorage.deserializeLevelData = function(str)
    --RPD.debug("deserialize")
    local res, _levelData = serpent.load(str)
    levelData = _levelData or {}
end

gameStorage.serializeModData = function()
    --RPD.debug("serialize mod")
    return serpent.dump(modData)
end

gameStorage.deserializeModData = function(str)
    --RPD.debug("deserialize mod")
    local res, _modData = serpent.load(str)
    modData = _modData or {}
end

gameStorage.resetLevelData = function()
    levelData = {}
    --RPD.debug("reset level data")
end

gameStorage.put = function(k,v)
    levelData[k] = v
    --RPD.debug("put: %s -> %s", tostring(k), tostring(serpent.dump({v})))
end

gameStorage.get = function(k, dv)
    --RPD.debug("get: %s -> %s", tostring(k), tostring(serpent.dump({levelData[k]})))
    return levelData[k] or dv
end

gameStorage.gamePut = function(k,v)
    --RPD.debug("game put: %s -> %s",
    --        tostring(k),
    --       tostring(serpent.dump({v})))
    gameData[k] = v
end

gameStorage.gameGet = function(k, dv)
    --RPD.debug("game get: %s -> %s", tostring(k), tostring(serpent.dump({levelData[k]})))
    return gameData[k] or dv
end

gameStorage.modPut = function(k,v)
    --RPD.debug("mod put: %s -> %s",
    --        tostring(k),
    --       tostring(serpent.dump({v})))
    modData[k] = v
end

gameStorage.modGet = function(k, dv)
    --RPD.debug("mod get: %s -> %s", tostring(k), tostring(serpent.dump({levelData[k]})))
    return modData[k] or dv
end

return gameStorage