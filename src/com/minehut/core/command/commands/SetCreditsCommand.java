package com.minehut.core.command.commands;

import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.chat.F;
import com.minehut.commons.common.sound.S;
import com.minehut.core.Core;
import com.minehut.core.command.Command;
import com.minehut.core.player.PlayerInfo;
import com.minehut.core.player.Rank;
import com.minehut.core.util.UUIDFetcher;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Created by luke on 6/1/15.
 */
public class SetCreditsCommand extends Command {

    public SetCreditsCommand(JavaPlugin plugin) {
        super(plugin, "cred", Rank.Admin);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args.size() != 3) {
            player.sendMessage(C.red + "/cred (add/set) (name) (amount)");
            return true;
        }

        UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(args.get(1)));

        Map<String, UUID> response = null;
        try {
            response = fetcher.call();
        } catch (Exception e) {
            F.log("Exception while running UUIDFetcher");
            e.printStackTrace();
        }

        if (response != null) {
            UUID uuid = response.get(args.get(1));

            DBObject query = new BasicDBObject("uuid", uuid.toString());
            DBObject found = Core.getInstance().getPlayersCollection().findOne(query);

            Long amount = Long.parseLong(args.get(2));
            Long oldCredits = (long) found.get("credits");

            long updatedCredits;
            if (args.get(0).equalsIgnoreCase("add")) {
                updatedCredits = (long) oldCredits + amount;
            } else {
                updatedCredits = amount;
            }

            found.put("credits", updatedCredits);

            Core.getInstance().getPlayersCollection().findAndModify(query, found);

            /* update player if on server */
            Player updatedPlayer = Bukkit.getServer().getPlayer(args.get(1));
            if (updatedPlayer != null) {
                Core.getInstance().getPlayerInfo(updatedPlayer).setCredits(updatedCredits);
                updatedPlayer.sendMessage("");
                updatedPlayer.sendMessage(C.green + "Your credits have been set to " + C.aqua + updatedCredits);
                updatedPlayer.sendMessage("");
                S.playSound(updatedPlayer, Sound.LEVEL_UP);
            }

            player.sendMessage(C.green + "Successfully set " + C.aqua + args.get(1) + C.green + "'s credits to " + C.aqua + updatedCredits);
        } else {
            player.sendMessage(C.red + "Couldn't find UUID for " + C.aqua + args.get(0));
        }


        return false;
    }
}
