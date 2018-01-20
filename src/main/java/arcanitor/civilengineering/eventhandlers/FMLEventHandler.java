package arcanitor.civilengineering.eventhandlers;


import arcanitor.civilengineering.CivilEngineering;
import arcanitor.civilengineering.Config;
import arcanitor.civilengineering.command.BridgeCommand;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import java.io.File;

public class FMLEventHandler {
    public static Configuration config;

    public static void preInit(FMLPreInitializationEvent event) {
        CivilEngineering.logger.info("Reading bridge blueprints...");
        File directory = event.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "CivilEngineering.cfg"));
        Config.readConfig();
    }

    public static void postInit(FMLPostInitializationEvent event) {
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new BridgeCommand());
        CivilEngineering.incomingMessageThread.start();
    }

    public static void serverStopping(FMLServerStoppingEvent event) {
        CivilEngineering.incomingMessageThread.interrupt();
    }
}
