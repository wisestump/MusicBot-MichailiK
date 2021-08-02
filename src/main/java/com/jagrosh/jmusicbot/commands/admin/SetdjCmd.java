/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.admin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetdjCmd extends AdminCommand
{
    public SetdjCmd(Bot bot)
    {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "[rolename]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.ROLE, "role", "Sets the DJ Role. Leave it empty to remove the DJ role.", false));
    }

    public void doCommand(SlashCommandEvent event)
    {
        OptionMapping role = event.getOption("role");
        Settings s = getClient().getSettingsFor(event.getGuild());

        if(role == null)
        {
            s.setDJRole(null);
            event.reply(getClient().getSuccess()+" DJ role cleared; Only Admins can use the DJ commands.")
                .queue();
        }
        else
        {
            s.setDJRole(role.getAsRole());
            event.reply(getClient().getSuccess()+" DJ commands can now be used by users with the **"+role.getAsRole().getAsMention()+"** role.")
                .queue();
        }
    }
    
}
