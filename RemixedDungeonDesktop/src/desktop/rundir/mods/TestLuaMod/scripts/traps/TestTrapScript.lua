-- Test Lua script for the trap
function onEnter( trap, ch )
  -- Use the game's logging system instead of print
  GLog.debug("TestLuaScript: Trap activated by character " .. ch:getEntityKind())
  GLog.debug("TestLuaScript: This is a test from the custom mod")
  return true
end