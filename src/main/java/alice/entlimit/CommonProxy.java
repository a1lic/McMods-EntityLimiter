package alice.entlimit;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.command.ICommand;

public class CommonProxy
{
	private final ICommand cmd = new CommandEntLimit();

	public void preInit()
	{
	}

	public void init()
	{
	}

	public void startServer(FMLServerStartingEvent event)
	{
		event.registerServerCommand(cmd);
	}
}
