-- Test Lua script for an item
function onUse( item, cell )
  -- Use the game's logging system instead of print
  GLog.debug("TestLuaScript: Item " .. item:getEntityKind() .. " used at cell " .. cell)
  GLog.debug("TestLuaScript: This is a test from the custom mod item script")
  return true
end