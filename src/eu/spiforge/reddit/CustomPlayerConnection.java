package eu.spiforge.reddit;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.NetworkManager;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PlayerConnection;

public class CustomPlayerConnection extends PlayerConnection {

	private JavaPlugin plugin;
	private PacketSendHandler handler;
	
	public CustomPlayerConnection(Player player, PacketSendHandler handler, JavaPlugin plugin) {
		this(((CraftPlayer) player).getHandle(), handler, plugin);
	}
	
	public CustomPlayerConnection(EntityPlayer nmsPlayer, PacketSendHandler handler, JavaPlugin plugin) {
		this(nmsPlayer.server, nmsPlayer.playerConnection.networkManager, nmsPlayer, handler, plugin);
	}
	
	public CustomPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer, PacketSendHandler handler, JavaPlugin plugin) {
		super(minecraftserver, networkmanager, entityplayer);
		
		this.plugin = plugin;
		this.handler = handler;
	}

	@Override
	public void sendPacket(Packet<?> packet) {
		if (handler != null) {
			if (packet instanceof PacketPlayOutMapChunk) {
				packet = handler.handle((Player) this.player.getBukkitEntity(), (PacketPlayOutMapChunk) packet);
				
				if (packet == null) {
					return;
				}

				Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
					super.sendPacket(new PacketPlayOutMapChunk(this.player.world.getChunkAt(this.player.chunkX, this.player.chunkZ), 65535));
				}, 1);
			}
		}

		super.sendPacket(packet);
	}

	public static interface PacketSendHandler {
		public PacketPlayOutMapChunk handle(Player p, PacketPlayOutMapChunk packet);
	}
}
