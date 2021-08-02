/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetstatusCmd extends OwnerCommand
{
    public SetstatusCmd(Bot bot)
    {
        this.name = "setstatus";
        this.help = "sets the status the bot displays";
        this.arguments = "<status>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "status", "Status to use", true)
                    .addChoice("Online", "Online")
                    .addChoice("Idle", "Idle")
                    .addChoice("Do not disturb", "DND")
                    .addChoice("Invisible", "Invisible")
                );
    }
    
    @Override
    protected void execute(SlashCommandEvent event)
    {
        try {
            String arg = event.getOption("Status").getAsString();


            OnlineStatus status = OnlineStatus.fromKey(arg);
            if(status==OnlineStatus.UNKNOWN)
            {
                event.reply(getClient().getError()+" Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`")
                    .setEphemeral(true)
                    .queue();
            }
            else
            {
                event.getJDA().getPresence().setStatus(status);
                event.reply(getClient().getSuccess()+" Set the status to `"+status.getKey().toUpperCase()+"`")
                    .setEphemeral(true)
                    .queue();
            }
        } catch(Exception e) {
            event.reply(getClient().getError()+" The status could not be set!")
                .setEphemeral(true)
                .queue();
        }
    }
}
