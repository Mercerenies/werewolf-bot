
Discord bot to play One-Night Werewolf. Developed by Mercerenies.

Work in progress. More to come here later.

# Building

This project is built using `sbt`, the Scala build tool, which manages
the dependencies for the bot. Simply use `sbt compile` to compile the
project and `sbt run` to run it. `sbt test` can be used to run the
test suite.

## Optional Pandoc Add-on

**Optional:** If you wish to be able to upload text files of game logs
to Discord, you will need to install the `pandoc` command line tool,
available at [https://pandoc.org/](https://pandoc.org/). If this tool
is not on your system path, the feature will be silently disabled.
Note that you may need to restart the bot after installing the tool in
order to enable the feature.
