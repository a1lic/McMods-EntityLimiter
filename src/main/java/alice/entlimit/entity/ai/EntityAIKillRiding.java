package alice.entlimit.entity.ai;

import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public final class EntityAIKillRiding extends EntityAIBase
{
	private EntityLivingBase entity;

	public EntityAIKillRiding(EntityLivingBase entity)
	{
		this.entity = entity;
	}

	@Override
	public boolean shouldExecute()
	{
		return (entity.riddenByEntity != null);
	}

	@Override
	public void updateTask()
	{
		super.updateTask();

		Entity ridder = entity.riddenByEntity;
		if(ridder instanceof EntityZombie)
		{
			((EntityZombie)ridder).setDead();
			entity.setDead();

			@SuppressWarnings("unchecked")
			Stream<EntityPlayer> p = entity.worldObj.playerEntities.stream();
			p.forEach(pl -> pl.addChatMessage(new ChatComponentText("チキンジョッキー死すべし、慈悲は無い")));
		}
	}
}
