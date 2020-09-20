package eu.spiforge.reddit;

import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.UNDERLINE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.entity.Player;

import eu.spiforge.reddit.packet.AccessablePacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.NBTTagLongArray;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;

public class CommandChunk implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			
			Player player = (Player) sender;
			if (args.length == 0) {
				player.sendMessage("/chunk packet");
			} else if (args[0].equalsIgnoreCase("packet")) {
				
				CraftChunk craftChunk = (CraftChunk) player.getLocation().getChunk();
				PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(craftChunk.getHandle(), 65535);
				AccessablePacketPlayOutMapChunk accessablePacket = new AccessablePacketPlayOutMapChunk(packet);
				
				if (args.length == 1) {
					player.sendMessage("");
					for (String part : accessablePacket.compact()) {
						player.sendMessage(part);
					}
				} else {
					String subPacketCommand = args[1];
					
					if (subPacketCommand.equalsIgnoreCase("raw")) {
						if (args.length == 2) {
							player.sendMessage(RED + "Missing argument: /chunk packet raw " + UNDERLINE + "<valid, c, x, z, fullChunk, tileEntities, data, biomes, heightMap>");
						} else {
							String rawKey = args[2];
							
							if (rawKey.equalsIgnoreCase("valid")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "Valid");
								player.sendMessage(GREEN + "Determance if the packet could be parsed without problems");
								player.sendMessage(BLUE + "" + accessablePacket.isValid());
							} else if (rawKey.equalsIgnoreCase("x")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "X");
								player.sendMessage(GREEN + "The x coordinate of the chunk");
								player.sendMessage(BLUE + "" + accessablePacket.getX());
							} else if (rawKey.equalsIgnoreCase("z")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "Z");
								player.sendMessage(GREEN + "The z coordinate of the chunk");
								player.sendMessage(BLUE + "" + accessablePacket.getZ());
							} else if (rawKey.equalsIgnoreCase("c")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "C");
								player.sendMessage(GREEN + "Unknown reason");
								player.sendMessage(BLUE + "" + accessablePacket.getC());
							} else if (rawKey.equalsIgnoreCase("fullChunk")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "FullChunk");
								player.sendMessage(GREEN + "If true the packet represents a new chunk including biome information, else it represents a collection of block changes");
								player.sendMessage(BLUE + "" + accessablePacket.isFullChunk());
							} else if (rawKey.equalsIgnoreCase("tileEntities")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "TileEntities");
								player.sendMessage(GREEN + "A list of all tileentities in the chunk");
								
								if (accessablePacket.getTileEntities() != null) {
									player.sendMessage(BLUE + "All TileEntities (" + accessablePacket.getTileEntities().size() + ")");
									
									accessablePacket.getTileEntities().forEach(tileEntitiy -> {
										player.sendMessage(BLUE + "" + tileEntitiy.toString());
									});
								} else {
									player.sendMessage(BLUE + "No information in packet");
								}
							} else if (rawKey.equalsIgnoreCase("data")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "Data");
								player.sendMessage(GREEN + "All block data");
								player.sendMessage(BLUE + "" + Arrays.toString(accessablePacket.getData()));
							} else if (rawKey.equalsIgnoreCase("biomes")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "Biomes");
								player.sendMessage(GREEN + "All biomes. This uses 3D biomes");
								
								if (accessablePacket.getBiomes() != null) {
									player.sendMessage(BLUE + "int[" + accessablePacket.getBiomes().length + "]" + Arrays.toString(accessablePacket.getBiomes()));
								} else {
									player.sendMessage(BLUE + "No information in packet");
								}
							} else if (rawKey.equalsIgnoreCase("heightMap")) {
								player.sendMessage("");
								player.sendMessage(UNDERLINE + "" + GREEN + "HeightMap");
								player.sendMessage(GREEN + "The heighest block for each position in the chunk represented as long");
								
								if (accessablePacket.getHeightMap() != null) {
									accessablePacket.getHeightMap().getKeys().forEach(key -> {
										player.sendMessage(UNDERLINE + "" + BLUE + key + ":");
										
										if (accessablePacket.getHeightMap().get(key) instanceof NBTTagLongArray) {
											player.sendMessage(BLUE + "long[" + ((NBTTagLongArray) accessablePacket.getHeightMap().get(key)).size() + "]" + accessablePacket.getHeightMap().get(key));
										} else {
											player.sendMessage(BLUE + "" + accessablePacket.getHeightMap().get(key));
										}
									});
								} else {
									player.sendMessage(BLUE + "No information in packet");
								}
							}
						}
					}
				}
			} else {
				player.sendMessage(RED + "Unknown sub-command: '" + args[0] + "'");
			}
		} else {
			sender.sendMessage(RED + "This command can only be executed as a player!");
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		
		if (args.length == 1) {
			Arrays.asList("packet").forEach(key -> {
				if (key.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(key);
				}
			});
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("packet")) {
				Arrays.asList("raw").forEach(key -> {
					if (key.toLowerCase().startsWith(args[1].toLowerCase())) {
						result.add(key);
					}
				});
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("packet")) {
				if (args[1].equalsIgnoreCase("raw")) {
					Arrays.asList("valid", "c", "x", "z", "fullChunk", "tileEntities", "data", "biomes", "heightMap").forEach(key -> {
						if (key.toLowerCase().startsWith(args[2].toLowerCase())) {
							result.add(key);
						}
					});
				}
			}
		}
		
		return result;
	}
}
