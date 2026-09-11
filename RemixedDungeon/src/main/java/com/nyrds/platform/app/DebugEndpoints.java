package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.RemoteControlled;
import com.nyrds.pixeldungeon.alchemy.AlchemyRecipe;
import com.nyrds.pixeldungeon.alchemy.AlchemyRecipes;
import com.nyrds.pixeldungeon.alchemy.InputItem;
import com.nyrds.pixeldungeon.alchemy.OutputItem;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.ml.actions.Attack;
import com.nyrds.pixeldungeon.ml.actions.Interact;
import com.nyrds.pixeldungeon.ml.actions.InteractObject;
import com.nyrds.pixeldungeon.ml.actions.Move;
import com.nyrds.pixeldungeon.ml.actions.Unlock;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.pixeldungeon.windows.WndHelper;
import com.nyrds.pixeldungeon.windows.WndPetBag;
import com.nyrds.pixeldungeon.windows.WndPetInventoryOptions;
import com.nyrds.pixeldungeon.windows.WndPetSelect;
import com.nyrds.platform.storage.SaveUtils;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.pixeldungeon.windows.WndSettings;
import com.watabou.pixeldungeon.windows.WndTitledMessage;
import com.watabou.pixeldungeon.windows.elements.GenericInfo;
import com.watabou.utils.Bundle;
import fi.iki.elonen.NanoHTTPD;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

public class DebugEndpoints {
    
    private static JSONObject createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorMessage);
        return new JSONObject(response);
    }

    public static NanoHTTPD.Response handleDebugChangeLevel(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int level = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("level=")) {
                        String levelStr = param.substring(6); // Remove "level=" prefix
                        try {
                            level = Integer.parseInt(URLDecoder.decode(levelStr, "UTF-8"));
                        } catch (Exception e) {
                            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                                "{\"error\":\"Invalid level parameter\"}");
                        }
                        break;
                    }
                }
            }

            if (level < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid level parameter\"}");
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            // caveman: level switch touches scene + actor state - game thread only.
            // pushUiTaskAndWait runs inline when the loop is down (headless test use).
            final String finalLevelId = String.valueOf(level);
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Create a position object for the level
                    Position position = new Position();
                    position.levelId = finalLevelId;

                    // Use the createLevel method
                    Level newLevel = DungeonGenerator.createLevel(position);

                    // Collect any existing mobs to transfer to the new level
                    Collection<Mob> mobs = new ArrayList<>();
                    if (Dungeon.level != null) {
                        for (Mob mob : Dungeon.level.mobs) {
                            mobs.add(mob);
                        }
                    }

                    // Change the level - use a single integer position instead of array
                    int startPos = 1 + 1 * newLevel.getWidth(); // Convert x,y to cell position
                    Dungeon.switchLevel(newLevel, startPos, mobs); // Start at position 1,1
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Changed to level %d\",\"level\":%d}", level, level));
        } catch (Exception e) {
            GLog.w("Error in handleDebugChangeLevel: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugCreateMob(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String mobType = null;
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        mobType = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (mobType == null || mobType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing mob type parameter\"}");
            }

            boolean owned = false;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.equals("owned=true")) {
                        owned = true;
                        break;
                    }
                }
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            // Generate random coordinates if not specified
            if (x < 0 || y < 0) {
                // Find a random free cell - using the available method
                int cell = Dungeon.level.randomPassableCell();
                x = cell % Dungeon.level.getWidth();
                y = cell / Dungeon.level.getWidth();
            }

            final int finalX = x;
            final int finalY = y;
            final String finalMobType = mobType;
            final boolean finalOwned = owned;
            final int[] mobId = new int[1];
            final String[] error = new String[1];

            // caveman: run on game thread like other mutating endpoints.
            // spawnMob touches Actor.all (not thread-safe) and scene sprites.
            CountDownLatch latch = new CountDownLatch(1);
            GameLoop.pushUiTask(() -> {
                try {
                    Mob mob = MobFactory.mobByName(finalMobType);
                    mob.pos = finalX + finalY * Dungeon.level.getWidth();

                    if (finalOwned && Dungeon.hero != null) {
                        mob.makePet(Dungeon.hero);
                    }

                    // use level.spawnMob - it does Actor.addDelayed + sprite + onSpawn.
                    // old way (occupyCell + mobs.add only) made ghost mob: findChar blind to it,
                    // no sprite, never took turns.
                    Dungeon.level.spawnMob(mob);
                    mobId[0] = mob.getId();
                } catch (Exception e) {
                    error[0] = "Error creating mob: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for mob creation\"}");
            }
            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"%s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Created mob '%s' at (%d,%d)\",\"mobType\":\"%s\",\"x\":%d,\"y\":%d,\"owned\":%b,\"id\":%d}",
                    mobType, x, y, mobType, x, y, owned, mobId[0]));
        } catch (Exception e) {
            GLog.w("Error in handleDebugCreateMob: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugCreateItem(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String itemType = null;
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        itemType = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (itemType == null || itemType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing item type parameter\"}");
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            // Generate random coordinates if not specified
            if (x < 0 || y < 0) {
                // Find a random free cell - using the available method
                int cell = Dungeon.level.randomPassableCell();
                x = cell % Dungeon.level.getWidth();
                y = cell / Dungeon.level.getWidth();
            }

            // caveman: item drop mutates level heaps - game thread only
            final String finalItemType = itemType;
            final int finalX = x;
            final int finalY = y;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Create the item using the factory
                    Item item = ItemFactory.itemByName(finalItemType);

                    // Drop the item at the specified location
                    Dungeon.level.drop(item, finalX + finalY * Dungeon.level.getWidth());
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Created item '%s' at (%d,%d)\",\"itemType\":\"%s\",\"x\":%d,\"y\":%d}",
                    itemType, x, y, itemType, x, y));
        } catch (Exception e) {
            GLog.w("Error in handleDebugCreateItem: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugChangeMap(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String mapType = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        mapType = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    }
                }
            }

            if (mapType == null || mapType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing map type parameter\"}");
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            // Get the current depth
            int depth = Dungeon.depth;

            // For map type changes, we'll use the DungeonGenerator approach
            // This is a simplified approach - we'll create a position based on the map type

            // Map the type to a known level ID
            String levelId;
            switch(mapType.toLowerCase()) {
                case "sewer":
                case "sewers":
                    levelId = "1"; // Sewer level
                    break;
                case "prison":
                    levelId = "4"; // Prison level
                    break;
                case "caves":
                case "cave":
                    levelId = "7"; // Caves level
                    break;
                case "city":
                    levelId = "10"; // City level
                    break;
                case "halls":
                    levelId = "13"; // Halls level
                    break;
                default:
                    levelId = "1"; // Default to sewer level
                    break;
            }

            // caveman: level switch touches scene + actor state - game thread only
            final String finalLevelId = levelId;
            final String finalMapType = mapType;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    Position position = new Position();
                    position.levelId = finalLevelId;

                    // Use the createLevel method
                    Level newLevel = DungeonGenerator.createLevel(position);

                    if (newLevel == null) {
                        error[0] = String.format("Unknown map type: %s", finalMapType);
                        return;
                    }

                    // Collect any existing mobs to transfer to the new level
                    Collection<Mob> mobs = new ArrayList<>();
                    if (Dungeon.level != null) {
                        for (Mob mob : Dungeon.level.mobs) {
                            mobs.add(mob);
                        }
                    }

                    // Switch to the new level - use a single integer position instead of array
                    int startPos = 1 + 1 * newLevel.getWidth(); // Convert x,y to cell position
                    Dungeon.switchLevel(newLevel, startPos, mobs);
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"%s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Changed map to type '%s'\",\"mapType\":\"%s\"}",
                    mapType, mapType));
        } catch (Exception e) {
            GLog.w("Error in handleDebugChangeMap: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGiveItem(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String itemType = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        itemType = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    }
                }
            }

            if (itemType == null || itemType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing item type parameter\"}");
            }

            // Check if game state is initialized
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            // caveman: inventory collection touches hero state - game thread only
            final String finalItemType = itemType;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Create the item using the factory
                    Item item = ItemFactory.itemByName(finalItemType);

                    // Give the item to the hero
                    Dungeon.hero.getBelongings().collect(item);
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Gave item '%s' to hero\",\"itemType\":\"%s\"}",
                    itemType, itemType));
        } catch (Exception e) {
            GLog.w("Error in handleDebugGiveItem: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugSpawnAt(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String entityType = null;
            String entityValue = null;
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("entity=")) {
                        entityType = URLDecoder.decode(param.substring(7), "UTF-8"); // Remove "entity=" prefix
                    } else if (param.startsWith("value=")) {
                        entityValue = URLDecoder.decode(param.substring(6), "UTF-8"); // Remove "value=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (entityType == null || entityType.isEmpty() || entityValue == null || entityValue.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing entity type or value parameter\"}");
            }

            if (x < 0 || y < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid coordinates\"}");
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            final boolean spawnMob = "mob".equalsIgnoreCase(entityType);
            final boolean spawnItem = "item".equalsIgnoreCase(entityType);
            if (!spawnMob && !spawnItem) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Unknown entity type: %s\"}", entityType));
            }

            // caveman: actors + heaps are game-thread state
            final String finalEntityValue = entityValue;
            final int finalCellPos = x + y * Dungeon.level.getWidth();
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    if (spawnMob) {
                        // Spawn a mob
                        Mob mob = MobFactory.mobByName(finalEntityValue);

                        // Set the mob's position
                        mob.pos = finalCellPos;

                        // Add the mob to the game
                        Actor.occupyCell(mob);
                    } else {
                        // Spawn an item
                        Item item = ItemFactory.itemByName(finalEntityValue);

                        // Drop the item at the specified location
                        Dungeon.level.drop(item, finalCellPos);
                    }
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            if (spawnMob) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Spawned mob '%s' at (%d,%d)\",\"entityType\":\"%s\",\"entityValue\":\"%s\",\"x\":%d,\"y\":%d}",
                        entityValue, x, y, entityType, entityValue, x, y));
            }
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Spawned item '%s' at (%d,%d)\",\"entityType\":\"%s\",\"entityValue\":\"%s\",\"x\":%d,\"y\":%d}",
                    entityValue, x, y, entityType, entityValue, x, y));
        } catch (Exception e) {
            GLog.w("Error in handleDebugSpawnAt: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugContinueGame(NanoHTTPD.IHTTPSession session) {
        try {
            String heroClass = "WARRIOR";
            String query = session.getQueryParameterString();
            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("class=")) {
                        heroClass = java.net.URLDecoder.decode(param.substring(6), "UTF-8").toUpperCase();
                    }
                }
            }

            HeroClass selectedClass = null;
            for (HeroClass cls : HeroClass.values()) {
                if (cls.name().equals(heroClass)) {
                    selectedClass = cls;
                    break;
                }
            }

            if (selectedClass == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Unknown hero class: %s\"}", heroClass));
            }

            if (Dungeon.hero != null && Dungeon.level != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game already in progress\"}");
            }

            java.io.File saveFile = new java.io.File(
                    com.nyrds.platform.storage.FileSystem.getUserDataPath(com.nyrds.pixeldungeon.ml.BuildConfig.SAVES_PATH),
                    SaveUtils.gameFile(selectedClass));
            if (!saveFile.exists()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"No save found for %s\"}", heroClass));
            }

            Dungeon.heroClass = selectedClass;

            InterlevelScene.scheduleAndWait(InterlevelScene.Mode.CONTINUE, new Position(), "Continue game: " + heroClass);

            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Continue failed - level not loaded\"}");
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Continued game as %s\",\"heroClass\":\"%s\",\"depth\":%d}",
                    heroClass, heroClass, Dungeon.depth));
        } catch (Exception e) {
            GLog.w("Error in handleDebugContinueGame: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugStartGame(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String heroClass = "WARRIOR"; // Default hero class (using enum name)
            int difficulty = 0; // Default difficulty

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("class=")) {
                        heroClass = URLDecoder.decode(param.substring(6), "UTF-8").toUpperCase(); // Remove "class=" prefix and convert to uppercase
                    } else if (param.startsWith("difficulty=")) {
                        try {
                            difficulty = Integer.parseInt(URLDecoder.decode(param.substring(11), "UTF-8")); // Remove "difficulty=" prefix
                        } catch (NumberFormatException e) {
                            // Use default difficulty if parsing fails
                        }
                    }
                }
            }

            // Get the hero class enum value
            HeroClass selectedClass = null;
            for (HeroClass cls : HeroClass.values()) {
                if (cls.name().equals(heroClass)) {
                    selectedClass = cls;
                    break;
                }
            }

            if (selectedClass == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Unknown hero class: %s. Valid classes: WARRIOR, MAGE, ROGUE, HUNTRESS, ELF, NECROMANCER, GNOLL, PRIEST, DOCTOR\"}", heroClass));
            }

            // caveman: startNewGame swaps Dungeon statics and switches scenes - game thread only
            final String finalHeroClass = heroClass;
            final int finalDifficulty = difficulty;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Call the startNewGame method - using the correct method from GameControl
                    GameControl.startNewGame(finalHeroClass, finalDifficulty, false);
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Started new game with %s\",\"heroClass\":\"%s\",\"difficulty\":%d}",
                    heroClass, heroClass, difficulty));
        } catch (Exception e) {
            GLog.w("Error in handleDebugStartGame: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetGameState(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            // caveman: use public accessors. raw fields HP/HT/lvl are Scrambler-encoded
            // (reflection here returned garbage like -2081372377) and Hero has no 'lvl'.
            int heroLvl = Dungeon.hero.lvl();
            int heroHp = Dungeon.hero.hp();
            int heroHt = Dungeon.hero.ht();

            // caveman: actor time + now exposed so turn-economy tests can check spend per action
            String jsonString = String.format(
                "{\"hero\":{\"class\":\"%s\",\"level\":%d,\"hp\":%d,\"max_hp\":%d,\"actor_time\":%f,\"world_time\":%f},\"level\":{\"depth\":%d,\"width\":%d,\"height\":%d},\"depth\":%d}",
                Dungeon.hero.className(),
                heroLvl,
                heroHp,
                heroHt,
                Dungeon.hero.actorTime(),
                Actor.localTime(),
                Dungeon.depth,
                Dungeon.level.getWidth(),
                Dungeon.level.getHeight(),
                Dungeon.depth
            );

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetGameState: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetHeroInfo(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            Bundle heroBundle = new Bundle();
            Dungeon.hero.storeInBundle(heroBundle);
            
            String jsonString = heroBundle.serialize();
            
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetHeroInfo: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetLevelInfo(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // Create a simple JSON response instead of using Bundle.toJson()
            String jsonString = String.format(
                "{\"depth\":%d,\"width\":%d,\"height\":%d,\"name\":\"%s\"}",
                Dungeon.depth,
                Dungeon.level.getWidth(),
                Dungeon.level.getHeight(),
                Dungeon.level.getClass().getSimpleName()
            );
            
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetLevelInfo: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetMobs(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // Create JSON array of mobs
            StringBuilder mobsJson = new StringBuilder("[");
            boolean first = true;
            for (Mob mob : Dungeon.level.mobs) {
                if (!first) {
                    mobsJson.append(",");
                }
                
                Bundle mobBundle = new Bundle();
                mob.storeInBundle(mobBundle);
                String mobJson = mobBundle.serialize();
                // Remove trailing } and add id, pos, x, y, owned fields
                if (mobJson.endsWith("}")) {
                    mobJson = mobJson.substring(0, mobJson.length() - 1);
                    int width = Dungeon.level.getWidth();
                    int mobX = mob.pos % width;
                    int mobY = mob.pos / width;
                    boolean owned = mob.getOwnerId() == (Dungeon.hero != null ? Dungeon.hero.getId() : -1);
                    mobJson += String.format(",\"id\":%d,\"pos\":%d,\"x\":%d,\"y\":%d,\"owned\":%b,\"type\":\"%s\"}",
                        mob.getId(), mob.pos, mobX, mobY, owned, mob.getEntityKind());
                }
                mobsJson.append(mobJson);
                first = false;
            }
            mobsJson.append("]");
            
            String jsonString = String.format("{\"count\":%d,\"mobs\":%s}", Dungeon.level.mobs.size(), mobsJson.toString());
            
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetMobs: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetItems(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // Access the heaps field using reflection
            Field heapsField = Level.class.getDeclaredField("heaps");
            heapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Heap> heaps = (Map<Integer, Heap>) heapsField.get(Dungeon.level);

            // Create JSON array of items
            StringBuilder itemsJson = new StringBuilder("[");
            boolean first = true;
            for (Heap heap : heaps.values()) {
                if (!first) {
                    itemsJson.append(",");
                }
                
                // Get the first item in the heap to represent the heap
                Item item = heap.peek();
                if (item != null) {
                    Bundle itemBundle = new Bundle();
                    item.storeInBundle(itemBundle);
                    itemsJson.append(itemBundle.serialize());
                }
                first = false;
            }
            itemsJson.append("]");
            
            String jsonString = String.format("{\"count\":%d,\"items\":%s}", heaps.size(), itemsJson.toString());
            
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetItems: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetInventory(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            // Access belongings using reflection
            Field belongingsField = Char.class.getDeclaredField("belongings");
            belongingsField.setAccessible(true);
            Belongings belongings = (Belongings) belongingsField.get(Dungeon.hero);

            // Create JSON response for inventory
            StringBuilder inventoryJson = new StringBuilder("{");
            
            // Add weapon if exists
            if (belongings.weapon != null) {
                Bundle weaponBundle = new Bundle();
                belongings.weapon.storeInBundle(weaponBundle);
                inventoryJson.append(String.format(
                    "\"weapon\":%s,",
                    weaponBundle.serialize()
                ));
            }
            
            // Add armor if exists
            if (belongings.armor != null) {
                Bundle armorBundle = new Bundle();
                belongings.armor.storeInBundle(armorBundle);
                inventoryJson.append(String.format(
                    "\"armor\":%s,",
                    armorBundle.serialize()
                ));
            }
            
            // Add backpack items
            StringBuilder backpackJson = new StringBuilder("[");
            boolean first = true;
            for (Item item : belongings.backpack.items) {
                if (!first) {
                    backpackJson.append(",");
                }
                Bundle itemBundle = new Bundle();
                item.storeInBundle(itemBundle);
                backpackJson.append(itemBundle.serialize());
                first = false;
            }
            backpackJson.append("]");
            
            inventoryJson.append(String.format("\"backpack\":%s}", backpackJson.toString()));
            
            String jsonString = inventoryJson.toString();
            
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetInventory: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugSetHeroStat(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String stat = null;
            int value = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("stat=")) {
                        stat = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "stat=" prefix
                    } else if (param.startsWith("value=")) {
                        try {
                            value = Integer.parseInt(URLDecoder.decode(param.substring(6), "UTF-8")); // Remove "value=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid value
                        }
                    }
                }
            }

            if (stat == null || stat.isEmpty() || value < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing stat or value parameter\"}");
            }

            // Check if game state is initialized
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            // Set the specified stat
            switch (stat.toLowerCase()) {
                case "hp": {
                    // caveman: hp writes fire UI observers - game thread only
                    final Hero hero = Dungeon.hero;
                    final int finalValue = value;
                    GameLoop.pushUiTaskAndWait(() -> hero.hp(Math.min(finalValue, hero.ht())));
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                        String.format("{\"success\":true,\"message\":\"Set HP to %d\",\"stat\":\"%s\",\"value\":%d}",
                            value, stat, value));
                }
                case "max_hp":
                case "ht":
                    // Note: HT is a private field, so we can't set it directly
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Setting max HP directly is not supported through this endpoint.\"}"));
                case "str":
                    // Note: STR is a private field, so we can't set it directly
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Setting strength directly is not supported through this endpoint.\"}"));
                case "lvl":
                    // Level cannot be set directly, but we can adjust exp to reach desired level
                    // This is approximate since exact exp values per level aren't exposed
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Setting level directly is not supported. Use exp instead.\"}"));
                default:
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Unknown stat: %s. Valid stats: hp, max_hp, str, exp\"}", stat));
            }

            //return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
            //    String.format("{\"success\":true,\"message\":\"Set %s to %d\",\"stat\":\"%s\",\"value\":%d}",
            //        stat, value, stat, value));
        } catch (Exception e) {
            GLog.w("Error in handleDebugSetHeroStat: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugKillMob(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;
            int mobId = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("id=")) {
                        try {
                            mobId = Integer.parseInt(URLDecoder.decode(param.substring(3), "UTF-8"));
                        } catch (NumberFormatException e) {
                            // Ignore invalid id
                        }
                    }
                }
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // caveman: mob lookup + die() fire death effects and sprites - game thread only
            final int finalMobId = mobId;
            final int finalX = x;
            final int finalY = y;
            final int[] killedId = new int[1];
            final boolean[] found = new boolean[1];
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    Mob targetMob = null;

                    if (finalMobId > 0) {
                        // Find mob by ID
                        for (Mob mob : Dungeon.level.mobs) {
                            if (mob.getId() == finalMobId) {
                                targetMob = mob;
                                break;
                            }
                        }
                    } else if (finalX >= 0 && finalY >= 0) {
                        // Find mob at the specified coordinates
                        int cellPos = finalX + finalY * Dungeon.level.getWidth();
                        for (Mob mob : Dungeon.level.mobs) {
                            if (mob.pos == cellPos) {
                                targetMob = mob;
                                break;
                            }
                        }
                    }

                    if (targetMob == null) {
                        return;
                    }

                    found[0] = true;
                    // Kill the mob
                    targetMob.die(targetMob);
                    killedId[0] = targetMob.getId();
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            if (!found[0]) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"No mob found (id=%d or coords=(%d,%d))\"}", mobId, x, y));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Killed mob with id=%d\",\"id\":%d}",
                    killedId[0], killedId[0]));
        } catch (Exception e) {
            GLog.w("Error in handleDebugKillMob: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugRemoveItem(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (x < 0 || y < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid coordinates\"}");
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // caveman: heap destroy mutates level state - game thread only
            final int finalX = x;
            final int finalY = y;
            final boolean[] found = new boolean[1];
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Remove item at the specified coordinates
                    int cellPos = finalX + finalY * Dungeon.level.getWidth();

                    // Access the heaps field using reflection
                    Field heapsField = Level.class.getDeclaredField("heaps");
                    heapsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<Integer, Heap> heaps = (Map<Integer, Heap>) heapsField.get(Dungeon.level);

                    // Remove the heap at this location if it exists
                    if (heaps.containsKey(cellPos)) {
                        Heap heap = heaps.get(cellPos);
                        heap.destroy(); // Use destroy() method instead of clear()
                        found[0] = true;
                    }
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            if (!found[0]) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"No item found at coordinates (%d,%d)\"}", x, y));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Removed item at (%d,%d)\",\"x\":%d,\"y\":%d}",
                    x, y, x, y));
        } catch (Exception e) {
            GLog.w("Error in handleDebugRemoveItem: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugResetLevel(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // Get current depth to recreate the same level
            int currentDepth = Dungeon.depth;

            // caveman: level switch touches scene + actor state - game thread only
            final int finalDepth = currentDepth;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Create a new level of the same type
                    Position position = new Position();
                    position.levelId = String.valueOf(finalDepth);
                    Level newLevel = DungeonGenerator.createLevel(position);

                    // Collect existing mobs to transfer to the new level
                    Collection<Mob> mobs = new ArrayList<>();
                    for (Mob mob : Dungeon.level.mobs) {
                        mobs.add(mob);
                    }

                    // Switch to the new level
                    int startPos = Dungeon.hero.pos; // Keep hero at the same position
                    Dungeon.switchLevel(newLevel, startPos, mobs);
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Reset level %d\",\"level\":%d}", currentDepth, currentDepth));
        } catch (Exception e) {
            GLog.w("Error in handleDebugResetLevel: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetDungeonSeed(NanoHTTPD.IHTTPSession session) {
        try {
            // Check if game state is initialized
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Dungeon not initialized - start a game first\"}");
            }

            // Since there doesn't appear to be a seed field in the Dungeon class,
            // we'll return a message indicating this
            String jsonString = String.format("{\"seed\":%d}", System.currentTimeMillis()); // Use current time as a placeholder
            
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetDungeonSeed: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugSetDungeonSeed(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            long seed = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("seed=")) {
                        try {
                            seed = Long.parseLong(URLDecoder.decode(param.substring(5), "UTF-8")); // Remove "seed=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid seed
                        }
                    }
                }
            }

            if (seed == -1) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid seed parameter\"}");
            }

            // Note: Setting the seed directly isn't possible after game start
            // This would require restarting the game with the new seed
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                String.format("{\"error\":\"Setting seed after game start is not supported. Restart the game with the new seed.\"}"));
        } catch (Exception e) {
            GLog.w("Error in handleDebugSetDungeonSeed: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugGetTileInfo(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (x < 0 || y < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid coordinates\"}");
            }

            // Check if game state is initialized
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            // Validate coordinates are within level bounds
            if (x >= Dungeon.level.getWidth() || y >= Dungeon.level.getHeight()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Coordinates (%d,%d) are outside level bounds (width: %d, height: %d)\"}", 
                        x, y, Dungeon.level.getWidth(), Dungeon.level.getHeight()));
            }

            int cellPos = x + y * Dungeon.level.getWidth();
            
            // Create JSON response for tile info
            StringBuilder tileJson = new StringBuilder("{");
            tileJson.append(String.format(
                "\"x\":%d,\"y\":%d,\"cell_pos\":%d,\"terrain\":%d,\"passable\":%b,\"visible\":%b,\"visited\":%b,\"mapped\":%b",
                x, y, cellPos, Dungeon.level.map[cellPos], Dungeon.level.passable[cellPos], 
                Dungeon.level.visited != null && cellPos < Dungeon.level.visited.length && Dungeon.level.visited[cellPos],
                Dungeon.level.visited != null && cellPos < Dungeon.level.visited.length && Dungeon.level.visited[cellPos],
                Dungeon.level.mapped != null && cellPos < Dungeon.level.mapped.length && Dungeon.level.mapped[cellPos]
            ));
            
            // Access the heaps field using reflection
            Field heapsField = Level.class.getDeclaredField("heaps");
            heapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Heap> heaps = (Map<Integer, Heap>) heapsField.get(Dungeon.level);

            // Check for heaps (items) at this location
            if (heaps.containsKey(cellPos)) {
                Heap heap = heaps.get(cellPos);
                Item item = heap.peek();
                if (item != null) {
                    Bundle itemBundle = new Bundle();
                    item.storeInBundle(itemBundle);
                    
                    tileJson.append(String.format(
                        ",\"items\":[%s]",
                        itemBundle.serialize()
                    ));
                } else {
                    tileJson.append(",\"items\":[]");
                }
            } else {
                tileJson.append(",\"items\":[]");
            }

            // Check for chars (hero or mobs) at this location
            Char charAtPos = Actor.findChar(cellPos);
            if (charAtPos != null) {
                Bundle charBundle = new Bundle();
                charAtPos.storeInBundle(charBundle);
                
                tileJson.append(String.format(
                    ",\"character\":%s",
                    charBundle.serialize()
                ));
            } else {
                tileJson.append(",\"character\":null");
            }
            
            tileJson.append("}");
            
            String jsonString = tileJson.toString();

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetTileInfo: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugHandleCell(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (x < 0 || y < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid coordinates\"}");
            }

            // Check if game state is initialized
            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            // Validate coordinates are within level bounds
            if (x >= Dungeon.level.getWidth() || y >= Dungeon.level.getHeight() || x < 0 || y < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Coordinates (%d,%d) are outside level bounds (width: %d, height: %d)\"}",
                        x, y, Dungeon.level.getWidth(), Dungeon.level.getHeight()));
            }

            // Calculate target cell position
            final int finalCellPos = x + y * Dungeon.level.getWidth();
            final int finalX = x;
            final int finalY = y;
            
            // Use CountDownLatch to wait for action to complete on game thread
            CountDownLatch latch = new CountDownLatch(1);
            
            // Schedule the action to run on the main game thread
            GameLoop.pushUiTask(() -> {
                try {
                    // Simulate clicking on the cell (this mimics the behavior of clicking in the game)
                    // This will trigger the appropriate action based on what's at that cell
                    // Use the hero's move method to move to the cell if it's passable
                    if (Dungeon.level.passable[finalCellPos]) {
                        Dungeon.hero.move(finalCellPos - Dungeon.hero.pos);
                    } else {
                        // If not passable, check if there's a character to attack
                        Char ch = Actor.findChar(finalCellPos);
                        if (ch != null && ch instanceof Mob) {
                            Dungeon.hero.attack(ch);
                        }
                    }
                    
                    // The game will handle turn processing automatically
                } catch (Exception e) {
                    GLog.n("Error handling cell: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the action to complete (up to 5 seconds)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for cell handling to complete\"}");
            }

            // Use reflection to access the private heaps field
            Field heapsField = Level.class.getDeclaredField("heaps");
            heapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Heap> heaps = (Map<Integer, Heap>) heapsField.get(Dungeon.level);

            // Return information about what happened at the cell
            StringBuilder response = new StringBuilder("{");
            response.append(String.format("\"success\":true,\"message\":\"Handled cell (%d,%d)\",\"x\":%d,\"y\":%d,", x, y, x, y));

            // Check if there's a character at the cell (mob or hero)
            Char cellCh = Actor.findChar(finalCellPos);
            if (cellCh != null) {
                response.append(String.format("\"character\":\"%s\",", cellCh.getClass().getSimpleName()));
            }

            // Check if there's an item heap at the cell
            if (heaps.containsKey(finalCellPos)) {
                Heap heap = heaps.get(finalCellPos);
                response.append(String.format("\"item\":\"%s\",", heap.peek().getClass().getSimpleName()));
            }

            // Include terrain information
            response.append(String.format("\"terrain\":%d,\"passable\":%b}", 
                Dungeon.level.map[finalCellPos], Dungeon.level.passable[finalCellPos]));

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", response.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugHandleCell: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    public static NanoHTTPD.Response handleDebugCastSpell(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String spellName = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        spellName = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                        break;
                    }
                }
            }

            if (spellName == null || spellName.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing spell type parameter").toString());
            }

            final String finalSpellName = spellName; // Make it final for lambda access

            // Use CountDownLatch to wait for spell casting to complete
            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            
            // Schedule the spell casting on the main game thread
            GameLoop.pushUiTask(() -> {
                try {
                    // Check if the game state is initialized in the main thread
                    if (Dungeon.hero == null || Dungeon.level == null) {
                        error[0] = "Game state not initialized - start a game first";
                        GLog.n(error[0]);
                    } else {
                        Spell spell = SpellFactory.getSpellByName(finalSpellName);
                        if (spell != null) {
                            // For targeted spells, we'll use the castOnRandomTarget method which selects an appropriate target
                            // This bypasses the UI targeting and directly casts on a random valid target
                            spell.castOnRandomTarget(Dungeon.hero);
                            GLog.i("Casting spell '" + finalSpellName + "'");
                        } else {
                            error[0] = "Spell not found: " + finalSpellName;
                            GLog.n(error[0]);
                        }
                    }
                } catch (Exception e) {
                    error[0] = "Error casting spell: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the spell casting to complete (up to 5 seconds)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for spell casting to complete\"}");
            }

            Map<String, Object> response = new HashMap<>();
            if (error[0] != null) {
                response.put("success", false);
                response.put("message", error[0]);
            } else {
                response.put("success", true);
                response.put("message", "Cast spell '" + spellName + "'");
            }
            response.put("spellType", spellName);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", 
                new JSONObject(response).toString());
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Error casting spell: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugCastSpellOnMob(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String spellName = null;
            String mobType = null;
            boolean ownedOnly = false;

            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("spell=")) {
                        spellName = URLDecoder.decode(param.substring(6), "UTF-8");
                    } else if (param.startsWith("mobType=")) {
                        mobType = URLDecoder.decode(param.substring(8), "UTF-8");
                    } else if (param.equals("owned=true")) {
                        ownedOnly = true;
                    }
                }
            }

            if (spellName == null || spellName.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing spell parameter").toString());
            }
            if (mobType == null || mobType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing mobType parameter").toString());
            }

            final String finalSpellName = spellName;
            final String finalMobType = mobType;
            final boolean finalOwnedOnly = ownedOnly;

            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            final boolean[] success = new boolean[1];

            GameLoop.pushUiTask(() -> {
                try {
                    if (Dungeon.hero == null || Dungeon.level == null) {
                        error[0] = "Game state not initialized";
                    } else {
                        Spell spell = SpellFactory.getSpellByName(finalSpellName);
                        if (spell == null) {
                            error[0] = "Spell not found: " + finalSpellName;
                        } else {
                            Mob targetMob = null;
                            for (Mob mob : Dungeon.level.mobs) {
                                String entityKind = mob.getEntityKind();
                                String mobClassName = mob.getClass().getSimpleName();
                                boolean typeMatches = entityKind.equalsIgnoreCase(finalMobType)
                                    || mobClassName.equalsIgnoreCase(finalMobType)
                                    || entityKind.toLowerCase().contains(finalMobType.toLowerCase())
                                    || finalMobType.toLowerCase().contains(entityKind.toLowerCase());
                                if (typeMatches) {
                                    if (!finalOwnedOnly || mob.getOwnerId() == Dungeon.hero.getId()) {
                                        targetMob = mob;
                                        break;
                                    }
                                }
                            }
                            if (targetMob != null) {
                                spell.castOnTarget(Dungeon.hero, targetMob);
                                success[0] = true;
                                GLog.i("Casting spell '" + finalSpellName + "' on " + finalMobType);
                            } else {
                                error[0] = "No matching mob found (type=" + finalMobType + ", owned=" + finalOwnedOnly + ")";
                            }
                        }
                    }
                } catch (Exception e) {
                    error[0] = "Error: " + e.getMessage();
                } finally {
                    latch.countDown();
                }
            });

            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for spell casting\"}");
            }

            Map<String, Object> response = new HashMap<>();
            if (error[0] != null) {
                response.put("success", false);
                response.put("message", error[0]);
            } else {
                response.put("success", success[0]);
                response.put("message", "Cast spell '" + spellName + "' on " + mobType);
            }
            response.put("spellType", spellName);
            response.put("mobType", mobType);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Error casting spell on mob: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugCastSpellOnTarget(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String spellName = null;
            String targetXStr = null;
            String targetYStr = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        spellName = URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        targetXStr = URLDecoder.decode(param.substring(2), "UTF-8"); // Remove "x=" prefix
                    } else if (param.startsWith("y=")) {
                        targetYStr = URLDecoder.decode(param.substring(2), "UTF-8"); // Remove "y=" prefix
                    }
                }
            }

            if (spellName == null || spellName.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing spell type parameter").toString());
            }

            if (targetXStr == null || targetYStr == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing x or y coordinate parameters").toString());
            }

            int targetX = Integer.parseInt(targetXStr);
            int targetY = Integer.parseInt(targetYStr);

            final String finalSpellName = spellName;
            final int finalTargetX = targetX;
            final int finalTargetY = targetY;
            
            // Use CountDownLatch to wait for spell casting to complete
            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            
            // Schedule the spell casting on the main game thread
            GameLoop.pushUiTask(() -> {
                try {
                    // Check if the game state is initialized in the main thread
                    if (Dungeon.hero == null || Dungeon.level == null) {
                        error[0] = "Game state not initialized - start a game first";
                        GLog.n(error[0]);
                    } else {
                        // Validate coordinates in the main thread
                        if (finalTargetX < 0 || finalTargetX >= Dungeon.level.getWidth() || 
                            finalTargetY < 0 || finalTargetY >= Dungeon.level.getHeight()) {
                            error[0] = "Invalid coordinates: (" + finalTargetX + "," + finalTargetY + ")";
                            GLog.n(error[0]);
                        } else {
                            Spell spell = SpellFactory.getSpellByName(finalSpellName);
                            if (spell != null) {
                                int targetCell = finalTargetX + finalTargetY * Dungeon.level.getWidth();
                                Char target = null;
                                for (Mob mob : Dungeon.level.mobs) {
                                    if (mob.pos == targetCell) {
                                        target = mob;
                                        break;
                                    }
                                }
                                if (target == null && Dungeon.hero.pos == targetCell) {
                                    target = Dungeon.hero;
                                }
                                if (target != null) {
                                    spell.castOnTarget(Dungeon.hero, target);
                                    GLog.i("Casting spell '" + finalSpellName + "' on target at (" + finalTargetX + "," + finalTargetY + ")");
                                } else {
                                    error[0] = "No character found at (" + finalTargetX + "," + finalTargetY + ")";
                                    GLog.n(error[0]);
                                }
                            } else {
                                error[0] = "Spell not found: " + finalSpellName;
                                GLog.n(error[0]);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    error[0] = "Invalid coordinate format: " + e.getMessage();
                    GLog.n(error[0]);
                } catch (Exception e) {
                    error[0] = "Error casting spell on target: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the spell casting to complete (up to 5 seconds)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for spell casting to complete\"}");
            }

            Map<String, Object> response = new HashMap<>();
            if (error[0] != null) {
                response.put("success", false);
                response.put("message", error[0]);
            } else {
                response.put("success", true);
                response.put("message", "Cast spell '" + spellName + "' on target (" + targetX + "," + targetY + ")");
            }
            response.put("spellType", spellName);
            response.put("x", targetX);
            response.put("y", targetY);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", 
                new JSONObject(response).toString());
        } catch (NumberFormatException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                createErrorResponse("Invalid coordinate format: " + e.getMessage()).toString());
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Error casting spell on target: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetAvailableSpells(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String affinity = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("affinity=")) {
                        affinity = URLDecoder.decode(param.substring(9), "UTF-8"); // Remove "affinity=" prefix
                        break;
                    }
                }
            }

            List<String> spells;
            if (affinity != null && !affinity.isEmpty()) {
                // Get spells for a specific affinity
                spells = SpellFactory.getSpellsByAffinity(affinity);
            } else {
                // Get all available spells
                spells = SpellFactory.getAllSpells();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", spells.size());
            response.put("spells", spells);
            response.put("affinity", affinity != null ? affinity : "all");
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Error getting available spells: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetRecentLogs(NanoHTTPD.IHTTPSession session) {
        try {
            // Get recent log messages since the last call
            String[] recentLogs = GLog.getRecentMessagesSinceLastCall();

            Map<String, Object> response = new HashMap<>();
            response.put("count", recentLogs.length);
            response.put("logs", recentLogs);
            response.put("message", "Retrieved " + recentLogs.length + " recent log messages");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetRecentLogs: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Error getting recent logs: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetMobPositions(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            StringBuilder mobsJson = new StringBuilder("[");
            boolean first = true;

            for (Mob mob : Dungeon.level.mobs) {
                if (!first) {
                    mobsJson.append(",");
                }

                int pos = mob.getPos();
                int x = pos % Dungeon.level.getWidth();
                int y = pos / Dungeon.level.getWidth();

                mobsJson.append(String.format(
                    "{\"id\":%d,\"type\":\"%s\",\"x\":%d,\"y\":%d,\"pos\":%d,\"hp\":%d,\"ht\":%d,\"state\":\"%s\"}",
                    mob.getId(),
                    mob.getEntityKind(),
                    x, y, pos,
                    mob.hp(),
                    mob.ht(),
                    mob.getState().getClass().getSimpleName()
                ));
                first = false;
            }
            mobsJson.append("]");

            String jsonString = String.format("{\"count\":%d,\"mobs\":%s}", Dungeon.level.mobs.size(), mobsJson.toString());

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetMobPositions: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetHeroPosition(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            int pos = Dungeon.hero.getPos();
            int x = pos % Dungeon.level.getWidth();
            int y = pos / Dungeon.level.getWidth();

            String jsonString = String.format(
                "{\"x\":%d,\"y\":%d,\"pos\":%d,\"class\":\"%s\"}",
                x, y, pos, Dungeon.hero.getHeroClass().name()
            );

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetHeroPosition: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugMoveHero(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;
            int cell = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("y=")) {
                        y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("cell=")) {
                        cell = Integer.parseInt(URLDecoder.decode(param.substring(5), "UTF-8"));
                    }
                }
            }

            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            // If x,y provided, convert to cell
            if (x >= 0 && y >= 0) {
                cell = x + y * Dungeon.level.getWidth();
            }

            if (cell < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing coordinates. Provide x&y or cell parameter\"}");
            }

            if (!Dungeon.level.cellValid(cell)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Invalid cell: %d\"}", cell));
            }

            final int targetCell = cell;
            
            // Use CountDownLatch to wait for move to complete
            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            
            GameLoop.pushUiTask(() -> {
                try {
                    Dungeon.hero.nextAction(new Move(targetCell));
                    GLog.i("Moving hero to cell %d", targetCell);
                } catch (Exception e) {
                    error[0] = "Error moving hero: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the move to complete (up to 5 seconds)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for hero move to complete\"}");
            }

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            int respX = cell % Dungeon.level.getWidth();
            int respY = cell / Dungeon.level.getWidth();

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Moved to cell %d\",\"x\":%d,\"y\":%d,\"cell\":%d}",
                    cell, respX, respY, cell));
        } catch (Exception e) {
            GLog.w("Error in handleDebugMoveHero: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugHeroAttack(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;
            int cell = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("y=")) {
                        y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("cell=")) {
                        cell = Integer.parseInt(URLDecoder.decode(param.substring(5), "UTF-8"));
                    }
                }
            }

            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            // If x,y provided, convert to cell
            if (x >= 0 && y >= 0) {
                cell = x + y * Dungeon.level.getWidth();
            }

            if (cell < 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing coordinates. Provide x&y or cell parameter\"}");
            }

            // Find mob at the target cell
            Char target = Actor.findChar(cell);
            if (target == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"No character found at cell %d\"}", cell));
            }

            if (!(target instanceof Mob)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Target at cell %d is not a mob\"}", cell));
            }

            final Mob targetMob = (Mob) target;
            final int targetCell = cell;
            
            // Use CountDownLatch to wait for attack to complete
            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            
            GameLoop.pushUiTask(() -> {
                try {
                    Dungeon.hero.nextAction(new Attack(targetMob));
                    GLog.i("Hero attacking %s at cell %d", targetMob.getEntityKind(), targetCell);
                } catch (Exception e) {
                    error[0] = "Error attacking: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the attack to complete (up to 5 seconds)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for attack to complete\"}");
            }

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Attacked %s\",\"target\":\"%s\",\"cell\":%d}",
                    targetMob.getEntityKind(), targetMob.getEntityKind(), cell));
        } catch (Exception e) {
            GLog.w("Error in handleDebugHeroAttack: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugWaitTicks(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int ticks = 10;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("ticks=")) {
                        try {
                            ticks = Integer.parseInt(URLDecoder.decode(param.substring(6), "UTF-8"));
                        } catch (Exception e) {
                            // Use default value
                        }
                        break;
                    }
                }
            }

            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("No hero in game").toString());
            }

            final int finalTicks = ticks;
            
            // Use CountDownLatch to wait for tick waiting to complete
            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            
            // Schedule tick waiting
            GameLoop.pushUiTask(() -> {
                try {
                    for (int i = 0; i < finalTicks; i++) {
                        // Process one game tick
                        Dungeon.hero.spendAndNext(1f);
                    }
                    GLog.i("Waited %d ticks", finalTicks);
                } catch (Exception e) {
                    error[0] = "Error waiting ticks: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for tick waiting to complete (up to 5 seconds)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for ticks to complete\"}");
            }

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Waited %d ticks\",\"ticks\":%d}", ticks, ticks));
        } catch (Exception e) {
            GLog.w("Error in handleDebugWaitTicks: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGoToLevel(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String levelId = null;
            int entranceCell = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("id=")) {
                        levelId = URLDecoder.decode(param.substring(3), "UTF-8");
                    } else if (param.startsWith("entrance=")) {
                        entranceCell = Integer.parseInt(URLDecoder.decode(param.substring(9), "UTF-8"));
                    }
                }
            }

            if (levelId == null || levelId.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing 'id' parameter\"}");
            }

            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            // Check if level exists
            if (!DungeonGenerator.isLevelExist(levelId)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Level '%s' does not exist\"}", levelId));
            }

            // Create position, do NOT create the level here - DungeonGenerator.createLevel
            // calls Actor.clearActors() and would wipe the live game's actor registry
            // before the transition collects follower pets. The level is created by
            // the transition itself; an explicit entrance cell is applied after arrival.
            Position position = new Position();
            position.levelId = levelId;
            position.cellId = entranceCell;

            // Schedule InterlevelScene transition on GL thread and wait for level load
            InterlevelScene.scheduleAndWait(InterlevelScene.Mode.RETURN, position, "Switching to level: " + levelId);

            String levelKind = DungeonGenerator.getLevelKind(levelId);
            int depth = DungeonGenerator.getLevelDepth(levelId);

            int arrivedEntrance = entranceCell;
            if (arrivedEntrance < 0 && Dungeon.level != null) {
                arrivedEntrance = Dungeon.level.entrance;
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"levelId\":\"%s\",\"kind\":\"%s\",\"depth\":%d,\"entrance\":%d}",
                    levelId, levelKind, depth, arrivedEntrance));
        } catch (Exception e) {
            GLog.w("Error in handleDebugGoToLevel: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugListLevels(NanoHTTPD.IHTTPSession session) {
        try {
            List<String> levelIds = DungeonGenerator.getLevelsList();

            StringBuilder json = new StringBuilder("{\"count\":").append(levelIds.size()).append(",\"levels\":[");

            boolean first = true;
            for (String levelId : levelIds) {
                if (!first) {
                    json.append(",");
                }
                String kind = DungeonGenerator.getLevelKind(levelId);
                int depth = DungeonGenerator.getLevelDepth(levelId);

                json.append(String.format("{\"id\":\"%s\",\"kind\":\"%s\",\"depth\":%d}",
                    levelId, kind, depth));
                first = false;
            }

            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugListLevels: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetExits(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            String currentLevelId = DungeonGenerator.getCurrentLevelId();
            JSONArray exits = DungeonGenerator.getLevelExits(currentLevelId);

            StringBuilder json = new StringBuilder("{\"levelId\":\"").append(currentLevelId).append("\",\"exits\":[");

            for (int i = 0; i < exits.length(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                String exitId = exits.getString(i);
                String kind = DungeonGenerator.getLevelKind(exitId);
                int depth = DungeonGenerator.getLevelDepth(exitId);
                json.append(String.format("{\"id\":\"%s\",\"kind\":\"%s\",\"depth\":%d}",
                    exitId, kind, depth));
            }

            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetExits: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetEntrances(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Level not initialized - start a game first\"}");
            }

            String currentLevelId = DungeonGenerator.getCurrentLevelId();
            JSONArray entrances = DungeonGenerator.getLevelEntrances(currentLevelId);

            StringBuilder json = new StringBuilder("{\"levelId\":\"").append(currentLevelId).append("\",\"entrances\":[");

            for (int i = 0; i < entrances.length(); i++) {
                if (i > 0) {
                    json.append(",");
                }
                String entranceId = entrances.getString(i);
                String kind = DungeonGenerator.getLevelKind(entranceId);
                int depth = DungeonGenerator.getLevelDepth(entranceId);
                json.append(String.format("{\"id\":\"%s\",\"kind\":\"%s\",\"depth\":%d}",
                    entranceId, kind, depth));
            }

            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetEntrances: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugDescendTo(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String targetLevelId = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("id=")) {
                        targetLevelId = URLDecoder.decode(param.substring(3), "UTF-8");
                    }
                }
            }

            if (targetLevelId == null || targetLevelId.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing 'id' parameter\"}");
            }

            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            // Verify the target is a valid exit from current level
            String currentLevelId = DungeonGenerator.getCurrentLevelId();
            JSONArray exits = DungeonGenerator.getLevelExits(currentLevelId);
            boolean validExit = false;
            for (int i = 0; i < exits.length(); i++) {
                if (exits.getString(i).equals(targetLevelId)) {
                    validExit = true;
                    break;
                }
            }
            if (!validExit) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"'%s' is not a valid exit from current level '%s'\",\"availableExits\":%s}",
                        targetLevelId, currentLevelId, exits.toString()));
            }

            // Schedule InterlevelScene transition on GL thread and wait for level load
            final Position position = new Position();
            position.levelId = targetLevelId;
            InterlevelScene.scheduleAndWait(InterlevelScene.Mode.DESCEND, position, "Descending to: " + targetLevelId);

            String levelKind = DungeonGenerator.getLevelKind(targetLevelId);
            int depth = DungeonGenerator.getLevelDepth(targetLevelId);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"from\":\"%s\",\"to\":\"%s\",\"kind\":\"%s\",\"depth\":%d}",
                    currentLevelId, targetLevelId, levelKind, depth));
        } catch (Exception e) {
            GLog.w("Error in handleDebugDescendTo: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // caveman: full load path (loadGame from bundle + level restore) - exercises
    // the pendingFollowers roster restore, like a crash+reload
    public static NanoHTTPD.Response handleDebugReloadGame(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            String currentLevelId = DungeonGenerator.getCurrentLevelId();
            InterlevelScene.scheduleAndWait(InterlevelScene.Mode.CONTINUE, null, "Debug: reloading game");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"reloadedFrom\":\"%s\",\"nowAt\":\"%s\"}",
                    currentLevelId, DungeonGenerator.getCurrentLevelId()));
        } catch (Exception e) {
            GLog.w("Error in handleDebugReloadGame: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugAscend(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            String currentLevelId = DungeonGenerator.getCurrentLevelId();
            JSONArray entrances = DungeonGenerator.getLevelEntrances(currentLevelId);

            if (entrances.length() == 0) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"No entrance from current level '%s'\"}", currentLevelId));
            }

            if (entrances.length() > 1) {
                // Multiple entrances - return options
                StringBuilder options = new StringBuilder("[");
                for (int i = 0; i < entrances.length(); i++) {
                    if (i > 0) options.append(",");
                    String id = entrances.getString(i);
                    options.append(String.format("{\"id\":\"%s\",\"kind\":\"%s\",\"depth\":%d}",
                        id, DungeonGenerator.getLevelKind(id), DungeonGenerator.getLevelDepth(id)));
                }
                options.append("]");
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Multiple entrances available\",\"entrances\":%s}", options.toString()));
            }

            String targetLevelId = entrances.getString(0);

            // Schedule InterlevelScene transition on GL thread and wait for level load
            final Position position = new Position();
            position.levelId = targetLevelId;
            InterlevelScene.scheduleAndWait(InterlevelScene.Mode.ASCEND, position, "Ascending to: " + targetLevelId);

            String levelKind = DungeonGenerator.getLevelKind(targetLevelId);
            int depth = DungeonGenerator.getLevelDepth(targetLevelId);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"from\":\"%s\",\"to\":\"%s\",\"kind\":\"%s\",\"depth\":%d}",
                    currentLevelId, targetLevelId, levelKind, depth));
        } catch (Exception e) {
            GLog.w("Error in handleDebugAscend: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // Alchemy System Debug Endpoints

    public static NanoHTTPD.Response handleAlchemyListRecipes(NanoHTTPD.IHTTPSession session) {
        try {
            List<AlchemyRecipe> recipes = AlchemyRecipes.getAllRecipes();

            StringBuilder json = new StringBuilder("{\"count\":").append(recipes.size()).append(",\"recipes\":[");

            boolean first = true;
            for (AlchemyRecipe recipe : recipes) {
                if (!first) {
                    json.append(",");
                }

                // Build recipe JSON
                json.append("{");

                // Input items
                json.append("\"inputs\":[");
                List<InputItem> inputs = recipe.getInput();
                for (int i = 0; i < inputs.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"name\":\"%s\",\"count\":%d}", inputs.get(i).getName(), inputs.get(i).getCount()));
                }
                json.append("],");

                // Output items
                json.append("\"outputs\":[");
                List<OutputItem> outputs = recipe.getOutput();
                for (int i = 0; i < outputs.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"name\":\"%s\",\"count\":%d}", outputs.get(i).getName(), outputs.get(i).getCount()));
                }
                json.append("]");

                json.append("}");
                first = false;
            }

            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleAlchemyListRecipes: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleAlchemyGetRecipe(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            List<String> ingredientNames = new ArrayList<>();

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("ingredient=")) {
                        String ingredientName = URLDecoder.decode(param.substring(11), "UTF-8");
                        ingredientNames.add(ingredientName);
                    }
                }
            }

            if (ingredientNames.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing ingredient parameters. Use ingredient=NAME for each ingredient.\"}");
            }

            // Try to find matching recipe by ingredient names (ignoring counts)
            AlchemyRecipe matchedRecipe = null;
            for (AlchemyRecipe recipe : AlchemyRecipes.getAllRecipes()) {
                List<InputItem> recipeInputs = recipe.getInput();

                // Check if the number of ingredients matches
                if (recipeInputs.size() != ingredientNames.size()) {
                    continue;
                }

                // Check if all ingredient names match (order-independent)
                boolean namesMatch = true;
                for (InputItem recipeInput : recipeInputs) {
                    if (!ingredientNames.contains(recipeInput.getName())) {
                        namesMatch = false;
                        break;
                    }
                }

                // Also check that all requested ingredient names are in the recipe
                for (String requestedName : ingredientNames) {
                    boolean found = false;
                    for (InputItem recipeInput : recipeInputs) {
                        if (recipeInput.getName().equals(requestedName)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        namesMatch = false;
                        break;
                    }
                }

                if (namesMatch) {
                    matchedRecipe = recipe;
                    break;
                }
            }

            if (matchedRecipe == null) {
                StringBuilder ingList = new StringBuilder("[");
                for (int i = 0; i < ingredientNames.size(); i++) {
                    if (i > 0) ingList.append(",");
                    ingList.append("\"").append(ingredientNames.get(i)).append("\"");
                }
                ingList.append("]");

                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                    String.format("{\"error\":\"No recipe found for ingredients: %s\"}", ingList.toString()));
            }

            List<OutputItem> outputs = matchedRecipe.getOutput();
            List<InputItem> inputs = matchedRecipe.getInput();

            // Build response
            StringBuilder json = new StringBuilder("{\"success\":true,\"inputs\":[");
            for (int i = 0; i < inputs.size(); i++) {
                if (i > 0) json.append(",");
                json.append(String.format("{\"name\":\"%s\",\"count\":%d}", inputs.get(i).getName(), inputs.get(i).getCount()));
            }
            json.append("],\"outputs\":[");

            for (int i = 0; i < outputs.size(); i++) {
                if (i > 0) json.append(",");
                json.append(String.format("{\"name\":\"%s\",\"count\":%d}", outputs.get(i).getName(), outputs.get(i).getCount()));
            }
            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleAlchemyGetRecipe: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleAlchemyCraft(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            List<String> ingredientNames = new ArrayList<>();
            int times = 1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("ingredient=")) {
                        String ingredientName = URLDecoder.decode(param.substring(11), "UTF-8");
                        ingredientNames.add(ingredientName);
                    } else if (param.startsWith("times=")) {
                        try {
                            times = Integer.parseInt(URLDecoder.decode(param.substring(6), "UTF-8"));
                        } catch (NumberFormatException e) {
                            // Use default value
                        }
                    }
                }
            }

            if (ingredientNames.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing ingredient parameters. Use ingredient=NAME for each ingredient.\"}");
            }

            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            // Try to find matching recipe by ingredient names (ignoring counts)
            AlchemyRecipe matchedRecipe = null;
            for (AlchemyRecipe recipe : AlchemyRecipes.getAllRecipes()) {
                List<InputItem> recipeInputs = recipe.getInput();

                // Check if the number of ingredients matches
                if (recipeInputs.size() != ingredientNames.size()) {
                    continue;
                }

                // Check if all ingredient names match (order-independent)
                boolean namesMatch = true;
                for (InputItem recipeInput : recipeInputs) {
                    if (!ingredientNames.contains(recipeInput.getName())) {
                        namesMatch = false;
                        break;
                    }
                }

                // Also check that all requested ingredient names are in the recipe
                for (String requestedName : ingredientNames) {
                    boolean found = false;
                    for (InputItem recipeInput : recipeInputs) {
                        if (recipeInput.getName().equals(requestedName)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        namesMatch = false;
                        break;
                    }
                }

                if (namesMatch) {
                    matchedRecipe = recipe;
                    break;
                }
            }

            if (matchedRecipe == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                    "{\"error\":\"No recipe found for the given ingredients\"}");
            }

            List<OutputItem> outputs = matchedRecipe.getOutput();
            List<InputItem> recipeInputs = matchedRecipe.getInput();

            // caveman: inventory + heaps are game-thread state; pushUiTaskAndWait
            // runs the task inline when the loop is down (headless/test use)
            final int finalTimes = times;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    // Check if hero has required ingredients (on the game thread)
                    Map<String, Integer> heroInventory = AlchemyRecipes.buildAlchemyInventory(Dungeon.hero);

                    for (InputItem ingredient : recipeInputs) {
                        String name = ingredient.getName();
                        int required = ingredient.getCount() * finalTimes;
                        int available = heroInventory.getOrDefault(name, 0);

                        if (available < required) {
                            error[0] = String.format("Insufficient ingredients: %s (have %d, need %d)", name, available, required);
                            return;
                        }
                    }

                    // Consume ingredients
                    for (InputItem ingredient : recipeInputs) {
                        String name = ingredient.getName();
                        int toRemove = ingredient.getCount() * finalTimes;

                        // Find and remove items from inventory
                        for (Item item : Dungeon.hero.getBelongings()) {
                            if (item.getEntityKind().equals(name) && toRemove > 0) {
                                int multiplier = (item instanceof Carcass)
                                        ? ((Carcass) item).upgradeMultiplier()
                                        : 1;
                                int itemsNeeded = (int) Math.ceil((double) toRemove / multiplier);
                                int toConsume = Math.min(item.quantity(), itemsNeeded);

                                item.quantity(item.quantity() - toConsume);
                                toRemove -= toConsume * multiplier;
                                if (item.quantity() <= 0) {
                                    Dungeon.hero.getBelongings().removeItem(item);
                                }
                            }
                        }
                    }

                    // Create outputs
                    for (OutputItem output : outputs) {
                        AlchemyRecipes.EntityType entityType =
                            AlchemyRecipes.determineEntityType(output.getName());

                        if (entityType == AlchemyRecipes.EntityType.ITEM) {
                            // Create item and give to hero
                            for (int i = 0; i < output.getCount() * finalTimes; i++) {
                                Item item = ItemFactory.itemByName(output.getName());
                                if (item != null) {
                                    Dungeon.hero.getBelongings().collect(item);
                                }
                            }
                        } else if (entityType == AlchemyRecipes.EntityType.MOB) {
                            // Create mob - same flow as WndItemAlchemy: real spawnMob, not a ghost
                            for (int i = 0; i < output.getCount() * finalTimes; i++) {
                                Mob mob = MobFactory.mobByName(output.getName());
                                if (mob != null && Dungeon.level != null) {
                                    int cell = Dungeon.level.getEmptyCellNextTo(Dungeon.hero.getPos());
                                    if (Dungeon.level.cellValid(cell)) {
                                        mob.setPos(cell);
                                        mob.makePet(Dungeon.hero);
                                        Dungeon.level.spawnMob(mob, -1, Dungeon.hero.getPos());
                                    } else {
                                        Dungeon.level.animatedDrop(mob.carcass(), Dungeon.hero.getPos());
                                    }
                                }
                            }
                        }
                    }

                    GLog.i("Crafted %dx recipe", finalTimes);
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"%s\"}", error[0]));
            }

            // Build response
            StringBuilder json = new StringBuilder("{\"success\":true,\"message\":\"Crafting ");
            json.append(times).append("x recipe\",\"times\":").append(times).append(",\"outputs\":[");

            for (int i = 0; i < outputs.size(); i++) {
                if (i > 0) json.append(",");
                json.append(String.format("{\"name\":\"%s\",\"count\":%d}", outputs.get(i).getName(), outputs.get(i).getCount() * times));
            }
            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleAlchemyCraft: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleAlchemyGetInventory(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            Map<String, Integer> inventory = AlchemyRecipes.buildAlchemyInventory(Dungeon.hero);

            StringBuilder json = new StringBuilder("{\"count\":").append(inventory.size()).append(",\"inventory\":[");

            boolean first = true;
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append(String.format("{\"name\":\"%s\",\"quantity\":%d}", entry.getKey(), entry.getValue()));
                first = false;
            }

            json.append("]}");

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (Exception e) {
            GLog.w("Error in handleAlchemyGetInventory: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleAlchemyGiveItem(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String itemType = null;
            int count = 1;
            int level = 0;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        itemType = URLDecoder.decode(param.substring(5), "UTF-8");
                    } else if (param.startsWith("count=")) {
                        try {
                            count = Integer.parseInt(URLDecoder.decode(param.substring(6), "UTF-8"));
                        } catch (NumberFormatException e) {
                            // Use default value
                        }
                    } else if (param.startsWith("level=")) {
                        try {
                            level = Integer.parseInt(URLDecoder.decode(param.substring(6), "UTF-8"));
                        } catch (NumberFormatException e) {
                            // Use default value
                        }
                    }
                }
            }

            if (itemType == null || itemType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing item type parameter\"}");
            }

            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Hero not initialized - start a game first\"}");
            }

            // caveman: inventory collection touches hero state - game thread only;
            // pushUiTaskAndWait runs inline when the loop is down (headless/test use)
            final String finalItemType = itemType;
            final int finalCount = count;
            final int finalLevel = level;
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    for (int i = 0; i < finalCount; i++) {
                        Item item = ItemFactory.itemByName(finalItemType);
                        if (item != null) {
                            if (finalLevel > 0) {
                                item.upgrade(finalLevel);
                            }
                            Dungeon.hero.getBelongings().collect(item);
                        }
                    }
                    GLog.i("Gave %dx %s +%d to hero", finalCount, finalItemType, finalLevel);
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Gave %dx %s +%d to hero\",\"type\":\"%s\",\"count\":%d,\"level\":%d}",
                    count, itemType, level, itemType, count, level));
        } catch (Exception e) {
            GLog.w("Error in handleAlchemyGiveItem: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // test endpoint: /debug/item_info?type=<ItemFactory name> - inspect factory-fresh item (price drives FOR_SALE gating)
    public static NanoHTTPD.Response handleDebugItemInfo(NanoHTTPD.IHTTPSession session) {
        try {
            String itemType = null;
            String query = session.getQueryParameterString();
            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("type=")) {
                        itemType = URLDecoder.decode(param.substring(5), "UTF-8");
                    }
                }
            }

            if (itemType == null || itemType.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing item type parameter\"}");
            }

            final String finalItemType = itemType;
            final String[] error = new String[1];
            final String[] json = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    Item item = ItemFactory.itemByName(finalItemType);
                    if (item == null || !item.valid()) {
                        error[0] = "unknown item: " + finalItemType;
                        return;
                    }
                    json[0] = String.format(
                        "{\"kind\":\"%s\",\"price\":%d,\"quantity\":%d,\"stackable\":%b,\"upgradable\":%b}",
                        item.getEntityKind(), item.price(), item.quantity(), item.stackable, item.isUpgradable());
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                    String.format("{\"error\":\"%s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json[0]);
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse(e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugScreenshot(NanoHTTPD.IHTTPSession session) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_IMPLEMENTED, "application/json",
            "{\"error\":\"Screenshot not supported on this platform\"}");
    }

    public static NanoHTTPD.Response handleDebugToggleUI(NanoHTTPD.IHTTPSession session) {
        try {
            final boolean[] uiHidden = new boolean[1];
            // caveman: hideUI is read by the render loop - flip it on the game thread
            GameLoop.pushUiTaskAndWait(() -> {
                GameScene.hideUI = !GameScene.hideUI;
                uiHidden[0] = GameScene.hideUI;
            });
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"uiHidden\":%b,\"message\":\"UI is now %s\"}",
                    uiHidden[0], uiHidden[0] ? "hidden" : "visible"));
        } catch (Exception e) {
            GLog.w("Error in handleDebugToggleUI: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // caveman: UI test aid - shows a real window on the game thread and reports its
    // geometry against the WndHelper budget, so window layout can be verified
    // without tapping the actual UI. wnd=petbag|petoptions|petselect|herobag|optionstest
    public static NanoHTTPD.Response handleDebugOpenWindow(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String wnd = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("wnd=")) {
                        wnd = URLDecoder.decode(param.substring(4), "UTF-8");
                        break;
                    }
                }
            }

            if (wnd == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing wnd parameter (petbag|petoptions|petselect|herobag|optionstest)\"}");
            }

            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game state not initialized - start a game first\"}");
            }

            final String wndParam = wnd;
            final String[] result = new String[3];

            GameLoop.pushUiTaskAndWait(() -> {
                Hero hero = Dungeon.hero;
                switch (wndParam) {
                    case "petbag":
                    case "petoptions":
                    case "petselect": {
                        List<Mob> pets = PetInventoryManager.getHeroPets(hero);
                        if (pets.isEmpty()) {
                            result[0] = "ERROR: hero has no pets";
                            return;
                        }
                        Mob pet = pets.get(0);
                        Window w;
                        if (wndParam.equals("petbag")) {
                            w = new WndPetBag(hero, pet);
                        } else if (wndParam.equals("petoptions")) {
                            w = new WndPetInventoryOptions(hero, pet);
                        } else {
                            w = new WndPetSelect(hero);
                        }
                        GameScene.show(w);
                        result[0] = w.getClass().getSimpleName();
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    case "herobag": {
                        Window w = new WndBag(hero.getBelongings(), hero.getBelongings().backpack,
                            null, WndBag.Mode.ALL, null);
                        GameScene.show(w);
                        result[0] = w.getClass().getSimpleName();
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    case "optionstest": {
                        // exercises the WndOptions overflow/scroll path
                        Window w = new WndOptions("WndOptions scroll test",
                            "A deliberately long message so the panel grows past the screen height on small displays.",
                            "Option One", "Option Two", "Option Three", "Option Four",
                            "Option Five", "Option Six", "Option Seven", "Option Eight") {
                            @Override
                            public void onSelect(int index) {
                            }
                        };
                        GameScene.show(w);
                        result[0] = w.getClass().getSimpleName();
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    case "msgtest": {
                        // quest-popup shape: WndTitledMessage with long text
                        Window w = new WndTitledMessage(Icons.get(Icons.WARRIOR),
                            "Old beardy questgiver",
                            "Greetings, adventurer! I have a favour to ask of someone with your particular set of skills. " +
                            "Deep beneath the sewers lies a talisman of great importance, lost there by an ancestor of mine. " +
                            "The rats have carried it off into the darkness, and I am far too old to go crawling after it myself. " +
                            "Bring it back to me and I shall reward you handsomely - or at least tell you where the next " +
                            "dozen pages of tedious lore are hidden. Beware the goo, it bites. And should you descend further, know that the prison levels above the caves are haunted by things worse than rats: gaunt guards in rusted armour who do not sleep, and a warden whose name is spoken only in whispers. Pack antidoes, for the air itself festers down there, and whatever you do, do not drink from the red fountains however thirsty you become.");
                        GameScene.show(w);
                        result[0] = w.getClass().getSimpleName();
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    case "msgplain": {
                        Window w = new WndMessage("Something important happened here and the message drags on: " +
                            "you have found a scroll that explains, at great length, the history of the dungeon, " +
                            "the lineage of its kings, the dietary habits of its gnolls, and several paragraphs of " +
                            "foreshadowing that will surely matter later. This line exists to make the window tall.");
                        GameScene.show(w);
                        result[0] = w.getClass().getSimpleName();
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    case "infotest": {
                        // WndInfoItem/WndInfoCell shape: GenericInfo with long desc
                        Window w = new Window() {
                        };
                        GenericInfo.makeInfo(w, new ItemSprite(ItemFactory.itemByName("Sword")),
                            "Sword", 0xFFFFFF, "A rather ordinary sword of the kind that litters every dungeon floor. " +
                            "This particular specimen has seen better decades, yet it still holds an edge - barely - " +
                            "and its balance is adequate for slashing through rats, skeletons and the occasional " +
                            "unlucky mud contractor. Long description on purpose to test the scroll zone.");
                        GameScene.show(w);
                        result[0] = "GenericInfoWindow";
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    case "settings": {
                        Window w = new WndSettings();
                        GameScene.show(w);
                        result[0] = w.getClass().getSimpleName();
                        result[1] = String.valueOf(w.getWidth());
                        result[2] = String.valueOf(w.getHeight());
                        return;
                    }
                    default:
                        result[0] = "ERROR: unknown wnd " + wndParam;
                }
            });

            if (result[0] != null && result[0].startsWith("ERROR")) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"" + result[0] + "\"}");
            }

            JSONObject resp = new JSONObject();
            resp.put("success", true);
            resp.put("window", result[0]);
            resp.put("size", result[1] + "x" + result[2]);
            resp.put("budget", WndHelper.getFullscreenWidth() + "x" + WndHelper.getAlmostFullscreenHeight());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", resp.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugOpenWindow: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugRevealMap(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"No level loaded\"}");
            }

            // caveman: fog + visibility arrays are game-thread state
            final String[] error = new String[1];

            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    if (Dungeon.visible == null) {
                        Dungeon.visible = new boolean[Dungeon.level.getLength()];
                    }

                    // Reveal entire map
                    Arrays.fill(Dungeon.visible, true);
                    if (Dungeon.level.visited != null) {
                        Arrays.fill(Dungeon.level.visited, true);
                    }
                    if (Dungeon.level.mapped != null) {
                        Arrays.fill(Dungeon.level.mapped, true);
                    }

                    // Update fog of war
                    GameScene.updateFog();
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Internal error: %s\"}", error[0]));
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                "{\"success\":true,\"message\":\"Map revealed\"}");
        } catch (Exception e) {
            GLog.w("Error in handleDebugRevealMap: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugGetWarehouseRooms(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"No level loaded\"}");
            }
            List<Map<String, Integer>> warehouseRooms = new ArrayList<>();

            // Check all rooms for warehouse type
            if (Dungeon.level instanceof RegularLevel) {
                RegularLevel regularLevel =
                    (RegularLevel) Dungeon.level;

                Set<Room> levelRooms = regularLevel.getRooms();
                for (Room room : levelRooms) {
                    if (room.type == Room.Type.WAREHOUSE) {
                        Map<String, Integer> roomInfo = new HashMap<>();
                        roomInfo.put("left", room.left);
                        roomInfo.put("right", room.right);
                        roomInfo.put("top", room.top);
                        roomInfo.put("bottom", room.bottom);
                        roomInfo.put("centerX", (room.left + room.right) / 2);
                        roomInfo.put("centerY", (room.top + room.bottom) / 2);

                        // Find entrance position
                        if (room.entrance() != null) {
                            roomInfo.put("entranceX", room.entrance().x);
                            roomInfo.put("entranceY", room.entrance().y);
                        }

                        warehouseRooms.add(roomInfo);
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("warehouseRooms", warehouseRooms);
            response.put("count", warehouseRooms.size());

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetWarehouseRooms: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // ---- LLM control surface ----
    // caveman: one call per loop tick for an agent. observe = full frame,
    // hero_status = cheap poll, get_map = planning grid.

    // caveman: alive/pos/action poll - lets a driver WAIT instead of sleep()
    public static NanoHTTPD.Response handleDebugHeroStatus(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Hero not initialized - start a game first").toString());
            }

            var hero = Dungeon.hero;
            int pos = hero.getPos();
            int x = -1, y = -1;
            if (Dungeon.level != null) {
                int width = Dungeon.level.getWidth();
                x = pos % width;
                y = pos / width;
            }

            // caveman: no curAction == idle. that is the thing a driver polls for.
            var action = hero.getCurAction();
            String actionName = (action == null) ? "idle" : action.getClass().getSimpleName();

            String levelId = (Dungeon.level != null) ? DungeonGenerator.getCurrentLevelId() : "";

            String jsonString = String.format(
                "{\"alive\":%b,\"hp\":%d,\"ht\":%d,\"pos\":%d,\"x\":%d,\"y\":%d,\"action\":\"%s\",\"levelId\":\"%s\",\"depth\":%d,\"speed\":%f,\"str\":%d}",
                hero.isAlive(),
                hero.hp(),
                hero.ht(),
                pos, x, y,
                actionName,
                levelId,
                Dungeon.depth,
                hero.speed(),
                hero.effectiveSTR()
            );

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleDebugHeroStatus: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // caveman: atomic frame. agent reads this ONE response and decides.
    // visible mobs + items + stairs + hero vitals, all from the same tick.
    public static NanoHTTPD.Response handleDebugObserve(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Game state not initialized - start a game first").toString());
            }

            var hero = Dungeon.hero;
            Level level = Dungeon.level;
            int width = level.getWidth();
            int height = level.getHeight();
            int heroPos = hero.getPos();

            JSONObject root = new JSONObject();
            root.put("levelId", DungeonGenerator.getCurrentLevelId());
            root.put("depth", Dungeon.depth);
            root.put("width", width);
            root.put("height", height);

            // hero vitals + current action
            JSONObject heroJson = new JSONObject();
            heroJson.put("class", hero.className());
            heroJson.put("level", hero.lvl());
            heroJson.put("hp", hero.hp());
            heroJson.put("max_hp", hero.ht());
            heroJson.put("str", hero.effectiveSTR());
            heroJson.put("gold", hero.gold());
            var hunger = hero.hunger();
            heroJson.put("hunger", hunger.level());
            heroJson.put("starving", hero.isStarving());
            heroJson.put("x", heroPos % width);
            heroJson.put("y", heroPos / width);
            var action = hero.getCurAction();
            heroJson.put("action", (action == null) ? "idle" : action.getClass().getSimpleName());
            JSONArray buffsJson = new JSONArray();
            for (var buff : hero.buffs()) {
                JSONObject buffJson = new JSONObject();
                buffJson.put("name", buff.getClass().getSimpleName());
                buffJson.put("level", buff.level());
                buffsJson.put(buffJson);
            }
            heroJson.put("buffs", buffsJson);
            root.put("hero", heroJson);

            // visible mobs
            JSONArray mobsJson = new JSONArray();
            for (Mob mob : level.mobs) {
                int mobPos = mob.getPos();
                if (!Dungeon.visible[mobPos]) {
                    continue;
                }
                JSONObject mobJson = new JSONObject();
                mobJson.put("id", mob.getId());
                mobJson.put("type", mob.getEntityKind());
                mobJson.put("x", mobPos % width);
                mobJson.put("y", mobPos / width);
                mobJson.put("hp", mob.hp());
                mobJson.put("ht", mob.ht());
                mobJson.put("state", mob.getState().getClass().getSimpleName());
                mobJson.put("dist", level.distance(heroPos, mobPos));
                mobJson.put("owned", mob.getOwnerId() == hero.getId());
                mobJson.put("remote", isRemote(mob));
                mobsJson.put(mobJson);
            }
            root.put("mobs", mobsJson);

            // visible item heaps
            JSONArray itemsJson = new JSONArray();
            Field heapsField = Level.class.getDeclaredField("heaps");
            heapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, Heap> heaps = (Map<Integer, Heap>) heapsField.get(level);
            for (Map.Entry<Integer, Heap> entry : heaps.entrySet()) {
                int heapPos = entry.getKey();
                if (!Dungeon.visible[heapPos]) {
                    continue;
                }
                Item item = entry.getValue().peek();
                if (item == null) {
                    continue;
                }
                JSONObject itemJson = new JSONObject();
                itemJson.put("x", heapPos % width);
                itemJson.put("y", heapPos / width);
                itemJson.put("type", item.getEntityKind());
                itemJson.put("quantity", item.quantity());
                itemsJson.put(itemJson);
            }
            root.put("items", itemsJson);

            // stairs: entrance field + exit scan (exitMap has no public reader)
            JSONArray exitsJson = new JSONArray();
            for (int cell = 0; cell < level.getLength(); cell++) {
                if (level.isExit(cell)) {
                    JSONObject exitJson = new JSONObject();
                    exitJson.put("x", cell % width);
                    exitJson.put("y", cell / width);
                    exitsJson.put(exitJson);
                }
            }
            root.put("exits", exitsJson);

            int entrance = level.entrance;
            if (entrance >= 0) {
                JSONObject entranceJson = new JSONObject();
                entranceJson.put("x", entrance % width);
                entranceJson.put("y", entrance / width);
                root.put("entrance", entranceJson);
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", root.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugObserve: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // caveman: tap on a cell - the exact path a human screen tap takes
    // (Hero.handle -> CharUtils.actionForCell). empty passable cell ->
    // pathfinding walk; mob -> attack; heap -> pickup/open; stairs -> descend;
    // locked door -> unlock; object -> interact. agent does not re-derive
    // any of that. poll hero_status until action==idle to know when done.
    public static NanoHTTPD.Response handleDebugMoveTo(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int x = -1, y = -1;
            int cell = -1;
            int charId = -1;

            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("x=")) {
                        x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("y=")) {
                        y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("cell=")) {
                        cell = Integer.parseInt(URLDecoder.decode(param.substring(5), "UTF-8"));
                    } else if (param.startsWith("char=")) {
                        charId = Integer.parseInt(URLDecoder.decode(param.substring(5), "UTF-8"));
                    }
                }
            }

            if (Dungeon.hero == null || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Game not initialized - start a game first").toString());
            }

            final Char actor;
            if (charId < 0) {
                actor = Dungeon.hero;
            } else {
                Mob puppet = findMobById(charId);
                if (puppet == null) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                        createErrorResponse("No mob with id " + charId).toString());
                }
                if (!isRemote(puppet)) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        createErrorResponse("Char " + charId + " is not remote-controlled (use /debug/remote/possess)").toString());
                }
                actor = puppet;
            }

            final boolean heroPath = (actor == Dungeon.hero);
            if (heroPath ? !actor.isReady() : actor.getCurAction() != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.CONFLICT, "application/json",
                    createErrorResponse("Char is busy, poll char_status/hero_status until action==idle").toString());
            }

            if (x >= 0 && y >= 0) {
                cell = x + y * Dungeon.level.getWidth();
            }

            if (cell < 0 || !Dungeon.level.cellValid(cell)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing or invalid coordinates. Provide x&y or cell").toString());
            }

            final int targetCell = cell;

            CountDownLatch latch = new CountDownLatch(1);
            final String[] error = new String[1];
            final String[] preview = new String[1];

            GameLoop.pushUiTask(() -> {
                try {
                    // preview the resolution for the response, then tap for real.
                    // hero.handle does the same resolution internally.
                    Dungeon.level.updateFieldOfView(actor);
                    var action = CharUtils.actionForCell(actor, targetCell, Dungeon.level);
                    preview[0] = action.toString();

                    if (!action.valid()) {
                        error[0] = "No action possible at cell " + targetCell;
                        return;
                    }

                    if (!heroPath) {
                        // caveman: puppet whitelist - Descend/Ascend would transition
                        // the HERO's interlevel state, PickUp/OpenChest are
                        // hero-belongings paths. Move/Attack/Interact family only.
                        Class<?> actionClass = action.getClass();
                        boolean supported =
                            actionClass == Move.class
                            || actionClass == Attack.class
                            || actionClass == Interact.class
                            || actionClass == InteractObject.class
                            || actionClass == Unlock.class;
                        if (!supported) {
                            error[0] = "Action not supported for remote char: " + action.getClass().getSimpleName();
                            return;
                        }
                    }

                    if (heroPath) {
                        Dungeon.hero.handle(targetCell);
                    } else {
                        actor.nextAction(action);
                    }

                    // caveman: command-pump - let the world run while the char acts.
                    // turn-based clock parks at the hero, so the hero waits one
                    // tick per loop (same primitive as /debug/wait_ticks). bounded.
                    int guard = 0;
                    while (guard++ < 20 && actor.isAlive() && actor.getCurAction() != null) {
                        Hero hero = Dungeon.hero;
                        if (hero != null && hero.isAlive() && hero.getCurAction() == null && !Dungeon.realtime()) {
                            hero.spendAndNext(Actor.TICK);
                        }
                        Actor.processTurnBased(0f);
                    }
                } catch (Exception e) {
                    error[0] = "Error in move_to: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(15, TimeUnit.SECONDS)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse("Timeout waiting for move_to").toString());
            }

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            int respX = targetCell % Dungeon.level.getWidth();
            int respY = targetCell / Dungeon.level.getWidth();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cell", targetCell);
            response.put("x", respX);
            response.put("y", respY);
            response.put("resolved", preview[0]);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugMoveTo: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // caveman: whole level as grid. terrain ints per row (same codes
    // get_tile_info reports), masks as 0/1 strings, stairs marked.
    // mask=1 -> unexplored AND not visible cells come back as terrain -1.
    public static NanoHTTPD.Response handleDebugGetMap(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Level not initialized - start a game first").toString());
            }

            boolean masked = false;
            String query = session.getQueryParameterString();
            if (query != null && query.contains("mask=1")) {
                masked = true;
            }

            Level level = Dungeon.level;
            int width = level.getWidth();
            int height = level.getHeight();

            JSONArray terrainRows = new JSONArray();
            JSONArray passableRows = new JSONArray();
            JSONArray visibleRows = new JSONArray();
            JSONArray mappedRows = new JSONArray();

            for (int row = 0; row < height; row++) {
                JSONArray terrainRow = new JSONArray();
                StringBuilder passableRow = new StringBuilder(width);
                StringBuilder visibleRow = new StringBuilder(width);
                StringBuilder mappedRow = new StringBuilder(width);

                for (int col = 0; col < width; col++) {
                    int cell = row * width + col;
                    boolean known = !masked || Dungeon.visible[cell] || level.mapped[cell];
                    terrainRow.put(known ? level.map[cell] : -1);
                    passableRow.append(level.passable[cell] ? '1' : '0');
                    visibleRow.append(Dungeon.visible[cell] ? '1' : '0');
                    mappedRow.append(level.mapped[cell] ? '1' : '0');
                }

                terrainRows.put(terrainRow);
                passableRows.put(passableRow.toString());
                visibleRows.put(visibleRow.toString());
                mappedRows.put(mappedRow.toString());
            }

            JSONObject root = new JSONObject();
            root.put("levelId", DungeonGenerator.getCurrentLevelId());
            root.put("depth", Dungeon.depth);
            root.put("width", width);
            root.put("height", height);
            root.put("terrain", terrainRows);
            root.put("passable", passableRows);
            root.put("visible", visibleRows);
            root.put("mapped", mappedRows);

            // stair cells for pathing targets
            JSONArray exitsJson = new JSONArray();
            for (int cell = 0; cell < level.getLength(); cell++) {
                if (level.isExit(cell)) {
                    JSONObject exitJson = new JSONObject();
                    exitJson.put("x", cell % width);
                    exitJson.put("y", cell / width);
                    exitsJson.put(exitJson);
                }
            }
            root.put("exits", exitsJson);

            int entrance = level.entrance;
            if (entrance >= 0) {
                JSONObject entranceJson = new JSONObject();
                entranceJson.put("x", entrance % width);
                entranceJson.put("y", entrance / width);
                root.put("entrance", entranceJson);
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", root.toString());
        } catch (Exception e) {
            GLog.w("Error in handleDebugGetMap: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // ---- remote-controlled chars (see docs/superpowers/specs/2026-08-30-remote-controlled-chars-design.md) ----

    private static boolean isRemote(Mob mob) {
        return mob.isRemoteControlled();
    }

    // test endpoint: /debug/order_pet?id=<pet>&cell=<cell> - drives the real order
    // flow for a hero-owned pet (Interact enters order mode, handleCell issues it),
    // returns the resulting AI state, enemy validity and move target
    public static NanoHTTPD.Response handleDebugOrderPet(NanoHTTPD.IHTTPSession session) {
        try {
            int id = -1, cell = -1;
            String query = session.getQueryParameterString();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=")) {
                        id = Integer.parseInt(param.substring(3));
                    } else if (param.startsWith("cell=")) {
                        cell = Integer.parseInt(param.substring(5));
                    }
                }
            }

            Mob pet = findMobById(id);
            if (pet == null || cell < 0 || Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("need id & cell, game running").toString());
            }

            final Mob finalPet = pet;
            final int finalCell = cell;
            GameLoop.pushUiTaskAndWait(() -> {
                // the real production flow: tap on owned pet enters order mode, tap on target cell issues it
                new Interact(finalPet).act(Dungeon.hero);
                GameScene.handleCell(finalCell);
            });

            String state = finalPet.getState() != null ? finalPet.getState().getTag() : "none";
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"state\":\"%s\",\"enemySet\":%b,\"target\":%d}",
                    state, finalPet.getEnemy().valid(), finalPet.getTarget()));
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse(e.getMessage()).toString());
        }
    }

    // test endpoint: /debug/test_damage?id=<mob>&dmg=<n>&src=buff|srcid=<mobId> -
    // damages a mob with a non-Char source (Burning buff, like a DoT tick) or with
    // another mob as the source, returns the resulting AI state - used to check
    // whether AI states (pet orders) survive damage
    public static NanoHTTPD.Response handleDebugTestDamage(NanoHTTPD.IHTTPSession session) {
        try {
            int id = -1, dmg = 1, srcId = -1;
            boolean buffSrc = false;
            String query = session.getQueryParameterString();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=")) {
                        id = Integer.parseInt(param.substring(3));
                    } else if (param.startsWith("dmg=")) {
                        dmg = Integer.parseInt(param.substring(4));
                    } else if (param.startsWith("srcid=")) {
                        srcId = Integer.parseInt(param.substring(6));
                    } else if (param.startsWith("src=buff")) {
                        buffSrc = true;
                    }
                }
            }

            Mob victim = findMobById(id);
            if (victim == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("need id").toString());
            }

            final Mob finalVictim = victim;
            final int finalDmg = dmg;
            final int finalSrcId = srcId;
            final boolean finalBuffSrc = buffSrc;
            GameLoop.pushUiTaskAndWait(() -> {
                if (finalBuffSrc) {
                    finalVictim.damage(finalDmg, new Burning());
                } else {
                    Mob attacker = findMobById(finalSrcId);
                    if (attacker != null) {
                        finalVictim.damage(finalDmg, attacker);
                    }
                }
            });

            String state = finalVictim.getState() != null ? finalVictim.getState().getTag() : "none";
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"state\":\"%s\",\"enemySet\":%b,\"target\":%d}",
                    state, finalVictim.getEnemy().valid(), finalVictim.getTarget()));
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse(e.getMessage()).toString());
        }
    }

    // test endpoint: /debug/test_equip?id=<mob>&item=<ItemFactory name>&level=<n> -
    // force-equips an item on any char, bypassing the STR gate (same freedom the
    // pet equip window has). Negative armor level raises requiredSTR - makes the
    // wearer overloaded, useful to test encumbrance speed/evasion penalties.
    public static NanoHTTPD.Response handleDebugTestEquip(NanoHTTPD.IHTTPSession session) {
        try {
            String idParam = null, itemType = null;
            int level = 0;
            String query = session.getQueryParameterString();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=")) {
                        idParam = URLDecoder.decode(param.substring(3), "UTF-8");
                    } else if (param.startsWith("item=")) {
                        itemType = URLDecoder.decode(param.substring(5), "UTF-8");
                    } else if (param.startsWith("level=")) {
                        level = Integer.parseInt(param.substring(6));
                    }
                }
            }

            Char chr;
            if ("hero".equals(idParam)) {
                chr = Dungeon.hero;
            } else if (idParam != null) {
                chr = findMobById(Integer.parseInt(idParam));
            } else {
                chr = null;
            }

            if (chr == null || itemType == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("need id (\"hero\" or mob id) & item").toString());
            }

            final Char finalChr = chr;
            final String finalItemType = itemType;
            final int finalLevel = level;
            final String[] error = new String[1];
            final String[] equippedSlot = new String[1];
            GameLoop.pushUiTaskAndWait(() -> {
                try {
                    Item item = ItemFactory.itemByName(finalItemType);
                    item.level(finalLevel);
                    if (!(item instanceof EquipableItem)) {
                        error[0] = "not equipable: " + finalItemType;
                        return;
                    }
                    if (!finalChr.getBelongings().collect(item)) {
                        error[0] = "backpack full";
                        return;
                    }
                    Belongings.Slot slot = ((EquipableItem) item).slot(finalChr.getBelongings());
                    if (slot == Belongings.Slot.NONE) {
                        error[0] = "no slot for " + finalItemType;
                        return;
                    }
                    finalChr.getBelongings().equip((EquipableItem) item, slot);
                    equippedSlot[0] = slot.name();
                } catch (Exception e) {
                    error[0] = e.getMessage();
                }
            });

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"item\":\"%s\",\"level\":%d,\"slot\":\"%s\",\"effectiveSTR\":%d,\"speed\":%f}",
                    itemType, finalLevel, equippedSlot[0], chr.effectiveSTR(), chr.speed()));
        } catch (Exception e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse(e.getMessage()).toString());
        }
    }

    private static Mob findMobById(int id) {
        if (Dungeon.level == null) {
            return null;
        }
        for (Mob mob : Dungeon.level.mobs) {
            if (mob.getId() == id) {
                return mob;
            }
        }
        return null;
    }

    private static void applyStance(Mob mob, String stance) {
        if ("friend".equalsIgnoreCase(stance) && Dungeon.hero != null) {
            mob.makePet(Dungeon.hero);
        }
        // "foe" = untouched dungeon mob - nothing to do
    }

    // GET /debug/remote/spawn?type=Rat&x=&y=&stance=&revertAfter=
    public static NanoHTTPD.Response handleRemoteSpawn(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String type = null;
            String stance = "foe";
            int x = -1, y = -1;
            int revertAfter = 10;

            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("type=")) {
                        type = URLDecoder.decode(param.substring(5), "UTF-8");
                    } else if (param.startsWith("stance=")) {
                        stance = URLDecoder.decode(param.substring(7), "UTF-8");
                    } else if (param.startsWith("revertAfter=")) {
                        try {
                            revertAfter = Integer.parseInt(URLDecoder.decode(param.substring(12), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(URLDecoder.decode(param.substring(2), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (type == null || type.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Missing type parameter").toString());
            }
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Game state not initialized - start a game first").toString());
            }
            if ("friend".equalsIgnoreCase(stance) && Dungeon.hero == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("stance=friend needs a hero").toString());
            }

            final String finalType = type;
            final String finalStance = stance;
            final int finalX = x, finalY = y;
            final int finalRevertAfter = revertAfter;
            final int[] mobId = new int[1];
            final String[] error = new String[1];

            CountDownLatch latch = new CountDownLatch(1);
            GameLoop.pushUiTask(() -> {
                try {
                    Mob mob = MobFactory.mobByName(finalType);
                    if (mob == null) {
                        error[0] = "Unknown mob type: " + finalType;
                        return;
                    }
                    int cell = (finalX >= 0 && finalY >= 0)
                        ? finalX + finalY * Dungeon.level.getWidth()
                        : Dungeon.level.randomPassableCell();
                    mob.setPos(cell);
                    mob.remoteRevertStateTag = mob.getState().getTag();
                    Dungeon.level.spawnMob(mob);
                    applyStance(mob, finalStance);
                    mob.remoteRevertAfter = finalRevertAfter;
                    mob.remoteIdleTurns = 0;
                    mob.setState(MobAi.getStateByClass(RemoteControlled.class));
                    mobId[0] = mob.getId();
                } catch (Exception e) {
                    error[0] = "spawn failed: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse("Timeout waiting for remote spawn").toString());
            }
            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", mobId[0]);
            response.put("stance", finalStance);
            response.put("revertAfter", finalRevertAfter);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleRemoteSpawn: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // GET /debug/remote/possess?id=&stance=&revertAfter=
    public static NanoHTTPD.Response handleRemotePossess(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int id = -1;
            String stance = null;
            int revertAfter = -1;

            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=")) {
                        try {
                            id = Integer.parseInt(URLDecoder.decode(param.substring(3), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (param.startsWith("stance=")) {
                        stance = URLDecoder.decode(param.substring(7), "UTF-8");
                    } else if (param.startsWith("revertAfter=")) {
                        try {
                            revertAfter = Integer.parseInt(URLDecoder.decode(param.substring(12), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            final int finalId = id;
            final String finalStance = stance;
            final int finalRevertAfter = revertAfter;
            final String[] error = new String[1];

            CountDownLatch latch = new CountDownLatch(1);
            GameLoop.pushUiTask(() -> {
                try {
                    Mob mob = findMobById(finalId);
                    if (mob == null) {
                        error[0] = "No mob with id " + finalId;
                        return;
                    }
                    if (isRemote(mob)) {
                        return; // already remote - nothing to do
                    }
                    mob.remoteRevertStateTag = mob.getState().getTag();
                    if (finalStance != null) {
                        applyStance(mob, finalStance);
                    }
                    if (finalRevertAfter >= 0) {
                        mob.remoteRevertAfter = finalRevertAfter;
                    } else if (mob.remoteRevertAfter <= 0) {
                        mob.remoteRevertAfter = 10;
                    }
                    mob.remoteIdleTurns = 0;
                    mob.setState(MobAi.getStateByClass(RemoteControlled.class));
                } catch (Exception e) {
                    error[0] = "possess failed: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse("Timeout waiting for possess").toString());
            }
            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", finalId);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleRemotePossess: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // GET /debug/remote/release?id=
    public static NanoHTTPD.Response handleRemoteRelease(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int id = -1;
            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=")) {
                        try {
                            id = Integer.parseInt(URLDecoder.decode(param.substring(3), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            final int finalId = id;
            final String[] error = new String[1];

            CountDownLatch latch = new CountDownLatch(1);
            GameLoop.pushUiTask(() -> {
                try {
                    Mob mob = findMobById(finalId);
                    if (mob == null) {
                        error[0] = "No mob with id " + finalId;
                        return;
                    }
                    mob.revertRemoteControl();
                } catch (Exception e) {
                    error[0] = "release failed: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            if (!latch.await(5, TimeUnit.SECONDS)) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse("Timeout waiting for release").toString());
            }
            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", finalId);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleRemoteRelease: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // GET /debug/remote/list
    public static NanoHTTPD.Response handleRemoteList(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Game state not initialized - start a game first").toString());
            }

            JSONArray list = new JSONArray();
            int width = Dungeon.level.getWidth();
            for (Mob mob : Dungeon.level.mobs) {
                if (!isRemote(mob)) {
                    continue;
                }
                JSONObject entry = new JSONObject();
                entry.put("id", mob.getId());
                entry.put("type", mob.getEntityKind());
                entry.put("x", mob.getPos() % width);
                entry.put("y", mob.getPos() / width);
                entry.put("stance", mob.isPet() ? "friend" : "foe");
                list.put(entry);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", list.length());
            response.put("remote", list);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                new JSONObject(response).toString());
        } catch (Exception e) {
            GLog.w("Error in handleRemoteList: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    // GET /debug/char_status?id= - hero_status shape for any char on the level
    public static NanoHTTPD.Response handleCharStatus(NanoHTTPD.IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int id = -1;
            if (query != null && !query.isEmpty()) {
                for (String param : query.split("&")) {
                    if (param.startsWith("id=")) {
                        try {
                            id = Integer.parseInt(URLDecoder.decode(param.substring(3), "UTF-8"));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    createErrorResponse("Game state not initialized - start a game first").toString());
            }

            Mob mob = findMobById(id);
            if (mob == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json",
                    createErrorResponse("No mob with id " + id).toString());
            }

            int pos = mob.getPos();
            int width = Dungeon.level.getWidth();
            var action = mob.getCurAction();
            String actionName = (action == null) ? "idle" : action.getClass().getSimpleName();

            // one-shot: the driver learns the watchdog fired
            boolean reverted = mob.remoteReverted;
            mob.remoteReverted = false;

            String jsonString = String.format(
                "{\"alive\":%b,\"hp\":%d,\"ht\":%d,\"pos\":%d,\"x\":%d,\"y\":%d," +
                    "\"action\":\"%s\",\"levelId\":\"%s\",\"depth\":%d," +
                    "\"type\":\"%s\",\"fraction\":\"%s\",\"remote\":%b,\"reverted\":%b,\"revertAfter\":%d," +
                    "\"speed\":%f,\"str\":%d}",
                mob.isAlive(),
                mob.hp(),
                mob.ht(),
                pos, pos % width, pos / width,
                actionName,
                DungeonGenerator.getCurrentLevelId(),
                Dungeon.depth,
                mob.getEntityKind(),
                mob.fraction().name(),
                isRemote(mob),
                reverted,
                mob.remoteRevertAfter,
                mob.speed(),
                mob.effectiveSTR()
            );

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonString);
        } catch (Exception e) {
            GLog.w("Error in handleCharStatus: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }
}
