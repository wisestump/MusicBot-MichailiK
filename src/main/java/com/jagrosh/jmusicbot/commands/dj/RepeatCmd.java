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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RepeatCmd extends DJCommand
{
    public RepeatCmd(Bot bot)
    {
        super(bot);
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "[on|off]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, "enable", "Set repeat mode toggle. Leave empty to toggle", false));
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        boolean hasValue = event.getOption("Repeat Mode") != null;
        boolean value;

        Settings settings = getClient().getSettingsFor(event.getGuild());
        if(!hasValue)
            value = !settings.getRepeatMode();
        else
            value = event.getOption("Repeat Mode").getAsBoolean();

        settings.setRepeatMode(value);
        event.reply(getClient().getSuccess()+" Repeat mode is now `"+(value ? "ON" : "OFF")+"`").queue();
    }
}
