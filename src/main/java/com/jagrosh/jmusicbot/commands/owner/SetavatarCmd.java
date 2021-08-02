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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetavatarCmd extends OwnerCommand 
{
    public SetavatarCmd(Bot bot)
    {
        this.name = "setavatar";
        this.help = "sets the avatar of the bot";
        this.arguments = "<url>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "url", "image url to use", true));
    }
    
    @Override
    protected void execute(SlashCommandEvent event)
    {

        String url = event.getOption("url").getAsString();
        /*if(event.getArgs().isEmpty())
            if(!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage())
                url = event.getMessage().getAttachments().get(0).getUrl();
            else
                url = null;
        else
            url = event.getArgs();*/
        InputStream s = OtherUtil.imageFromUrl(url);
        if(s==null)
        {
            event.reply(getClient().getError()+" Invalid or missing URL").setEphemeral(true).queue();
        }
        else
        {
            try {
            event.getJDA().getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                    v -> event.reply(getClient().getSuccess()+" Successfully changed avatar.").setEphemeral(true).queue(),
                    t -> event.reply(getClient().getError()+" Failed to set avatar.").setEphemeral(true).queue());
            } catch(IOException e) {
                event.reply(getClient().getError()+" Could not load from provided URL.").setEphemeral(true).queue();
            }
        }
    }
}
