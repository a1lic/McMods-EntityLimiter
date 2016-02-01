package alice.entlimit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import scala.actors.threadpool.Arrays;

public final class EntityLimitManager
{
	public static final Integer UNLIMITED = new Integer(-1);
	private static final Map<Integer, Integer> PENDING_CHANGES = new HashMap<Integer, Integer>();

	public static Map<Integer, Integer> getLimitInWorlds()
	{
		Stream<WorldServer> worlds = Stream.of(DimensionManager.getWorlds());
		final Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		worlds.sorted((a, b) -> (a.provider.dimensionId - b.provider.dimensionId)).forEach(w -> m.put(w.provider.dimensionId, getLimitForWorld(w)));
		return m;
	}

	public static int getLimitForWorld(World world)
	{
		WorldData d = WorldData.forWorld(world);
		NBTTagCompound nbt = d.getData();

		if(nbt.hasKey("Limits"))
		{
			int limit = nbt.getInteger("Limits");
			if(limit < UNLIMITED.intValue())
			{
				limit = UNLIMITED.intValue();
				nbt.setInteger("Limits", UNLIMITED.intValue());
				d.markDirty();
			}
			return limit;
		}
		else
		{
			return UNLIMITED.intValue();
		}
	}

	public static void setLimitForWorld(World world, int newLimit)
	{
		if(newLimit < UNLIMITED.intValue())
		{
			newLimit = UNLIMITED.intValue();
		}

		WorldData d = WorldData.forWorld(world);
		NBTTagCompound nbt = d.getData();

		if(nbt.hasKey("Limits"))
		{
			int limit = nbt.getInteger("Limits");
			if(limit < UNLIMITED.intValue())
			{
				limit = UNLIMITED.intValue();
			}
			if(newLimit != limit)
			{
				nbt.setInteger("Limits", newLimit);
				d.markDirty();
			}
		}
		else
		{
			nbt.setInteger("Limits", newLimit);
			d.markDirty();
		}
	}

	public static void setLimitForWorld(int dim, int newLimit)
	{
		@SuppressWarnings("unchecked")
		List<Integer> dims = (List<Integer>)Arrays.asList(DimensionManager.getStaticDimensionIDs());
		if(dims.contains(dim))
		{
			World world = DimensionManager.getWorld(dim);
			if(world == null)
			{
				PENDING_CHANGES.put(dim, newLimit);
			}
			else
			{
				setLimitForWorld(world, newLimit);
			}
		}
	}

	public static void removeLimitForWorld(int dim)
	{
		setLimitForWorld(dim, UNLIMITED.intValue());
	}
}
