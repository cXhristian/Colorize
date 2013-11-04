package com.cxhristian.colorize;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.avaje.ebean.validation.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Entity
@Table(name = "colorize_users")
@UniqueConstraint(columnNames={"player_name"})
public class UserColor {

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

	public Character getColor() {
		return this.color;
	}

	public void setColor(Character color) {
		this.color = color;
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(this.getPlayerName());
	}

	public void setPlayer(Player player) {
		this.setPlayerName(player.getName());
	}

	public ChatColor getChatColor() {
		return ChatColor.getByChar(this.getColor());
	}

	public void setChatColor(ChatColor color) {
		this.setColor(color.getChar());
	}

    @Id
	private String playerName;

    @NotNull
	private Character color;
}
