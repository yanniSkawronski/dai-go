package ch.heigvd.daigo.commands;

import picocli.CommandLine;

@CommandLine.Command(
    description = "A small game to experiment with TCP.",
    version = "1.0.0",
    subcommands = {
      ClientCommand.class,
      ServerCommand.class,
    },
    scope = CommandLine.ScopeType.INHERIT,
    mixinStandardHelpOptions = true)
public class Root {}
