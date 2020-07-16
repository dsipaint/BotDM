package com.github.dsipaint;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StopListener extends ListenerAdapter
{
	//DEBUG CLASS ONLY
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		
		if(msg.equalsIgnoreCase("stop"))
		{
			e.getJDA().shutdownNow();
			System.exit(0);
		}
	}
}
