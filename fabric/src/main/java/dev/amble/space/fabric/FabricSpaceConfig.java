package dev.amble.space.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.amble.space.api.SpaceAPI;
import dev.amble.space.api.mod.SpaceConfig;
import dev.amble.space.xplat.IXplatAbstractions;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.resources.ResourceLocation;

@Config(name = SpaceAPI.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/calcite.png")
public class FabricSpaceConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject
    public final Common common = new Common();

    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public final Client client = new Client();

    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public final Server server = new Server();

    public static FabricSpaceConfig setup() {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

        AutoConfig.register(FabricSpaceConfig.class, PartitioningSerializer.wrap((cfg, clazz) ->
            new GsonConfigSerializer<>(cfg, clazz, gson)));

        FabricSpaceConfig instance = AutoConfig.getConfigHolder(FabricSpaceConfig.class).getConfig();

        SpaceConfig.setCommon(instance.common);

        if (IXplatAbstractions.Companion.getINSTANCE().isPhysicalClient()) {
            SpaceConfig.setClient(instance.client);
        }

        SpaceConfig.setServer(instance.server);
        return instance;
    }

    @Config(name = "common")
    public static final class Common implements SpaceConfig.CommonConfigAccess, ConfigData {

        @Override
        public void validatePostLoad() {
        }
    }

    @Config(name = "client")
    public static final class Client implements SpaceConfig.ClientConfigAccess, ConfigData {

        @Override
        public void validatePostLoad() {
        }
    }

    @Config(name = "server")
    public static final class Server implements SpaceConfig.ServerConfigAccess, ConfigData {

        @Override
        public void validatePostLoad() {
        }
    }
}