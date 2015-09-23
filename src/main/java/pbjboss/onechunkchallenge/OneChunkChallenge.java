package pbjboss.onechunkchallenge;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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
    public void playerRespawnEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        EntityPlayer player = event.player;
        if (player.worldObj.isRemote)
            return;

        World world = player.worldObj;

        ChunkCoordinates spawnCoords = world.getSpawnPoint();
        int spawnChunkX = getChunkForPos(spawnCoords.posX);
        int spawnChunkZ = getChunkForPos(spawnCoords.posZ);

        int x = (spawnChunkX * 16) + 7, maxX = x + 8;
        int y = spawnCoords.posY;
        int z = (spawnChunkZ * 16) + 7, maxZ = z + 8;

        if (checkCoordsForSpawn(x, y, z, world))
            ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(x + .5, y + 1, z + .5, player.cameraYaw, ((EntityPlayerMP) player).cameraPitch);
        else
        {
            for (x -= 7;  x < maxX; x++)
                for (z -= 7; z < maxZ; z++)
                {
                    if (checkCoordsForSpawn(x, y, z, world))
                    {
                        ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(x + .5, y + 1, z + .5, player.cameraYaw, ((EntityPlayerMP) player).cameraPitch);
                        return;
                    }
                }
        }
    }

    private boolean checkCoordsForSpawn(int x, int y, int z, World world)
    {
        if (world.getBlock(x, y + 1, z).equals(Blocks.air) && world.getBlock(x, y + 2, z).equals(Blocks.air)) {
            if (world.getBlock(x, y, z).equals(Blocks.air)) {
                world.setBlock(x, y, z, Blocks.dirt);
                return true;
            }
        }
        return false;
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
