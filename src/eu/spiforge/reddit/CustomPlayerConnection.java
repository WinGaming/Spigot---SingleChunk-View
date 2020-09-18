package eu.spiforge.reddit;

import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.NetworkManager;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PlayerConnection;

public class CustomPlayerConnection extends PlayerConnection {
	
	public CustomPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
		super(minecraftserver, networkmanager, entityplayer);
	}
	
	private PacketSendHandler handler;
	public void setHandler(PacketSendHandler handler) {
	    this.handler = handler;
	}

	public CustomPlayerConnection(EntityPlayer p) {
		this(p.server, p.playerConnection.networkManager, p);
	}
	
	public CustomPlayerConnection(Player player) {
	    this(getNMSPlayer(player));
	}
	
	public static EntityPlayer getNMSPlayer(Player p) {
	    return ((CraftPlayer)p).getHandle();
	}
	
	@Override
	public void sendPacket(Packet<?> packet) {
		if(handler != null) {
		    if(packet instanceof PacketPlayOutMapChunk) {
		        packet = handler.handle((Player) this.player.getBukkitEntity(), (PacketPlayOutMapChunk) packet);
		        if (packet == null) return;
		    }
		}
		
	    super.sendPacket(packet); //Methode in PlayerConnection aufrufen
	}
	
	public static interface PacketSendHandler {
	    public PacketPlayOutMapChunk handle(Player p, PacketPlayOutMapChunk packet);
	}
}
