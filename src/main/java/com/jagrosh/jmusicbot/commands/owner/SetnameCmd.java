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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetnameCmd extends OwnerCommand
{
    public SetnameCmd(Bot bot)
    {
        this.name = "setname";
        this.help = "sets the name of the bot";
        this.arguments = "<name>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "name", "New name to use", true));
    }
    
    @Override
    protected void execute(SlashCommandEvent event)
    {
        try 
        {
            String oldname = event.getJDA().getSelfUser().getName();
            String newname = event.getOption("name").getAsString();

            event.getJDA().getSelfUser().getManager().setName(newname).complete(false);
            event.reply(getClient().getSuccess()+" Name changed from `"+oldname+"` to `"+newname+"`")
                .setEphemeral(true)
                .queue();
        } 
        catch(RateLimitedException e) 
        {
            event.reply(getClient().getError()+" Name can only be changed twice per hour!")
                .setEphemeral(true)
                .queue();
        }
        catch(Exception e) 
        {
            event.reply(getClient().getError()+" That name is not valid!")
                .setEphemeral(true)
                .queue();
        }
    }
}
