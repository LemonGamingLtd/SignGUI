# SignGUI [![Maven Central](https://img.shields.io/maven-central/v/io.github.rapha149.signgui/signgui?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.rapha149.signgui/signgui) [![Javadoc](https://javadoc.io/badge2/io.github.rapha149.signgui/signgui/Javadoc.svg)](https://javadoc.io/doc/io.github.rapha149.signgui/signgui) 
An api to get input text via a sign in Minecraft.  
The api supports the Minecraft versions from `1.8` to `1.20.1`.

## Integration

Maven dependency:
```xml
<dependency>
    <groupId>io.github.rapha149.signgui</groupId>
    <artifactId>signgui</artifactId>
    <version>2.1.1</version>
</dependency>
```

## Usage
To open a sign editor gui for a player, do the following:
```java
SignGUI.builder()
    .setLines("§6Line 1", null, "§6Line 3") // set lines
    .setLine(3, "Line 4") // set specific line, starting index is 0
    .setType(Material.DARK_OAK_SIGN) // set the sign type
    .setColor(DyeColor.YELLOW) // set the sign color
    .setHandler((p, result) -> { // set the handler/listener (called when the player finishes editing)
        String line0 = result.getLine(0); // get a speficic line, starting index is 0
        String line1 = result.getLineWithoutColor(1); // get a specific line without color codes
        String[] lines = result.getLines(); // get all lines
        String[] linesWithoutColor = result.getLinesWithoutColor(); // get all lines without color codes

        if (line1.isEmpty()) {
            // The user has not entered anything on line 2, so we open the sign again
            return List.of(SignGUIAction.displayNewLines("§6Line 1", null, "§6Line 3", "Line 4"));
        }

        if (line1.equals("inv")) {
            // close the sign and open an inventory
            return List.of(
                SignGUIAction.openInventory(this, Bukkit.createInventory(player, 27)), // "this" = your JavaPlugin instance
                SignGUIAction.run(() -> player.sendMessage("Inventory opened!"))
            );
        }

        // Just close the sign by not returning any actions
        return Collections.emptyList();
    }).build().open(player);
```

You don't have to call all methods. Only `setHandler` is mandatory.

By default, the handler is called by an asynchronous thread. You can change that behaviour by calling the method `callHandlerSynchronously` of the builder.
An explanation for the different methods can be found on the [Javadoc](https://javadoc.io/doc/io.github.rapha149.signgui/signgui).

## Credits
This project structure was inspired by WesJD's [AnvilGUI](https://github.com/WesJD/AnvilGUI) and I used some code from Cleymax's [SignGUI](https://github.com/Cleymax/SignGUI).
