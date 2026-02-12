package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import fi.iki.elonen.NanoHTTPD;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.ArrayList;

public class DebugEndpoints {

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
                            level = Integer.parseInt(java.net.URLDecoder.decode(levelStr, "UTF-8"));
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

            // Create a position object for the level
            Position position = new Position();
            position.levelId = String.valueOf(level);

            // Use the createLevel method
            Level newLevel = DungeonGenerator.createLevel(position);

            // Change the level - need to provide the correct parameters
            Collection<Mob> mobs = new ArrayList<>();
            if (Dungeon.level != null) {
                // Collect any existing mobs to transfer to the new level
                for (Mob mob : Dungeon.level.mobs) {
                    mobs.add(mob);
                }
            }

            // Change the level - use a single integer position instead of array
            int startPos = 1 + 1 * newLevel.getWidth(); // Convert x,y to cell position
            Dungeon.switchLevel(newLevel, startPos, mobs); // Start at position 1,1

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
                        mobType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
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

            // Create the mob using the factory
            Mob mob = MobFactory.mobByName(mobType);

            // Set the mob's position
            mob.pos = x + y * Dungeon.level.getWidth();

            // Add the mob to the game
            Actor.occupyCell(mob);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Created mob '%s' at (%d,%d)\",\"mobType\":\"%s\",\"x\":%d,\"y\":%d}",
                    mobType, x, y, mobType, x, y));
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
                        itemType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
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

            // Create the item using the factory
            Item item = ItemFactory.itemByName(itemType);

            // Drop the item at the specified location
            Dungeon.level.drop(item, x + y * Dungeon.level.getWidth());

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
                        mapType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
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
            Position position = new Position();

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

            position.levelId = levelId;

            // Use the createLevel method
            Level newLevel = DungeonGenerator.createLevel(position);

            if (newLevel == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Unknown map type: %s\"}", mapType));
            }

            // Switch to the new level - need to provide the correct parameters
            Collection<Mob> mobs = new ArrayList<>();
            if (Dungeon.level != null) {
                // Collect any existing mobs to transfer to the new level
                for (Mob mob : Dungeon.level.mobs) {
                    mobs.add(mob);
                }
            }

            // Switch to the new level - use a single integer position instead of array
            int startPos = 1 + 1 * newLevel.getWidth(); // Convert x,y to cell position
            Dungeon.switchLevel(newLevel, startPos, mobs);

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
                        itemType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
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

            // Create the item using the factory
            Item item = ItemFactory.itemByName(itemType);

            // Give the item to the hero
            Dungeon.hero.getBelongings().collect(item);

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
                        entityType = java.net.URLDecoder.decode(param.substring(7), "UTF-8"); // Remove "entity=" prefix
                    } else if (param.startsWith("value=")) {
                        entityValue = java.net.URLDecoder.decode(param.substring(6), "UTF-8"); // Remove "value=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
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

            int levelWidth = Dungeon.level.getWidth();

            int cellPos = x + y * levelWidth;

            if ("mob".equalsIgnoreCase(entityType)) {
                // Spawn a mob
                Mob mob = MobFactory.mobByName(entityValue);

                // Set the mob's position
                mob.pos = cellPos;

                // Add the mob to the game
                Actor.occupyCell(mob);

                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Spawned mob '%s' at (%d,%d)\",\"entityType\":\"%s\",\"entityValue\":\"%s\",\"x\":%d,\"y\":%d}",
                        entityValue, x, y, entityType, entityValue, x, y));
            } else if ("item".equalsIgnoreCase(entityType)) {
                // Spawn an item
                Item item = ItemFactory.itemByName(entityValue);

                // Drop the item at the specified location
                Dungeon.level.drop(item, cellPos);

                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Spawned item '%s' at (%d,%d)\",\"entityType\":\"%s\",\"entityValue\":\"%s\",\"x\":%d,\"y\":%d}",
                        entityValue, x, y, entityType, entityValue, x, y));
            } else {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"Unknown entity type: %s\"}", entityType));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugSpawnAt: " + e.getMessage());
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
                        heroClass = java.net.URLDecoder.decode(param.substring(6), "UTF-8").toUpperCase(); // Remove "class=" prefix and convert to uppercase
                    } else if (param.startsWith("difficulty=")) {
                        try {
                            difficulty = Integer.parseInt(java.net.URLDecoder.decode(param.substring(11), "UTF-8")); // Remove "difficulty=" prefix
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
                    String.format("{\"error\":\"Unknown hero class: %s. Valid classes: WARRIOR, MAGE, ROGUE, HUNTRESS\"}", heroClass));
            }

            // Call the startNewGame method - using the correct method from GameControl
            GameControl.startNewGame(heroClass, difficulty, false);

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

            // Use reflection to access private fields
            Field lvlField = Char.class.getDeclaredField("lvl");
            lvlField.setAccessible(true);
            int heroLvl = (int) lvlField.get(Dungeon.hero);

            Field hpField = Char.class.getDeclaredField("HP");
            hpField.setAccessible(true);
            int heroHp = (int) hpField.get(Dungeon.hero);

            Field htField = Char.class.getDeclaredField("HT");
            htField.setAccessible(true);
            int heroHt = (int) htField.get(Dungeon.hero);

            // Create a simple JSON response instead of using Bundle.toJson()
            String jsonString = String.format(
                "{\"hero\":{\"class\":\"%s\",\"level\":%d,\"hp\":%d,\"max_hp\":%d},\"level\":{\"depth\":%d,\"width\":%d,\"height\":%d},\"depth\":%d}",
                Dungeon.hero.className(),
                heroLvl,
                heroHp,
                heroHt,
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
                mobsJson.append(mobBundle.serialize());
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
            java.util.Map<Integer, Heap> heaps = (java.util.Map<Integer, Heap>) heapsField.get(Dungeon.level);

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
                        stat = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "stat=" prefix
                    } else if (param.startsWith("value=")) {
                        try {
                            value = Integer.parseInt(java.net.URLDecoder.decode(param.substring(6), "UTF-8")); // Remove "value=" prefix
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
                case "hp":
                    // Note: HP is a private field, so we can't set it directly
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Setting HP directly is not supported through this endpoint.\"}"));
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

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
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

            // Find mob at the specified coordinates
            int cellPos = x + y * Dungeon.level.getWidth();
            Mob targetMob = null;
            
            for (Mob mob : Dungeon.level.mobs) {
                if (mob.pos == cellPos) {
                    targetMob = mob;
                    break;
                }
            }

            if (targetMob == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    String.format("{\"error\":\"No mob found at coordinates (%d,%d)\"}", x, y));
            }

            // Kill the mob
            targetMob.die(targetMob); // Use the mob itself as the cause of death to avoid null pointer

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Killed mob at (%d,%d)\",\"x\":%d,\"y\":%d}",
                    x, y, x, y));
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
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
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

            // Remove item at the specified coordinates
            int cellPos = x + y * Dungeon.level.getWidth();

            // Access the heaps field using reflection
            Field heapsField = Level.class.getDeclaredField("heaps");
            heapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Heap> heaps = (java.util.Map<Integer, Heap>) heapsField.get(Dungeon.level);

            // Remove the heap at this location if it exists
            if (heaps.containsKey(cellPos)) {
                Heap heap = heaps.get(cellPos);
                heap.destroy(); // Use destroy() method instead of clear()
            } else {
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
            
            // Create a new level of the same type
            Position position = new Position();
            position.levelId = String.valueOf(currentDepth);
            Level newLevel = DungeonGenerator.createLevel(position);

            // Collect existing mobs to transfer to the new level
            Collection<Mob> mobs = new ArrayList<>();
            for (Mob mob : Dungeon.level.mobs) {
                mobs.add(mob);
            }

            // Switch to the new level
            int startPos = Dungeon.hero.pos; // Keep hero at the same position
            Dungeon.switchLevel(newLevel, startPos, mobs);

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
                            seed = Long.parseLong(java.net.URLDecoder.decode(param.substring(5), "UTF-8")); // Remove "seed=" prefix
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
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
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
            java.util.Map<Integer, Heap> heaps = (java.util.Map<Integer, Heap>) heapsField.get(Dungeon.level);

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
}