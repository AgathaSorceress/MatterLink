package matterlink

import matterlink.api.ApiMessage.Companion.USER_ACTION
import matterlink.config.cfg
import matterlink.handlers.*
import net.minecraft.command.server.CommandBroadcast
import net.minecraft.command.server.CommandEmote
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.AchievementEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

//FORGE-DEPENDENT
@Mod.EventBusSubscriber
object EventHandler {

    //MC-VERSION & FORGE DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun progressEvent(e: AchievementEvent) {
        val achievement = e.achievement
        val entityPlayer = e.entityPlayer as? EntityPlayerMP ?: return
        val statFile = entityPlayer.statFile

        if (!statFile.canUnlockAchievement(achievement) || statFile.hasAchievementUnlocked(achievement)) {
            return
        }
        ProgressHandler.handleProgress(
                name = e.entityPlayer.displayName.unformattedText,
                message = "has earned the achievement",
                display = e.achievement.statName.unformattedText
        )
    }

    //FORGE-DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun chatEvent(e: ServerChatEvent) {
        ChatProcessor.sendToBridge(
                user = e.player.displayName.unformattedText,
                msg = e.message,
                event = ""
        )
    }

    //FORGE-DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun commandEvent(e: CommandEvent) {
        val sender = when {
            e.sender is DedicatedServer -> cfg.outgoing.systemUser
            else -> e.sender.displayName.unformattedText
        }
        val args = e.parameters.joinToString(" ")
        val type = when {
            e.command is CommandEmote -> USER_ACTION
            e.command.name == "me" -> USER_ACTION
            e.command is CommandBroadcast -> ""
            else -> return
        }
        ChatProcessor.sendToBridge(user = sender, msg = args, event = type)

    }

    //FORGE-DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun deathEvent(e: LivingDeathEvent) {
        if (e.entityLiving is EntityPlayer) {
            DeathHandler.handleDeath(
                    player = e.entityLiving.displayName.unformattedText,
                    deathMessage = e.entityLiving.combatTracker.deathMessage.unformattedText,
                    damageType = e.source.damageType
            )
        }
    }

    //FORGE-DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun joinEvent(e: PlayerEvent.PlayerLoggedInEvent) {
        JoinLeaveHandler.handleJoin(e.player.displayName.unformattedText)
    }

    //FORGE-DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun leaveEvent(e: PlayerEvent.PlayerLoggedOutEvent) {
        JoinLeaveHandler.handleLeave(e.player.displayName.unformattedText)
    }

    //FORGE-DEPENDENT
    @SubscribeEvent
    @JvmStatic
    fun serverTickEvent(e: TickEvent.ServerTickEvent) {
        if (e.phase == TickEvent.Phase.END)
            TickHandler.handleTick()
    }
}