package alice.entlimit;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public final class CommandEntLimit implements ICommand
{
	private void list(ICommandSender sender)
	{
		sender.addChatMessage(new ChatComponentTranslation("command.entlimit.listhead"));

		Map<Integer, Integer> m = EntityLimitManager.getLimitInWorlds();

		m.forEach((d, l) ->
		{
			if(l.equals(EntityLimitManager.UNLIMITED_CONST))
			{
				sender.addChatMessage(new ChatComponentTranslation("command.entlimit.diminfodef", d));
			}
			else
			{
				sender.addChatMessage(new ChatComponentTranslation("command.entlimit.diminfo", d, l));
			}
		});

		sender.addChatMessage(new ChatComponentTranslation("command.entlimit.listfoot"));
	}

	private void set(ICommandSender sender, String limit)
	{
		if(!(sender instanceof EntityPlayer))
		{
			// プレイヤーによって実行されていない
			throw new CommandException("command.entlimit.nodimid");
		}

		EntityPlayer player = (EntityPlayer)sender;
		World world = player.worldObj;
		WorldProvider provider = world.provider;

		this.set(sender, Integer.toString(provider.dimensionId), limit);
	}

	private void set(ICommandSender sender, String dimension, String limit)
	{
		if(sender instanceof EntityPlayerMP)
		{
			if(!sender.canCommandSenderUseCommand(3, "entlimit"))
			{
				throw new CommandException("command.entlimit.op", sender.getCommandSenderName());
			}
		}

		int dimensionId;
		try
		{
			dimensionId = Integer.parseInt(dimension);
		}
		catch(NumberFormatException e)
		{
			throw new CommandException("command.entlimit.baddimid", dimension);
		}
		if(!DimensionManager.isDimensionRegistered(dimensionId))
		{
			throw new CommandException("command.entlimit.baddimid", dimension);
		}

		int limitNum;
		try
		{
			limitNum = Integer.parseInt(limit);
			if(limitNum < 1)
			{
				throw new NumberFormatException();
			}
		}
		catch(NumberFormatException e)
		{
			throw new CommandException("command.entlimit.badnum", limit);
		}

		EntityLimitManager.setLimitForWorld(dimensionId, limitNum);
		sender.addChatMessage(new ChatComponentTranslation("command.entlimit.set", dimension, limit));
	}

	private void clear(ICommandSender sender)
	{
		if(!(sender instanceof EntityPlayer))
		{
			// プレイヤーによって実行されていない
			throw new CommandException("command.entlimit.nodimid");
		}

		EntityPlayer player = (EntityPlayer)sender;
		World world = player.worldObj;
		WorldProvider provider = world.provider;

		this.clear(sender, Integer.toString(provider.dimensionId));
	}

	private void clear(ICommandSender sender, String dimension)
	{
		if(sender instanceof EntityPlayerMP)
		{
			if(!sender.canCommandSenderUseCommand(3, "entlimit"))
			{
				throw new CommandException("command.entlimit.op", sender.getCommandSenderName());
			}
		}

		int dimensionId;
		try
		{
			dimensionId = Integer.parseInt(dimension);
		}
		catch(NumberFormatException e)
		{
			throw new CommandException("command.entlimit.baddimid", dimension);
		}
		if(!DimensionManager.isDimensionRegistered(dimensionId))
		{
			throw new CommandException("command.entlimit.baddimid", dimension);
		}

		EntityLimitManager.removeLimitForWorld(dimensionId);
		sender.addChatMessage(new ChatComponentTranslation("command.entlimit.clear", dimension));
	}

	private void vanish(ICommandSender sender)
	{
		if(sender instanceof EntityPlayerMP)
		{
			if(!sender.canCommandSenderUseCommand(3, "entlimit"))
			{
				throw new CommandException("command.entlimit.op", sender.getCommandSenderName());
			}
		}

		MinecraftServer server = MinecraftServer.getServer();
		if(server == null)
		{
			return;
		}

		Stream<WorldServer> streamWorld = Stream.of(server.worldServers);
		// 要素数1の配列を使うことで参照渡しっぽいやり方ができる
		final int[] killCount = new int[1];
		killCount[0] = 0;
		streamWorld.forEach(w -> EntityLimiter.vanishOvers(w, killCount));
		sender.addChatMessage(new ChatComponentTranslation("command.entlimit.vanished", Integer.toString(killCount[0])));
	}

	@Override
	public int compareTo(Object o)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName()
	{
		return "entlimit";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public List<String> getCommandAliases()
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] argv)
	{
		if(argv.length == 0)
		{
			throw new CommandException("command.entlimit.usage");
		}

		String action = argv[0].toUpperCase(Locale.ENGLISH);
		switch(action.charAt(0))
		{
		case 'L': // list
			this.list(sender);
			break;
		case 'S': // set
			if(argv.length >= 3)
			{
				// ディメンションIDが指定されている
				this.set(sender, argv[1], argv[2]);
			}
			else if(argv.length == 2)
			{
				// ディメンションIDが指定されていない
				this.set(sender, argv[1]);
			}
			else
			{
				throw new CommandException("command.endlimit.badcmd");
			}
			break;
		case 'C': // clear
			if(argv.length >= 2)
			{
				// ディメンションIDが指定されている
				this.clear(sender, argv[1]);
			}
			else
			{
				// ディメンションIDが指定されていない
				this.clear(sender);
			}
			break;
		case 'V': // vanish
			this.vanish(sender);
			break;
		default:
			throw new CommandException("command.entlimit.unknownsubcmd", argv[0]);
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return true;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] argv)
	{
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] argv, int argi)
	{
		return false;
	}
}
