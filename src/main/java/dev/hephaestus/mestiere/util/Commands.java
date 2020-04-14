package dev.hephaestus.mestiere.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillPerk;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static dev.hephaestus.mestiere.Mestiere.newID;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;

public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(Mestiere.MOD_ID)
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("action", word()).suggests(CompletionProvider.CMDS)
                .then(CommandManager.argument("players", EntityArgumentType.players())
                    .then(CommandManager.argument("skill_id", string()).suggests(CompletionProvider.SKILLS)
                        .then(CommandManager.argument("amount", integer())
                            .executes(ctx -> execute(ctx.getSource(), getString(ctx, "action").toLowerCase(), getPlayers(ctx, "players"), getString(ctx, "skill_id"), getInteger(ctx, "amount"))))
                        .executes(ctx -> execute(ctx.getSource(), getString(ctx, "action").toLowerCase(), getPlayers(ctx, "players"), getString(ctx, "skill_id"), 0)))
                ))
        );

        dispatcher.register(CommandManager.literal("skill")
            .then(CommandManager.argument("skill_id", string()).suggests(CompletionProvider.SKILLS)
                .executes(ctx -> info(ctx, getString(ctx, "skill_id")))
        ));
    }

    private static int info(CommandContext<ServerCommandSource> source, String skill) throws CommandSyntaxException {
        Skill s = Mestiere.SKILLS.get(skill.contains(":") ? new Identifier(skill) : Mestiere.newID(skill));

        if (s == Skills.NONE) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid skill: " + skill)).create();
        }

        List<SkillPerk> data = Mestiere.PERKS.get(s);
        data.sort(Comparator.comparing((p) -> p.level));

        ServerPlayerEntity player = source.getSource().getPlayer();

        MestiereComponent component = Mestiere.COMPONENT.get(player);

        LiteralText progress = new LiteralText(" - " + component.getLevel(s) + " (" + component.getXp(s) + "/" + component.getXp(component.getLevel(s)+1) + ")");
        progress.setStyle(new Style().setColor(Formatting.WHITE).setBold(false));

        player.sendChatMessage(s.getText(Mestiere.KEY_TYPE.NAME).setStyle(s.getText(Mestiere.KEY_TYPE.NAME).getStyle().deepCopy().setBold(true)).append(progress), MessageType.CHAT);

        for (SkillPerk p : data) {
            boolean unlocked = component.getLevel(p.skill) >= p.level;
            player.sendChatMessage(p.getText(Mestiere.KEY_TYPE.NAME).formatted(unlocked ? Formatting.WHITE : Formatting.DARK_GRAY).append(" - ").append(new LiteralText(p.level + "").formatted(unlocked ? Formatting.WHITE : Formatting.RED)), MessageType.CHAT);
            player.sendChatMessage(p.getText(Mestiere.KEY_TYPE.DESCRIPTION), MessageType.CHAT);
            player.sendChatMessage(new LiteralText(""), MessageType.CHAT);
        }

        return 1;
    }

    private static int execute(ServerCommandSource source, String cmd, Collection<ServerPlayerEntity> targets, String skill, int amount) throws CommandSyntaxException {
        Skill s = Mestiere.SKILLS.get(skill.contains(":") ? new Identifier(skill) : Mestiere.newID(skill));

        if (s == Skills.NONE) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid skill: " + skill)).create();
        }

        for(ServerPlayerEntity p : targets) {
            switch (cmd) {
                case "set":
                    Mestiere.COMPONENT.get(p).setXp(s, amount);
                    break;

                case "add":
                    Mestiere.COMPONENT.get(p).addXp(s, amount);
                    break;

                case "clear":
                    Mestiere.COMPONENT.get(p).setXp(s, 0);
                    break;

                default:
                    throw new SimpleCommandExceptionType(new LiteralText("Command not found: " + cmd)).create();
            }
        }

        return 1;
    }

    private static class CompletionProvider {
        public static final SuggestionProvider<ServerCommandSource> SKILLS = SuggestionProviders.register(newID("skills"), (ctx, builder) -> {
            Mestiere.SKILLS.forEach(skill -> builder.suggest(skill.id.getPath(), skill.getText(Mestiere.KEY_TYPE.NAME)));
            return builder.buildFuture();
        });

        public static final SuggestionProvider<ServerCommandSource> CMDS = SuggestionProviders.register(newID("commands"), (ctx, builder) -> {
            builder.suggest("set");
            builder.suggest("add");
            builder.suggest("clear");
            return builder.buildFuture();
        });
    }
}
