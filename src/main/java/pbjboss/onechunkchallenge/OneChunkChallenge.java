package pbjboss.onechunkchallenge;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

@Mod(modid = OneChunkChallenge.MODID, version = OneChunkChallenge.VERSION, name = OneChunkChallenge.NAME)
public class OneChunkChallenge
{
    public static final String MODID = "onechunkchallenge";
    public static final String VERSION = "1.7.10-0.1";
    public static final String NAME = "One Chunk Challenge";

    @Mod.Instance
    public static OneChunkChallenge instance;


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void blockBreakEvent(BlockEvent.BreakEvent event)
    {
        //checkBlockEvent(event);
    }

    @SubscribeEvent
    public void blockPlaceEvent(BlockEvent.PlaceEvent event)
    {
        checkBlockEvent(event);
    }

    private void checkBlockEvent(BlockEvent event)
    {
        if (event.world.isRemote)
            return;

        ChunkCoordinates spawnCoords = event.world.getSpawnPoint();
        int chunkX = getChunkForPos(spawnCoords.posX);
        int chunkZ = getChunkForPos(spawnCoords.posZ);
        int blockChunkX = getChunkForPos(event.x);
        int blockChunkZ = getChunkForPos(event.z);

        if (chunkX != blockChunkX || chunkZ != blockChunkZ)
            event.setCanceled(true);
    }

    private int getChunkForPos(int pos)
    {
        return (pos - (pos % 16)) / 16;
    }
}
