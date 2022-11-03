package com.orangemarshall.animations.config;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

public class CommandAnimationsConfig implements ICommand {

    private List aliases = Lists.newArrayList();

    public CommandAnimationsConfig() {
        this.aliases.add("animationsconfig");
        this.aliases.add("animationconfig");
        this.aliases.add("animations");
        this.aliases.add("animation");
        this.aliases.add("oldanimations");
        this.aliases.add("oldanimation");
        this.aliases.add("anconfig");
    }

    public String getCommandName() {
        return (String) this.aliases.get(0);
    }

    public String getCommandUsage(ICommandSender sender) {
        return (String) this.aliases.get(0);
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Config.getInstance().openGui();
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    public int compareTo(ICommand o) {
        return 0;
    }

    public List getCommandAliases() {
        return this.aliases;
    }

    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}
