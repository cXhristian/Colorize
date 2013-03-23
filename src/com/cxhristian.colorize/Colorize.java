package com.cxhristian.colorize;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import lib.PatPeter.SQLibrary.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Colorize extends JavaPlugin {

	private Random randomGenerator;
	public String logPrefix = "[COLORIZE] ";
	public SQLite sqlite;
	public File folder = new File("plugins" + File.separator + "Colorize");

	@Override
	public void onEnable(){
		this.randomGenerator = new Random();

		this.sqlite = new SQLite(getLogger(), this.logPrefix, this.folder.getPath(), "Colorize");
		// Initialize SQLite handler
		if (!this.sqlite.isOpen()) {
			this.sqlite.open();
		}
		if (!this.sqlite.isTable("players")) {
			this.getLogger().info("Creating table players");
			String query = "CREATE TABLE players (name VARCHAR(255) PRIMARY KEY, color VARCHAR(10));";
			try {
				this.sqlite.query(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// Event listener
		getServer().getPluginManager().registerEvents(new Listener() {


			@EventHandler
			public void playerJoin(PlayerJoinEvent event) {

				Player player = event.getPlayer();
				ChatColor color = null;


				// Checking for db entry, if so use that color
				ResultSet result = null;
				try {
					result = sqlite.query("SELECT * FROM players WHERE name='" + player.getName() + "'");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					if (result != null && result.next()) {
						color = ChatColor.getByChar(result.getString("color"));
					}
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
				finally {
					if (result != null) {
						try {
							result.close();
						}
						catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
				if (color != null) {
					setColor(player, color);
				}
				// Else set a random color and save that to db
				else {
					color = randomColor();
					saveColor(player, color);
				}
			}
		}, this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
		}
		else {
			Player player = (Player) sender;
			// Set random color
			if (cmd.getName().equalsIgnoreCase("randomcolor")){
				ChatColor color = randomColor();
				saveColor(player, color);
				player.sendMessage("Changed color to " + color.name());
				return true;
			}
			// Take color input
			else if (cmd.getName().equalsIgnoreCase("setcolor")) {
				if (args.length > 0) {
					String colorCode = args[0];
					Pattern p = Pattern.compile("^[0-9a-f]$");
					Matcher m = p.matcher(colorCode);
					if (m.find()) {
						ChatColor color = ChatColor.getByChar(colorCode);
						saveColor(player, color);
						player.sendMessage("Changed color to " + color.name());
					}
					else {
						player.sendMessage("Please provide a proper color code (0-9 a-z)");
					}
				}
				else {
					player.sendMessage("Please provide a color value");
				}
				return true;
			}
			// List all available colors
			else if (cmd.getName().equalsIgnoreCase("listcolors")) {
				for (ChatColor color : ChatColor.values()) {
					player.sendMessage("Color " + color +  color.name() + ChatColor.WHITE + " Code: " + color.getChar());
					if (color.equals(ChatColor.WHITE)) {
						break;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDisable() {
		sqlite.close();
	}

	private void log(String str) {
		getLogger().info(str);
	}

	private void saveColor(Player player, ChatColor color) {
		log("Saving color");
		// Save to db
		try {
			sqlite.query("INSERT OR REPLACE INTO players (name, color) VALUES ('" + player.getName() + "', '" + color.getChar() + "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.setColor(player, color);
	}

	private void setColor(Player player, ChatColor color) {
		player.setDisplayName(color + player.getName() + ChatColor.WHITE);

	}

	public ChatColor randomColor() {
		List<ChatColor> colors = Arrays.asList(
				ChatColor.DARK_GREEN,
				ChatColor.DARK_AQUA,
				ChatColor.DARK_RED,
				ChatColor.DARK_PURPLE,
				ChatColor.GOLD,
				ChatColor.GRAY,
				ChatColor.DARK_GRAY,
				ChatColor.BLUE,
				ChatColor.GREEN,
				ChatColor.AQUA,
				ChatColor.RED,
				ChatColor.LIGHT_PURPLE,
				ChatColor.YELLOW,
				ChatColor.WHITE);
		int index = randomGenerator.nextInt(colors.size());
		return colors.get(index);

	}
}

