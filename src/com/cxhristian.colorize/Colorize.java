package com.cxhristian.colorize;

import com.avaje.ebean.EbeanServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Colorize extends JavaPlugin {

	private Random randomGenerator;
	private Logger log;
	private List<ChatColor> colors;

	@Override
	public void onEnable(){
		this.randomGenerator = new Random();
		log = getLogger();
		colors = Arrays.asList(
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
				ChatColor.WHITE
		);
		setupDatabase();

		// Event listener
		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void playerJoin(PlayerJoinEvent event) {

				Player player = event.getPlayer();
				// Checking for db entry, if so use that color
				UserColor userColor = getUserColor(player);
				ChatColor color;
				if (userColor != null) {
					color = userColor.getChatColor();
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

	private void setupDatabase() {
		try {
			EbeanServer db = getDatabase();
			for (Class<?> c : getDatabaseClasses())
				db.find(c).findRowCount();
		} catch (Exception e) {
			log.info("Initializing database tables.");
			installDDL();
		}
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
		log.fine("enabled");
		return false;
	}

	@Override
	public void onDisable() {

	}

	private UserColor getUserColor(Player player) {
		return getDatabase().find(UserColor.class).where().ieq("playerName", player.getName()).findUnique();
	}

	private void saveColor(Player player, ChatColor color) {
		log.fine("Saving color");
		UserColor userColor = getUserColor(player);
		if (userColor == null) {
			userColor = new UserColor();
			userColor.setPlayer(player);
		}
		userColor.setChatColor(color);
		getDatabase().save(userColor);
		setColor(player, color);
	}

	private void setColor(Player player, ChatColor color) {
		player.setDisplayName(color + player.getName() + ChatColor.WHITE);

	}
	private ChatColor randomColor() {
		int index = randomGenerator.nextInt(colors.size());
		return colors.get(index);

	}
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(UserColor.class);
		return list;
	}
}

