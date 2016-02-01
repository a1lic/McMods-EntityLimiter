package alice.entlimit;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Mod(
	modid = "entitylimiter",
	name = "Entity Limiter",
	version = "0.3",
	dependencies = "required-after:Forge@[10.13.4.1558,)"
)
public final class EntityLimiter
{
	@Mod.Instance("entitylimiter")
	public static EntityLimiter INSTANCE;

	@Mod.Metadata("entitylimiter")
	public ModMetadata meta;

	public static final Logger LOG = LogManager.getLogger("entitylimiter");

	@SidedProxy(clientSide = "alice.entlimit.CommonProxy", serverSide = "alice.entlimit.CommonProxy")
	public static CommonProxy PROXY;

	private Configuration config;

	@Mod.EventHandler
	public void forgePreInitialization(FMLPreInitializationEvent event)
	{
		this.config = new Configuration(event.getSuggestedConfigurationFile());
		this.config.load();

		this.meta.description = "ディメンション単位でエンティティの上限を設定するMODです。";
		this.meta.url = "http://a1lic.net/";
		this.meta.authorList.add("alice");
		this.meta.credits = "© 2016 alice";
		this.meta.autogenerated = false;

		PROXY.preInit();
	}

	@Mod.EventHandler
	public void forgeInitialization(FMLInitializationEvent event)
	{
		EventBus bus = getForgeEventBus();
		bus.register(this);

		PROXY.init();
	}

	@Mod.EventHandler
	public void serverStartup(FMLServerStartingEvent event)
	{
		PROXY.startServer(event);
	}

	public static EventBus getForgeEventBus()
	{
		return MinecraftForge.EVENT_BUS;
	}

	public static EventBus getFMLEventBus()
	{
		FMLCommonHandler handler = FMLCommonHandler.instance();
		return handler.bus();
	}

	@NetworkCheckHandler
	public boolean netCheckHandler(Map<String, String> mods, Side side)
	{
		return true;
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event)
	{
		Entity entity = event.entity;
		if((entity instanceof EntityPlayer) || !(entity instanceof EntityLivingBase))
		{
			// プレイヤーが入ってきた場合は阻害しない
			return;
		}

		int limit = EntityLimitManager.getLimitForWorld(event.world);
		if(limit < 0)
		{
			// 上限が設定されていないか、不正な値の場合は何もしない
		}
		else
		{
			int current = getMobCount(event.world);
			if(current >= limit)
			{
				event.setCanceled(true);
		//		return;
			}
		}

		//if(entity instanceof EntityChicken)
		//{
		//	((EntityChicken)entity).tasks.addTask(0, new EntityAIKillRiding((EntityChicken)entity));
		//}
	}

	public static void vanishOvers(World world, int[] count)
	{
		if(count.length < 1)
		{
			throw new IllegalArgumentException("countは要素数が少なくとも1つ必要です");
		}

		int entityLimit = EntityLimitManager.getLimitForWorld(world);
		if(entityLimit < 0)
		{
			return;
		}

		// EntityMobが先に来るように並べ替える
		Entity[] mobs = EntityLimiter.getMobStream(world).sorted(EntityLimiter::compareEntity).toArray(Entity[]::new);
		int mobsCount = EntityLimiter.getMobCount(mobs);

		// 超過していないかチェック
		int overs = mobsCount - entityLimit;
		if(overs <= 0)
		{
			return;
		}

		// 超過分を切り出して、超過分のそれぞれのEntityにisDeadを設定する
		Entity[] arrayOvers = Stream.of(mobs).limit(overs).toArray(Entity[]::new);
		count[0] += arrayOvers.length;
		Stream.of(arrayOvers).forEach(m -> {
			m.setDead();
			m.isDead = true;
		});
	}

	private static int getFactor(Entity a)
	{
		// 種類ごとに優先度を返す
		// 数値が高い方が優先度が高い
		if(a instanceof EntityMob)
		{
			return -3;
		}
		else if(a instanceof EntityTameable)
		{
			return 0;
		}
		else if(a instanceof EntityVillager)
		{
			return -1;
		}
		else
		{
			return -2;
		}
	}

	private static int compareEntity(Entity a, Entity b)
	{
		int typeA = getFactor(a);
		int typeB = getFactor(b);
		return typeA - typeB;
	}

	private static Stream<Entity> getMobStream(@Nullable World world)
	{
		if(world == null)
		{
			return Stream.empty();
		}

		@SuppressWarnings("unchecked")
		List<Entity> l = world.loadedEntityList;
		return l.stream();
	}

	private static int getMobCount(@Nullable World world)
	{
		Stream<Entity> mobList = getMobStream(world);
		return (int)mobList.count();
	}

	private static int getMobCount(Entity[] entities)
	{
		Stream<Entity> entityStream = Stream.of(entities);
		long count = entityStream.filter(e -> (e instanceof EntityLivingBase)).filter(e -> !(e instanceof EntityPlayer)).count();
		return (int)count;
	}
}
