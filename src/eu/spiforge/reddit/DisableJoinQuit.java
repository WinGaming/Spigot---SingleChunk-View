package eu.spiforge.reddit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import eu.spiforge.reddit.CustomPlayerConnection.PacketSendHandler;
import net.minecraft.server.v1_16_R2.ChunkProviderServer;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.LightEngineThreaded;
import net.minecraft.server.v1_16_R2.PacketPlayOutLightUpdate;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R2.PlayerChunkMap;

public class DisableJoinQuit extends JavaPlugin implements Listener {
	
	private static Map<Integer, Map<Integer, PacketPlayOutMapChunk>> cachedChunks = new HashMap<>();
	
	public static final boolean SEND_CUSTOM_LIGHT_UPDATE = true;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	private int oldX = 99999;
	private int oldZ = 99999;
	
	@EventHandler
	public void onQuit(PlayerMoveEvent event) {
		int cX = event.getPlayer().getLocation().getChunk().getX();
		int cZ = event.getPlayer().getLocation().getChunk().getZ();
		
		if (cX != oldX || cZ != oldZ) {
			PacketPlayOutUnloadChunk unloadPacket = new PacketPlayOutUnloadChunk(oldX, oldZ);
			((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(unloadPacket);
			
			if (SEND_CUSTOM_LIGHT_UPDATE) {
				Bukkit.getScheduler().runTaskLater(this, () -> {
					try {
						Player player = event.getPlayer();
						CraftPlayer craftp = (CraftPlayer) player;
						EntityPlayer nmsPlayer = craftp.getHandle();
						CraftChunk craftc = (CraftChunk) player.getLocation().getChunk();
						ChunkProviderServer chunkServer = ((ChunkProviderServer) nmsPlayer.world.getChunkProvider());
						
						Field lightField = PlayerChunkMap.class.getDeclaredField("lightEngine");
						lightField.setAccessible(true);
						LightEngineThreaded engine = (LightEngineThreaded) lightField.get(chunkServer.playerChunkMap);
						lightField.setAccessible(false);
						
						System.out.println("Sending light update...");
						craftp.getHandle().playerConnection.sendPacket(new PacketPlayOutLightUpdate(craftc.getHandle().getPos(), engine, true));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}, 1);
			}
			
			oldX = cX;
			oldZ = cZ;
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
			if (SEND_CUSTOM_LIGHT_UPDATE) {
				Bukkit.getScheduler().runTaskLater(this, () -> {
					try {
						Player player = event.getPlayer();
						CraftPlayer craftp = (CraftPlayer) player;
						EntityPlayer nmsPlayer = craftp.getHandle();
						CraftChunk craftc = (CraftChunk) player.getLocation().getChunk();
						ChunkProviderServer chunkServer = ((ChunkProviderServer) nmsPlayer.world.getChunkProvider());
						
						Field lightField = PlayerChunkMap.class.getDeclaredField("lightEngine");
						lightField.setAccessible(true);
						LightEngineThreaded engine = (LightEngineThreaded) lightField.get(chunkServer.playerChunkMap);
						lightField.setAccessible(false);
						
						System.out.println("Sending light update...");
						craftp.getHandle().playerConnection.sendPacket(new PacketPlayOutLightUpdate(craftc.getHandle().getPos(), engine, true));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}, 1);
			}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		CustomPlayerConnection pc = new CustomPlayerConnection(event.getPlayer());
	    CustomPlayerConnection.getNMSPlayer(event.getPlayer()).playerConnection = pc;
	    pc.setHandler(new PacketSendHandler() {
	        @Override
	        public PacketPlayOutMapChunk handle(Player p, PacketPlayOutMapChunk packet) {
	        	int chunkX = 0, chunkZ = 0;	        	

	        	try {
		        	Field fieldX = packet.getClass().getDeclaredField("a");
		        	fieldX.setAccessible(true);
		        	chunkX = fieldX.getInt(packet);
		        	fieldX.setAccessible(false);
	        		
		        	Field fieldZ = packet.getClass().getDeclaredField("b");
		        	fieldZ.setAccessible(true);
					chunkZ = fieldZ.getInt(packet);
					fieldZ.setAccessible(false);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        	if (!cachedChunks.containsKey(chunkX)) {
	        		cachedChunks.put(chunkX, new HashMap<>());
	        	}
	        	
	        	cachedChunks.get(chunkX).put(chunkZ, packet);
	        	
	        	int playerChunkX = p.getLocation().getChunk().getX();
	        	int playerChunkZ = p.getLocation().getChunk().getZ();
	        	
	        	PacketPlayOutMapChunk chachedPacket = null;
	        	if (cachedChunks.containsKey(playerChunkX) && cachedChunks.get(playerChunkX).containsKey(playerChunkZ)) {
	        		chachedPacket = cachedChunks.get(playerChunkX).get(playerChunkZ);
	        	}
	        	
	        	if (chachedPacket != null) {
//	        		try {
//			        	Field fieldX = packet.getClass().getDeclaredField("a");
//			        	fieldX.setAccessible(true);
//			        	System.out.println(fieldX.getInt(chachedPacket));
//			        	fieldX.setAccessible(false);
//		        		
//			        	Field fieldZ = packet.getClass().getDeclaredField("b");
//			        	fieldZ.setAccessible(true);
//			        	System.out.println(fieldZ.getInt(chachedPacket));
//						fieldZ.setAccessible(false);
//					} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
	        		
	        		return chachedPacket;
	        	}
	        	
	        	if (playerChunkX == chunkX && playerChunkZ == chunkZ) {	        		
	        		return packet;
	        	}
	        	
	        	return null;
	        }
	    });
	}
}