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
		int dimension;
		int limit;
		switch(action.charAt(0))
		{
		case 'L': // list
			sender.addChatMessage(new ChatComponentTranslation("command.entlimit.listhead"));
			Map<Integer, Integer> m = EntityLimitManager.getLimitInWorlds();
			m.forEach((d, l) -> {
				if(l.intValue() == -1)
				{
					sender.addChatMessage(new ChatComponentTranslation("command.entlimit.diminfodef", d));
				}
				else
				{
					sender.addChatMessage(new ChatComponentTranslation("command.entlimit.diminfo", d, l));
				}
			});
			sender.addChatMessage(new ChatComponentTranslation("command.entlimit.listfoot"));
			break;
		case 'S': // set
			if(sender instanceof EntityPlayerMP)
			{
				if(!sender.canCommandSenderUseCommand(3, "entlimit"))
				{
					throw new CommandException("command.entlimit.op", sender.getCommandSenderName());
				}
			}
			if(argv.length >= 3)
			{
				// ディメンションIDが指定されている
				try
				{
					dimension = Integer.parseInt(argv[1]);
				}
				catch(NumberFormatException e)
				{
					throw new CommandException("command.entlimit.baddimid", argv[1]);
				}
				if(!DimensionManager.isDimensionRegistered(dimension))
				{
					throw new CommandException("command.entlimit.baddimid", argv[1]);
				}

				try
				{
					limit = Integer.parseInt(argv[2]);
					if(limit < 1)
					{
						throw new NumberFormatException();
					}
				}
				catch(NumberFormatException e)
				{
					throw new CommandException("command.entlimit.badnum", argv[2]);
				}
			}
			else if(argv.length == 2)
			{
				// ディメンションIDが指定されていない
				if(!(sender instanceof EntityPlayer))
				{
					// プレイヤーによって実行されていない
					throw new CommandException("command.entlimit.nodimid");
				}
				EntityPlayer player = (EntityPlayer)sender;
				World world = player.worldObj;
				WorldProvider provider = world.provider;
				dimension = provider.dimensionId;

				try
				{
					limit = Integer.parseInt(argv[1]);
					if(limit < 1)
					{
						throw new NumberFormatException();
					}
				}
				catch(NumberFormatException e)
				{
					throw new CommandException("command.entlimit.badnum", argv[1]);
				}
			}
			else
			{
				throw new CommandException("command.endlimit.badcmd");
			}
			EntityLimitManager.setLimitForWorld(dimension, limit);
			sender.addChatMessage(new ChatComponentTranslation("command.entlimit.set", new Integer(dimension), new Integer(limit)));
			break;
		case 'C': // clear
			if(sender instanceof EntityPlayerMP)
			{
				if(!sender.canCommandSenderUseCommand(3, "entlimit"))
				{
					throw new CommandException("command.entlimit.op", sender.getCommandSenderName());
				}
			}
			if(argv.length >= 2)
			{
				// ディメンションIDが指定されている
				try
				{
					dimension = Integer.parseInt(argv[1]);
				}
				catch(NumberFormatException e)
				{
					throw new CommandException("command.entlimit.baddimid", argv[1]);
				}
				if(!DimensionManager.isDimensionRegistered(dimension))
				{
					throw new CommandException("command.entlimit.baddimid", argv[1]);
				}
			}
			else
			{
				// ディメンションIDが指定されていない
				if(!(sender instanceof EntityPlayer))
				{
					// プレイヤーによって実行されていない
					throw new CommandException("command.entlimit.nodimid");
				}
				EntityPlayer player = (EntityPlayer)sender;
				World world = player.worldObj;
				WorldProvider provider = world.provider;
				dimension = provider.dimensionId;
			}
			EntityLimitManager.removeLimitForWorld(dimension);
			sender.addChatMessage(new ChatComponentTranslation("command.entlimit.clear", new Integer(dimension)));
			break;
		case 'V': // vanish
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
			sender.addChatMessage(new ChatComponentTranslation("command.entlimit.vanished", killCount[0]));
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
