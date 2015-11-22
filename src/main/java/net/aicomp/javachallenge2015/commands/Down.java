package net.aicomp.javachallenge2015.commands;

import net.aicomp.javachallenge2015.Field;
import net.aicomp.javachallenge2015.Player;
import net.exkazuu.gameaiarena.api.Direction4;

public class Down implements ICommand {

	@Override
	public void doCommand(Player player, Field field) {
		player.move(Direction4.DOWN, field);
	}

	@Override
	public String getValue() {
		return "D";
	}

}