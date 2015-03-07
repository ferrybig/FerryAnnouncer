/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferry.bukkit.plugins.announcer;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Fernando
 */
public class FerryAnnouncerPlugin extends JavaPlugin
{
	private int broadcastTickTime;
	private boolean broadcastWhenNobodyIsOnline;
	private List<String> broadcastMessages;
	private String broadcastMessagesPrefix;
	private boolean broadcastMessagesRandom;
	private int broadcastMessagesSize;

	@Override
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(this);
		logInfo("Stopped");
	}

	@Override
	public void onEnable()
	{
		FileConfiguration config = this.getConfig();
		config.options().header("# time: Time is ticks between broadcasts. 20 ticks for 1 sec, 1200 ticks = 60 sec\n"
			+ "# regenerateConfig: don't touch this\n"
			+ "# broadcastWhenNobodyIsOnline: must it broadcast when there aren't players online, set this to false to prevent the server.log from filling up by broadcasts\n"
			+ "# broadcastMessagePrefix: text to add in front of every message\n"
			+ "# messages: messages to broadcast\n"
			+ "# random: must it send themessages in a random order");
		boolean mustRegenConfig = config.getBoolean("regenerateConfig", true);
		if (mustRegenConfig)
		{
			this.saveDefaultConfig();
			this.reloadConfig();
			config = this.getConfig();
		}
		broadcastTickTime = config.getInt("time");
		broadcastWhenNobodyIsOnline = config.getBoolean("broadcastWhenNobodyIsOnline");
		broadcastMessagesPrefix = config.getString("broadcastMessagePrefix");
		broadcastMessages = config.getStringList("messages");
		broadcastMessagesRandom = config.getBoolean("random");
		if (this.broadcastMessages.isEmpty())
		{
			this.logWarning("Nothing to broadcast, disabling...");
			this.setEnabled(false);
			return;
		}
		if (broadcastTickTime < 20)
		{
			this.logWarning("Broadcast time set lower than 20, disabling to prevent lagg by broken config");
			this.setEnabled(false);
			return;
		}
		broadcastMessagesSize = FerryAnnouncerPlugin.this.broadcastMessages.size();
		Runnable task;
		if (broadcastMessagesRandom)
		{
			task = new BroadcastRandomTask();
		}
		else
		{
			task = new BroadcastSequenseTask();
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, task, broadcastTickTime, broadcastTickTime);
		logInfo("Started");
	}

	private void tryBroadcast(int index)
	{
		if (this.broadcastWhenNobodyIsOnline || (!Bukkit.getOnlinePlayers().isEmpty()))
		{
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.broadcastMessagesPrefix.concat(this.broadcastMessages.get(index))));
		}
	}

	private void logInfo(String string)
	{
		this.getLogger().info(string);
	}

	private void logWarning(String string)
	{
		this.getLogger().warning(string);
	}

	private class BroadcastRandomTask implements Runnable
	{
		public BroadcastRandomTask()
		{
		}
		Random r = new Random();

		@Override
		public void run()
		{
			FerryAnnouncerPlugin.this.tryBroadcast(this.r.nextInt(FerryAnnouncerPlugin.this.broadcastMessagesSize));
		}
	}

	private class BroadcastSequenseTask implements Runnable
	{
		public BroadcastSequenseTask()
		{
		}
		int i = 0;

		@Override
		public void run()
		{
			if (i >= FerryAnnouncerPlugin.this.broadcastMessagesSize)
			{
				i = 0;
			}
			FerryAnnouncerPlugin.this.tryBroadcast(i);
			i++;
		}
	}
}
