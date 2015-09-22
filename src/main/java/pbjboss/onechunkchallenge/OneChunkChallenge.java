package pbjboss.onechunkchallenge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(modid = OneChunkChallenge.MODID, version = OneChunkChallenge.VERSION, name = OneChunkChallenge.NAME)
public class OneChunkChallenge
{
    public static final String MODID = "onechunkchallenge";
    public static final String VERSION = "${version}";
    public static final String NAME = "One Chunk Challenge";
    public static Configuration config;
    public static int[] worldWhitelist;
    File configFile;

    @Mod.Instance
    public static OneChunkChallenge instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configFile = event.getSuggestedConfigurationFile();
        config = new Configuration(configFile);
        config.load();
        loadWorldWhitelist();
        config.save();

        MinecraftForge.EVENT_BUS.register(instance);
        FMLCommonHandler.instance().bus().register(instance);
    }

    @SubscribeEvent
    public void worldLoadEvent(WorldEvent.Load event)
    {
        if (event.world.isRemote || !isWhitelistedWorld(event.world))
            return;

        int spawnChunkX = getChunkForPos(event.world.getSpawnPoint().posX);
        int spawnChunkZ = getChunkForPos(event.world.getSpawnPoint().posZ);
    }

    @SubscribeEvent
    public void playerRespawnEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote)
            return;

        //TODO
//        if (!isInsideSpawnChunks(player.playerLocation.posX, player.playerLocation.posZ, player.worldObj))
//        {
//            FMLLog.warning("Invalid spawn! Moving player");
//            ChunkCoordinates spawnCoords = player.worldObj.getSpawnPoint();
//            ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(spawnCoords.posX + 0.5, spawnCoords.posY, spawnCoords.posZ + 0.5, event.player.cameraYaw, ((EntityPlayerMP) event.player).cameraPitch);
//        }
    }

    @SubscribeEvent
    public void blockInteractEvent(PlayerInteractEvent event)
    {
        if (event.world.isRemote || !isWhitelistedWorld(event.world) || event.entityPlayer.capabilities.isCreativeMode)
            return;

        if (!isInsideSpawnChunks(event.x, event.z, event.world))
        {
            if (event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK))
            {
                event.setCanceled(true);
            }
        }
    }

    private boolean isWhitelistedWorld(World world)
    {
        int dimId = world.provider.dimensionId;
        for (int aWorldWhitelist : worldWhitelist)
            if (aWorldWhitelist == dimId)
                return true;

        return false;
    }

    private boolean isInsideSpawnChunks(int x, int z, World world)
    {
        int spawnChunkX = getChunkForPos(world.getSpawnPoint().posX);
        int spawnChunkZ = getChunkForPos(world.getSpawnPoint().posZ);
        int blockChunkX = getChunkForPos(x);
        int blockChunkZ = getChunkForPos(z);

        FMLLog.info(String.format("sx: %s, sz: %s, x: %s, z: %s", spawnChunkX, spawnChunkZ, blockChunkX, blockChunkZ));

        return spawnChunkX == blockChunkX && spawnChunkZ == blockChunkZ;
    }

    private int getChunkForPos(int pos)
    {
        return pos >= 0 ? (pos - (pos % 16)) / 16 : ((pos + (Math.abs(pos) % 16)) / 16) - 1;
    }

    public static void loadWorldWhitelist()
    {
        String[] array = config.getStringList("worldWhitelist", Configuration.CATEGORY_GENERAL, new String[]{"0"}, "The list of worlds this mod effects");
        List<Integer> list = new ArrayList<Integer>(array.length);

        for (int i = 0; i < array.length; i++)
        {
            String s = array[i];
            try
            {
                list.add(Integer.parseInt(s));
            }
            catch (NumberFormatException ex)
            {
                FMLLog.warning(String.format("Invalid value found in config at index: %s", i));
            }
        }

        worldWhitelist = new int[list.size()];

        for (int i = 0; i < worldWhitelist.length; i++)
        {
            worldWhitelist[i] = list.get(i);
        }
    }
}
