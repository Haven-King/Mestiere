package dev.hephaestus.mestiere.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static dev.hephaestus.mestiere.Mestiere.newID;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;

public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(Mestiere.MOD_ID)
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("action", word()).suggests(CompletionProvider.CMDS)
                .then(CommandManager.argument("players", EntityArgumentType.players())
                    .then(CommandManager.argument("skill_id", identifier()).suggests(CompletionProvider.SKILLS)
                        .then(CommandManager.argument("amount", integer())
                                .executes(ctx -> execute(ctx.getSource(), getString(ctx, "action").toLowerCase(), getPlayers(ctx, "players"), getIdentifier(ctx, "skill_id"), getInteger(ctx, "amount"))))
                        .executes(ctx -> execute(ctx.getSource(), getString(ctx, "action").toLowerCase(), getPlayers(ctx, "players"), getIdentifier(ctx, "skill_id"), 0)))
                ))
        );
    }

    private static int execute(ServerCommandSource source, String cmd, Collection<ServerPlayerEntity> targets, Identifier skill, int amount) throws CommandSyntaxException {
        if (skill.toString().contains("*")) {
            for (Skill s : Mestiere.SKILLS) {
                execute(source, cmd, targets, s.id, amount);
            }

            return 1;
        }

        Skill s = Mestiere.SKILLS.get(skill);

        if (s == Skills.NONE) {
            throw new SimpleCommandExceptionType(new LiteralText("Invalid skill: " + skill.toString())).create();
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
            Mestiere.SKILLS.forEach(skill -> builder.suggest(skill.id.toString(), new LiteralText(skill.name)));
            builder.suggest("*");
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
