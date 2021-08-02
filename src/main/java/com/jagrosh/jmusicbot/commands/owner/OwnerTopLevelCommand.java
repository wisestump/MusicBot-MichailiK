/*
 * Copyright 2021 John Grosh <john.a.grosh@gmail.com>.
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

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class OwnerTopLevelCommand extends OwnerCommand
{
    public OwnerTopLevelCommand(Bot bot)
    {
        this.name = "owner";
        this.help = "owner commands";

        this.children = new OwnerCommand[]
                {
                        new AutoplaylistCmd(bot),
                        new DebugCmd(bot),
                        new EvalCmd(bot),

                        new PlaylistCmd.MakelistCmd(bot),
                        new PlaylistCmd.DeletelistCmd(bot),
                        new PlaylistCmd.AppendlistCmd(bot),
                        new PlaylistCmd.DefaultlistCmd(bot),
                        new PlaylistCmd.ListCmd(bot),

                        new SetavatarCmd(bot),

                        new SetgameCmd.SetnoneCmd(),
                        new SetgameCmd.SetplayingCmd(),
                        new SetgameCmd.SetstreamCmd(),
                        new SetgameCmd.SetlistenCmd(),
                        new SetgameCmd.SetwatchCmd(),

                        new SetnameCmd(bot),
                        new SetstatusCmd(bot),
                        new ShutdownCmd(bot)
                };
    }
}
