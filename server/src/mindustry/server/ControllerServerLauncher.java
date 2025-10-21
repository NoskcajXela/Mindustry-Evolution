package mindustry.server;

import arc.*;
import arc.backend.headless.*;
import arc.util.*;
import mindustry.*;
import mindustry.api.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.net.*;

import static mindustry.Vars.*;

/**
 * Enhanced ServerLauncher with Controller API integration.
 * Provides both traditional server functionality and headless Controller API access.
 */
public class ControllerServerLauncher implements ApplicationListener {
    
    static String[] args;
    private ControllerServerControl serverControl;
    private boolean enableControllerAPI = false;
    
    public static void main(String[] args) {
        // Check for Controller API flag
        boolean enableAPI = false;
        for (String arg : args) {
            if (arg.equals("--enable-api") || arg.equals("-api")) {
                enableAPI = true;
                break;
            }
        }
        
        try {
            ControllerServerLauncher.args = args;
            Vars.platform = new Platform(){};
            Vars.net = new Net(platform.getNet());

            logger = (level1, text) -> {
                String result = "[" + dateTime.format(java.time.LocalDateTime.now()) + "] " + 
                              format(tags[level1.ordinal()] + " " + text + "&fr");
                System.out.println(result);
            };
            
            var launcher = new ControllerServerLauncher();
            launcher.enableControllerAPI = enableAPI;
            
            new HeadlessApplication(launcher, throwable -> CrashHandler.handle(throwable, f -> {}));
        } catch (Throwable t) {
            CrashHandler.handle(t, f -> {});
        }
    }
    
    @Override
    public void init() {
        Log.info("Starting Mindustry server with Controller API support...");
        
        try {
            // Basic server initialization (same as original ServerLauncher)
            Core.settings.setDataDirectory(Core.files.local("config/"));
            
            headless = true;
            net = new Net(platform.getNet());
            tree = new FileTree();
            Vars.init();
            
            content.createBaseContent();
            add(logic = new Logic());
            add(netServer = new NetServer());
            
            mods.loadScripts();
            content.createModContent();
            content.init();
            mods.eachClass(Mod::init);
            
            // Handle content errors
            if (mods.hasContentErrors()) {
                Log.warn("Some mod content has errors and may not work correctly.");
                for (LoadedMod mod : mods.list()) {
                    if (mod.hasContentErrors()) {
                        Log.warn("Mod '@' has content errors:", mod.name);
                        for (Content cont : mod.erroredContent) {
                            Log.warn("  - @: @", cont.name, cont.error != null ? cont.error.getMessage() : "Unknown error");
                        }
                    }
                }
            }
            
            // Initialize enhanced server control
            serverControl = new ControllerServerControl();
            
            // Initialize Controller API if requested
            if (enableControllerAPI) {
                Log.info("Initializing Controller API...");
                serverControl.initializeControllerAPI();
                
                if (serverControl.isAPIEnabled()) {
                    Log.info("Controller API is now available");
                    Log.info("Use 'api status' to check API status");
                    Log.info("Use 'help' to see all available commands");
                } else {
                    Log.err("Failed to initialize Controller API");
                }
            }
            
            // Start command handling thread
            Thread thread = new Thread(serverControl.serverInput, "Server Controls");
            thread.setDaemon(true);
            thread.start();
            
            if (Version.build == -1) {
                Log.warn("BE ADVISED: You are running an unofficial build of Mindustry.");
                Log.warn("This server may be unstable and may not function correctly.");
                Log.warn("Connections to official servers may not work.");
                Log.warn("You are on your own.");
            }
            
            // Log startup completion
            Log.info("Mindustry server initialized.");
            if (enableControllerAPI) {
                Log.info("Controller API enabled. Server can be controlled programmatically.");
            }
            Log.info("Type 'help' for command usage.");
            
            Events.fire(new ServerLoadEvent());
            
        } catch (Exception e) {
            Log.err("Failed to initialize server", e);
            throw new RuntimeException("Server initialization failed", e);
        }
    }
    
    @Override
    public void update() {
        if (serverControl != null) {
            serverControl.update();
        }
    }
    
    @Override
    public void dispose() {
        Log.info("Shutting down server...");
        
        if (serverControl != null) {
            serverControl.dispose();
        }
        
        Log.info("Server shutdown complete.");
    }
    
    /** Get the Controller API if enabled */
    public ControllerAPI getControllerAPI() {
        if (serverControl != null && serverControl.isAPIEnabled()) {
            return serverControl.getControllerAPI();
        }
        throw new IllegalStateException("Controller API not available. Start server with --enable-api flag.");
    }
    
    /** Check if Controller API is available */
    public boolean hasControllerAPI() {
        return serverControl != null && serverControl.isAPIEnabled();
    }
    
    // === Static utility methods for external access ===
    
    /** Get the current server instance */
    public static ControllerServerLauncher getInstance() {
        // This would need proper singleton implementation
        return null;
    }
    
    /** Start a server with Controller API in a separate thread */
    public static Thread startServerWithAPI(String... serverArgs) {
        // Add API flag to arguments
        String[] newArgs = new String[serverArgs.length + 1];
        System.arraycopy(serverArgs, 0, newArgs, 0, serverArgs.length);
        newArgs[serverArgs.length] = "--enable-api";
        
        Thread serverThread = new Thread(() -> main(newArgs), "MindustryServer");
        serverThread.setDaemon(false); // Keep JVM alive
        serverThread.start();
        
        return serverThread;
    }
}
