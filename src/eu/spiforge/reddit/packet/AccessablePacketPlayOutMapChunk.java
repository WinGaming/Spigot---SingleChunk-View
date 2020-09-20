package eu.spiforge.reddit.packet;

import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import static eu.spiforge.reddit.Utils.getValue;
import static eu.spiforge.reddit.Utils.setValue;

import java.util.Arrays;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;

public class AccessablePacketPlayOutMapChunk {
	
	// Unknown:
	private int c;
	
	// Known:
	private int x;
	private int z;
	
	private boolean fullChunk;
	private List<NBTTagCompound> tileEntities;
	
	private byte[] data;
	private int[] biomes;
	
	private NBTTagCompound heightMap;
	
	private boolean valid = false;
	
	@SuppressWarnings("unchecked")
	public AccessablePacketPlayOutMapChunk(PacketPlayOutMapChunk mapPacket) {
		try {
			this.x = (int) getValue(PacketPlayOutMapChunk.class, "a", mapPacket);
			this.z = (int) getValue(PacketPlayOutMapChunk.class, "b", mapPacket);
			
			this.data = (byte[]) getValue(PacketPlayOutMapChunk.class, "f", mapPacket);
			this.biomes = (int[]) getValue(PacketPlayOutMapChunk.class, "e", mapPacket);
			
			this.fullChunk = (boolean) getValue(PacketPlayOutMapChunk.class, "h", mapPacket);
			this.heightMap = (NBTTagCompound) getValue(PacketPlayOutMapChunk.class, "d", mapPacket);
			this.tileEntities = (List<NBTTagCompound>) getValue(PacketPlayOutMapChunk.class, "g", mapPacket);
			
			this.c = (int) getValue(PacketPlayOutMapChunk.class, "c", mapPacket);
			
			this.valid = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public AccessablePacketPlayOutMapChunk(int c, int x, int z, boolean fullChunk, List<NBTTagCompound> tileEntities, byte[] data, int[] biomes, NBTTagCompound heightMap, boolean valid) {
		this.c = c;
		this.x = x;
		this.z = z;
		this.fullChunk = fullChunk;
		this.tileEntities = tileEntities;
		this.data = data;
		this.biomes = biomes;
		this.heightMap = heightMap;
		this.valid = valid;
	}
	
	public PacketPlayOutMapChunk toNMSPacket() {
		try {
			PacketPlayOutMapChunk result = new PacketPlayOutMapChunk();
			setValue(result, "a", this.x);
			setValue(result, "b", this.z);
			setValue(result, "c", this.c);
			setValue(result, "d", this.heightMap);
			setValue(result, "e", this.biomes);
			setValue(result, "f", this.data);
			setValue(result, "g", this.tileEntities);
			setValue(result, "h", this.fullChunk);
			
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public String[] compact() {
		return new String[] {
			GREEN + "Valid: " + YELLOW + this.valid,
			GREEN + "C: " + YELLOW + this.c,
			GREEN + "X / Z: " + YELLOW + this.x + ", " + this.z,
			GREEN + "FullChunk: " + YELLOW + this.fullChunk,
			GREEN + "TileEntities: " + YELLOW + ("NBTTagCompound[" + this.tileEntities.size() + "]"),
			GREEN + "Data: " + YELLOW + ("byte[" + this.data.length + "]"),
			GREEN + "Biomes: " + YELLOW + ("int[" + this.biomes.length + "]"),
			GREEN + "Heightmap: " + YELLOW + (this.heightMap.toString().substring(0, 15) + "..."),
		};
	}
	
	public int getC() {
		return c;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public boolean isFullChunk() {
		return fullChunk;
	}
	
	public List<NBTTagCompound> getTileEntities() {
		return tileEntities;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int[] getBiomes() {
		return biomes;
	}
	
	public NBTTagCompound getHeightMap() {
		return heightMap;
	}
	
	public boolean isValid() {
		return valid;
	}

	@Override
	public String toString() {
		return "AccessablePacketPlayOutMapChunk [c=" + c + ", x=" + x + ", z=" + z + ", fullChunk=" + fullChunk
				+ ", tileEntities=" + tileEntities + ", data=" + Arrays.toString(data) + ", biomes="
				+ Arrays.toString(biomes) + ", heightMap=" + heightMap + ", valid=" + valid + "]";
	}
}
