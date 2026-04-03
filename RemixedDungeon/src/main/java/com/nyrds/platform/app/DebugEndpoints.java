package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mechanics.spells.Spell;
import com.nyrds.pixeldungeon.mechanics.spells.SpellFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.pixeldungeon.utils.GameControl;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

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

            // Create the mob using the factory
            Mob mob = MobFactory.mobByName(mobType);

            // Set the mob's position
            mob.pos = x + y * Dungeon.level.getWidth();

            if (owned && Dungeon.hero != null) {
                mob.makePet(Dungeon.hero);
            }

            // Add the mob to the game
            Actor.occupyCell(mob);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Created mob '%s' at (%d,%d)\",\"mobType\":\"%s\",\"x\":%d,\"y\":%d,\"owned\":%b}",
                    mobType, x, y, mobType, x, y, owned));
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
                    String.format("{\"error\":\"Unknown hero class: %s. Valid classes: WARRIOR, MAGE, ROGUE, HUNTRESS, ELF, NECROMANCER, GNOLL, PRIEST, DOCTOR\"}", heroClass));
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

    public static NanoHTTPD.Response handleDebugHandleCell(NanoHTTPD.IHTTPSession session) {
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
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            
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
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for cell handling to complete\"}");
            }

            // Use reflection to access the private heaps field
            Field heapsField = Level.class.getDeclaredField("heaps");
            heapsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Heap> heaps = (java.util.Map<Integer, Heap>) heapsField.get(Dungeon.level);

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
                        spellName = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
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
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
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
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
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
                        spellName = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        targetXStr = java.net.URLDecoder.decode(param.substring(2), "UTF-8"); // Remove "x=" prefix
                    } else if (param.startsWith("y=")) {
                        targetYStr = java.net.URLDecoder.decode(param.substring(2), "UTF-8"); // Remove "y=" prefix
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
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
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
                                // For targeted spells, we'll use the castOnRandomTarget method which selects an appropriate target
                                // This bypasses the UI targeting and directly casts on a random valid target
                                spell.castOnRandomTarget(Dungeon.hero);
                                GLog.i("Casting spell '" + finalSpellName + "' on target (" + finalTargetX + "," + finalTargetY + ")");
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
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
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
                        affinity = java.net.URLDecoder.decode(param.substring(9), "UTF-8"); // Remove "affinity=" prefix
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
                        x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("y=")) {
                        y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("cell=")) {
                        cell = Integer.parseInt(java.net.URLDecoder.decode(param.substring(5), "UTF-8"));
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
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final String[] error = new String[1];
            
            GameLoop.pushUiTask(() -> {
                try {
                    Dungeon.hero.nextAction(new com.nyrds.pixeldungeon.ml.actions.Move(targetCell));
                    GLog.i("Moving hero to cell %d", targetCell);
                } catch (Exception e) {
                    error[0] = "Error moving hero: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the move to complete (up to 5 seconds)
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
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
                        x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("y=")) {
                        y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8"));
                    } else if (param.startsWith("cell=")) {
                        cell = Integer.parseInt(java.net.URLDecoder.decode(param.substring(5), "UTF-8"));
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
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final String[] error = new String[1];
            
            GameLoop.pushUiTask(() -> {
                try {
                    Dungeon.hero.nextAction(new com.nyrds.pixeldungeon.ml.actions.Attack(targetMob));
                    GLog.i("Hero attacking %s at cell %d", targetMob.getEntityKind(), targetCell);
                } catch (Exception e) {
                    error[0] = "Error attacking: " + e.getMessage();
                    GLog.n(error[0]);
                } finally {
                    latch.countDown();
                }
            });

            // Wait for the attack to complete (up to 5 seconds)
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
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
                            ticks = Integer.parseInt(java.net.URLDecoder.decode(param.substring(6), "UTF-8"));
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
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
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
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
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
                        levelId = java.net.URLDecoder.decode(param.substring(3), "UTF-8");
                    } else if (param.startsWith("entrance=")) {
                        entranceCell = Integer.parseInt(java.net.URLDecoder.decode(param.substring(9), "UTF-8"));
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

            // Create position and level
            Position position = new Position();
            position.levelId = levelId;

            Level newLevel = DungeonGenerator.createLevel(position);
            if (newLevel == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to create level '%s'\"}", levelId));
            }

            // Determine entrance position
            int startPos;
            if (entranceCell >= 0 && entranceCell < newLevel.getLength()) {
                startPos = entranceCell;
            } else {
                startPos = newLevel.entrance;
            }

            position.cellId = startPos;

            String levelKind = DungeonGenerator.getLevelKind(levelId);
            int depth = DungeonGenerator.getLevelDepth(levelId);

            // Build response BEFORE triggering scene change
            String responseBody = String.format("{\"success\":true,\"levelId\":\"%s\",\"kind\":\"%s\",\"depth\":%d,\"entrance\":%d}",
                levelId, levelKind, depth, startPos);

            // Schedule the InterlevelScene transition to happen AFTER the HTTP response is sent.
            // This avoids closing the connection mid-scene-change.
            final Position finalPosition = position;
            final String finalLevelId = levelId;
            GameLoop.pushUiTask(() -> {
                try {
                    InterlevelScene.returnTo = finalPosition;
                    InterlevelScene.Do(InterlevelScene.Mode.RETURN);
                    GLog.i("Switching to level: " + finalLevelId);
                } catch (Exception e) {
                    GLog.w("Error switching level: " + e.getMessage());
                }
            });

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", responseBody);
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
            org.json.JSONArray exits = DungeonGenerator.getLevelExits(currentLevelId);

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
            org.json.JSONArray entrances = DungeonGenerator.getLevelEntrances(currentLevelId);

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
                        targetLevelId = java.net.URLDecoder.decode(param.substring(3), "UTF-8");
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
            org.json.JSONArray exits = DungeonGenerator.getLevelExits(currentLevelId);
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

            // Use InterlevelScene to descend
            final Position position = new Position();
            position.levelId = targetLevelId;
            InterlevelScene.returnTo = position;
            InterlevelScene.Do(InterlevelScene.Mode.DESCEND);

            // Wait for scene switch
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}

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

    public static NanoHTTPD.Response handleDebugAscend(NanoHTTPD.IHTTPSession session) {
        try {
            if (Dungeon.level == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Game not initialized - start a game first\"}");
            }

            String currentLevelId = DungeonGenerator.getCurrentLevelId();
            org.json.JSONArray entrances = DungeonGenerator.getLevelEntrances(currentLevelId);

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

            // Use InterlevelScene to ascend
            final Position position = new Position();
            position.levelId = targetLevelId;
            InterlevelScene.returnTo = position;
            InterlevelScene.Do(InterlevelScene.Mode.ASCEND);

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
            List<com.nyrds.pixeldungeon.alchemy.AlchemyRecipe> recipes = com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.getAllRecipes();

            StringBuilder json = new StringBuilder("{\"count\":").append(recipes.size()).append(",\"recipes\":[");

            boolean first = true;
            for (com.nyrds.pixeldungeon.alchemy.AlchemyRecipe recipe : recipes) {
                if (!first) {
                    json.append(",");
                }

                // Build recipe JSON
                json.append("{");

                // Input items
                json.append("\"inputs\":[");
                List<com.nyrds.pixeldungeon.alchemy.InputItem> inputs = recipe.getInput();
                for (int i = 0; i < inputs.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(String.format("{\"name\":\"%s\",\"count\":%d}", inputs.get(i).getName(), inputs.get(i).getCount()));
                }
                json.append("],");

                // Output items
                json.append("\"outputs\":[");
                List<com.nyrds.pixeldungeon.alchemy.OutputItem> outputs = recipe.getOutput();
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
                        String ingredientName = java.net.URLDecoder.decode(param.substring(11), "UTF-8");
                        ingredientNames.add(ingredientName);
                    }
                }
            }

            if (ingredientNames.isEmpty()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing ingredient parameters. Use ingredient=NAME for each ingredient.\"}");
            }

            // Try to find matching recipe by ingredient names (ignoring counts)
            com.nyrds.pixeldungeon.alchemy.AlchemyRecipe matchedRecipe = null;
            for (com.nyrds.pixeldungeon.alchemy.AlchemyRecipe recipe : com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.getAllRecipes()) {
                List<com.nyrds.pixeldungeon.alchemy.InputItem> recipeInputs = recipe.getInput();

                // Check if the number of ingredients matches
                if (recipeInputs.size() != ingredientNames.size()) {
                    continue;
                }

                // Check if all ingredient names match (order-independent)
                boolean namesMatch = true;
                for (com.nyrds.pixeldungeon.alchemy.InputItem recipeInput : recipeInputs) {
                    if (!ingredientNames.contains(recipeInput.getName())) {
                        namesMatch = false;
                        break;
                    }
                }

                // Also check that all requested ingredient names are in the recipe
                for (String requestedName : ingredientNames) {
                    boolean found = false;
                    for (com.nyrds.pixeldungeon.alchemy.InputItem recipeInput : recipeInputs) {
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

            List<com.nyrds.pixeldungeon.alchemy.OutputItem> outputs = matchedRecipe.getOutput();
            List<com.nyrds.pixeldungeon.alchemy.InputItem> inputs = matchedRecipe.getInput();

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
                        String ingredientName = java.net.URLDecoder.decode(param.substring(11), "UTF-8");
                        ingredientNames.add(ingredientName);
                    } else if (param.startsWith("times=")) {
                        try {
                            times = Integer.parseInt(java.net.URLDecoder.decode(param.substring(6), "UTF-8"));
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
            com.nyrds.pixeldungeon.alchemy.AlchemyRecipe matchedRecipe = null;
            for (com.nyrds.pixeldungeon.alchemy.AlchemyRecipe recipe : com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.getAllRecipes()) {
                List<com.nyrds.pixeldungeon.alchemy.InputItem> recipeInputs = recipe.getInput();

                // Check if the number of ingredients matches
                if (recipeInputs.size() != ingredientNames.size()) {
                    continue;
                }

                // Check if all ingredient names match (order-independent)
                boolean namesMatch = true;
                for (com.nyrds.pixeldungeon.alchemy.InputItem recipeInput : recipeInputs) {
                    if (!ingredientNames.contains(recipeInput.getName())) {
                        namesMatch = false;
                        break;
                    }
                }

                // Also check that all requested ingredient names are in the recipe
                for (String requestedName : ingredientNames) {
                    boolean found = false;
                    for (com.nyrds.pixeldungeon.alchemy.InputItem recipeInput : recipeInputs) {
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

            List<com.nyrds.pixeldungeon.alchemy.OutputItem> outputs = matchedRecipe.getOutput();
            List<com.nyrds.pixeldungeon.alchemy.InputItem> recipeInputs = matchedRecipe.getInput();

            // Check if hero has required ingredients
            java.util.Map<String, Integer> heroInventory = com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.buildAlchemyInventory(Dungeon.hero);

            for (com.nyrds.pixeldungeon.alchemy.InputItem ingredient : recipeInputs) {
                String name = ingredient.getName();
                int required = ingredient.getCount() * times;
                int available = heroInventory.getOrDefault(name, 0);

                if (available < required) {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Insufficient ingredients: %s (have %d, need %d)\"}", name, available, required));
                }
            }

            // Execute crafting - do it directly (not via pushUiTask)
            // because the game loop may not be running in headless mode
            try {
                // Consume ingredients
                for (com.nyrds.pixeldungeon.alchemy.InputItem ingredient : recipeInputs) {
                    String name = ingredient.getName();
                    int toRemove = ingredient.getCount() * times;

                    // Find and remove items from inventory
                    for (Item item : Dungeon.hero.getBelongings()) {
                        if (item.getEntityKind().equals(name)) {
                            int removed = Math.min(item.quantity(), toRemove);
                            item.quantity(item.quantity() - removed);
                            toRemove -= removed;
                            if (item.quantity() <= 0) {
                                item.detach(Dungeon.hero.getBelongings().backpack);
                            }
                            if (toRemove <= 0) break;
                        }
                    }
                }

                // Create outputs
                for (com.nyrds.pixeldungeon.alchemy.OutputItem output : outputs) {
                    com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.EntityType entityType =
                        com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.determineEntityType(output.getName());

                    if (entityType == com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.EntityType.ITEM) {
                        // Create item and give to hero
                        for (int i = 0; i < output.getCount() * times; i++) {
                            Item item = ItemFactory.itemByName(output.getName());
                            if (item != null) {
                                Dungeon.hero.getBelongings().collect(item);
                            }
                        }
                    } else if (entityType == com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.EntityType.MOB) {
                        // Create mob
                        for (int i = 0; i < output.getCount() * times; i++) {
                            Mob mob = MobFactory.mobByName(output.getName());
                            if (mob != null && Dungeon.level != null) {
                                int cell = Dungeon.level.randomPassableCell();
                                mob.pos = cell;
                                mob.makePet(Dungeon.hero);
                                Actor.occupyCell(mob);
                            }
                        }
                    }
                }

                GLog.i("Crafted %dx recipe", times);
            } catch (Exception e) {
                GLog.n("Error crafting: %s", e.getMessage());
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

            java.util.Map<String, Integer> inventory = com.nyrds.pixeldungeon.alchemy.AlchemyRecipes.buildAlchemyInventory(Dungeon.hero);

            StringBuilder json = new StringBuilder("{\"count\":").append(inventory.size()).append(",\"inventory\":[");

            boolean first = true;
            for (java.util.Map.Entry<String, Integer> entry : inventory.entrySet()) {
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

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        itemType = java.net.URLDecoder.decode(param.substring(5), "UTF-8");
                    } else if (param.startsWith("count=")) {
                        try {
                            count = Integer.parseInt(java.net.URLDecoder.decode(param.substring(6), "UTF-8"));
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

            // Give items to hero - do it directly (not via pushUiTask)
            // because the game loop may not be running in headless mode
            for (int i = 0; i < count; i++) {
                Item item = ItemFactory.itemByName(itemType);
                if (item != null) {
                    Dungeon.hero.getBelongings().collect(item);
                }
            }
            GLog.i("Gave %dx %s to hero", count, itemType);

            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                String.format("{\"success\":true,\"message\":\"Gave %dx %s to hero\",\"type\":\"%s\",\"count\":%d}",
                    count, itemType, itemType, count));
        } catch (Exception e) {
            GLog.w("Error in handleAlchemyGiveItem: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }

    public static NanoHTTPD.Response handleDebugScreenshot(NanoHTTPD.IHTTPSession session) {
        try {
            if (Gdx.graphics == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Graphics not initialized - start a game first\"}");
            }

            // Use CountDownLatch to wait for screenshot to be captured on game thread
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final String[] error = new String[1];
            final String[] screenshotPath = new String[1];

            // Schedule screenshot capture on game thread
            GameLoop.pushUiTask(() -> {
                try {
                    int width = Gdx.graphics.getWidth();
                    int height = Gdx.graphics.getHeight();

                    Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);

                    if (pixmap == null) {
                        error[0] = "Failed to capture screenshot";
                        latch.countDown();
                        return;
                    }

                    // Save to temporary file
                    String path = "screenshot_" + System.currentTimeMillis() + ".png";
                    com.badlogic.gdx.files.FileHandle tempFile = Gdx.files.local(path);
                    com.badlogic.gdx.graphics.PixmapIO.writePNG(tempFile, pixmap);
                    
                    screenshotPath[0] = path;
                    
                    // Cleanup
                    pixmap.dispose();
                    
                    GLog.i("Screenshot saved to: " + path);
                } catch (Exception e) {
                    error[0] = "Error capturing screenshot: " + e.getMessage();
                    GLog.w("Error capturing screenshot: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            // Wait for screenshot to be captured (up to 5 seconds)
            boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);

            if (!completed) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Timeout waiting for screenshot\"}");
            }

            if (error[0] != null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse(error[0]).toString());
            }

            if (screenshotPath[0] == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                    createErrorResponse("Screenshot path not set").toString());
            }

            // Read the screenshot file
            com.badlogic.gdx.files.FileHandle tempFile = Gdx.files.local(screenshotPath[0]);
            byte[] screenshotBytes = tempFile.readBytes();

            // Delete the temporary file
            tempFile.delete();

            // Return PNG as response
            return NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "image/png",
                new java.io.ByteArrayInputStream(screenshotBytes),
                screenshotBytes.length
            );
        } catch (Exception e) {
            GLog.w("Error in handleDebugScreenshot: " + e.getMessage());
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json",
                createErrorResponse("Internal error: " + e.getMessage()).toString());
        }
    }
}