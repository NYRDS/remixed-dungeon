
        package com.nyrds.pixeldungeon.ml;

        public class BuildConfig {
            public static boolean DEBUG = false;
            public static final String SAVES_PATH = "./saves/";
            public static final String FLAVOR_platform = "desktop";
            public static final String FLAVOR_market = "vkplay";
            public static final String VERSION_NAME = "32.3.alpha.19";
            public static final int VERSION_CODE = 1257;
            
            public static void init(String[] args) {
                // Check command line arguments for debug flags
                if (args != null) {
                    for (String arg : args) {
                        if (arg != null && (arg.equalsIgnoreCase("--debug") || arg.equalsIgnoreCase("-debug") || 
                                           arg.equalsIgnoreCase("--debug=true") || arg.equalsIgnoreCase("-Ddebug=true"))) {
                            DEBUG = true;
                            return;
                        }
                        if (arg != null && (arg.equalsIgnoreCase("--debug=false") || arg.equalsIgnoreCase("-Ddebug=false"))) {
                            DEBUG = false;
                            return;
                        }
                    }
                }
                
                // Check system properties as fallback
                String debugProp = System.getProperty("debug");
                if (debugProp != null) {
                    DEBUG = Boolean.parseBoolean(debugProp);
                }
            }
        }
        