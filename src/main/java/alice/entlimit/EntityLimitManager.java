package alice.entlimit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

public final class EntityLimitManager
{
	public static final Integer UNLIMITED = new Integer(-1);
	private static final Map<Integer, Integer> LIMIT_OF_DIMENSIONS = new HashMap<Integer, Integer>(4);

	public static int getLimitForWorld(World world)
	{
		return getLimitForWorld(world.provider.dimensionId);
	}

	public static int getLimitForWorld(int dim)
	{
		Integer l = LIMIT_OF_DIMENSIONS.getOrDefault(new Integer(dim), UNLIMITED);
		return l.intValue();
	}

	public static void setLimitForWorld(World world, int newLimit)
	{
		setLimitForWorld(world.provider.dimensionId, newLimit);
	}

	public static void setLimitForWorld(int dim, int newLimit)
	{
		if(newLimit < UNLIMITED.intValue())
		{
			newLimit = UNLIMITED.intValue();
		}

		Integer dimInteger = new Integer(dim);
		Integer limits = LIMIT_OF_DIMENSIONS.get(dimInteger);
		if(limits == null)
		{
			limits = new Integer(newLimit);
			LIMIT_OF_DIMENSIONS.put(dim, limits);
		}
		else
		{
			int limitsInt = limits.intValue();
			if(limitsInt < UNLIMITED.intValue())
			{
				limitsInt = UNLIMITED.intValue();
			}
			if(limitsInt != newLimit)
			{
				limits = new Integer(limitsInt);
				LIMIT_OF_DIMENSIONS.put(dim, limits);
			}
		}
	}
}
