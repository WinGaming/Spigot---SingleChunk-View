package eu.spiforge.reddit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_16_R2.ChunkProviderServer;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.LightEngineThreaded;
import net.minecraft.server.v1_16_R2.PacketPlayOutLightUpdate;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R2.PlayerChunkMap;

public class SingleChunkView extends JavaPlugin implements Listener {
	
	public static final boolean SEND_CUSTOM_LIGHT_UPDATE = true;
	
	private static Map<Integer, Map<Integer, PacketPlayOutMapChunk>> cachedChunks = new HashMap<>();
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	private Map<Player, PosXZ> lastPositions = new HashMap<>();
	
	public class PosXZ {
		public int x, z;

		public PosXZ(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}
	
	@EventHandler
	public void onQuit(PlayerMoveEvent event) {
		int currentX = event.getPlayer().getLocation().getChunk().getX();
		int currentZ = event.getPlayer().getLocation().getChunk().getZ();
		
		PosXZ oldChunk = lastPositions.get(event.getPlayer());
		
		if (currentX != oldChunk.x || currentZ != oldChunk.z) {
			Player player = event.getPlayer();
			CraftPlayer craftPlayer = (CraftPlayer) player;
			EntityPlayer nmsPlayer = craftPlayer.getHandle();
			PacketPlayOutUnloadChunk unloadPacket = new PacketPlayOutUnloadChunk(oldChunk.x, oldChunk.z);
			
			nmsPlayer.playerConnection.sendPacket(unloadPacket);
			
			if (SEND_CUSTOM_LIGHT_UPDATE) {
				Bukkit.getScheduler().runTaskLater(this, () -> {
					try {
						
						CraftChunk craftc = (CraftChunk) player.getLocation().getChunk();
						ChunkProviderServer chunkServer = ((ChunkProviderServer) nmsPlayer.world.getChunkProvider());
						
						Field lightField = PlayerChunkMap.class.getDeclaredField("lightEngine");
						lightField.setAccessible(true);
						LightEngineThreaded engine = (LightEngineThreaded) lightField.get(chunkServer.playerChunkMap);
						lightField.setAccessible(false);
						
						craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutLightUpdate(craftc.getHandle().getPos(), engine, true));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}, 1);
			}
			
			lastPositions.put(player, new PosXZ(currentX, currentZ));
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (SEND_CUSTOM_LIGHT_UPDATE) {
			Bukkit.getScheduler().runTaskLater(this, () -> {
				try {
					Player player = event.getPlayer();
					CraftPlayer craftPlayer = (CraftPlayer) player;
					EntityPlayer nmsPlayer = craftPlayer.getHandle();
					CraftChunk craftc = (CraftChunk) player.getLocation().getChunk();
					ChunkProviderServer chunkServer = ((ChunkProviderServer) nmsPlayer.world.getChunkProvider());
					
					Field lightEngineField = PlayerChunkMap.class.getDeclaredField("lightEngine");
					lightEngineField.setAccessible(true);
					LightEngineThreaded lightEngine = (LightEngineThreaded) lightEngineField.get(chunkServer.playerChunkMap);
					lightEngineField.setAccessible(false);
					
					craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutLightUpdate(craftc.getHandle().getPos(), lightEngine, true));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}, 1);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Chunk chunk = event.getPlayer().getLocation().getChunk();
		lastPositions.put(event.getPlayer(), new PosXZ(chunk.getX(), chunk.getZ()));
		
		Player player = event.getPlayer();
		CraftPlayer craftPlayer = (CraftPlayer) player;
		EntityPlayer nmsPlayer = craftPlayer.getHandle();
		
		nmsPlayer.playerConnection = new CustomPlayerConnection(event.getPlayer(), (innerPlayer, mapPacket) -> {
			int chunkX = 0;
			int chunkZ = 0;	        	

        	try {
	        	Field fieldChunkX = mapPacket.getClass().getDeclaredField("a");
	        	fieldChunkX.setAccessible(true);
	        	chunkX = fieldChunkX.getInt(mapPacket);
	        	fieldChunkX.setAccessible(false);
        		
	        	Field fieldChunkZ = mapPacket.getClass().getDeclaredField("b");
	        	fieldChunkZ.setAccessible(true);
				chunkZ = fieldChunkZ.getInt(mapPacket);
				fieldChunkZ.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
        	if (!cachedChunks.containsKey(chunkX)) {
        		cachedChunks.put(chunkX, new HashMap<>());
        	}
        	
        	cachedChunks.get(chunkX).put(chunkZ, mapPacket);
        	
        	int playerChunkX = innerPlayer.getLocation().getChunk().getX();
        	int playerChunkZ = innerPlayer.getLocation().getChunk().getZ();
        	
        	PacketPlayOutMapChunk cachedPacket = null;
        	if (cachedChunks.containsKey(playerChunkX) && cachedChunks.get(playerChunkX).containsKey(playerChunkZ)) {
        		cachedPacket = cachedChunks.get(playerChunkX).get(playerChunkZ);
        	}
        	
        	if (cachedPacket != null) {
        		return cachedPacket;
        	}
        	
        	if (playerChunkX == chunkX && playerChunkZ == chunkZ) {	        		
        		// Dead code: This code should never be called
        		System.out.println("This message should never be displayed. This is a bug");
        		return mapPacket;
        	}
        	
        	return null;
		}, this);
	}
}