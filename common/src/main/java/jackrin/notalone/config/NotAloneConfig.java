package jackrin.notalone.config;

import jackrin.notalone.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NotAloneConfig {

    public static final String FILE_NAME = "notalone.toml";

    // [general]
    public static int markedPlayerDurationSeconds = 1200;

    // [herobrine]
    public static boolean herobrineEnabled = true;
    public static int herobrineRarity = 2000;
    public static int herobrineMinDistance = 48;
    public static int herobrineMaxDistance = 96;
    public static int herobrineMinDistanceForest = 24;
    public static int herobrineMaxDistanceForest = 96;

    // [footsteps]
    public static boolean footstepsEnabled = true;
    public static int footstepsRarity = 4000;

    // [animal_possession]
    public static boolean possessionEnabled = true;
    public static int possessionRarity = 2000;
    public static int possessionDurationSeconds = 180;

    private static boolean missingKeys = false;

    private NotAloneConfig() {
    }

    public static long markedPlayerDurationTicks() {
        return markedPlayerDurationSeconds * 20L;
    }

    public static long possessionDurationTicks() {
        return possessionDurationSeconds * 20L;
    }

    // Called once during mod construction, on both loaders.
    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);

        try {
            if (!Files.exists(file)) {
                Files.createDirectories(configDir);
                write(file);
                Constants.LOG.info("Created default config at {}", file);
                return;
            }

            missingKeys = false;
            Map<String, String> values = parse(Files.readAllLines(file, StandardCharsets.UTF_8));
            apply(values);

            // Older files won't have the newer keys, so rewrite to add them.
            if (missingKeys) {
                write(file);
                Constants.LOG.info("Updated {} with options added in this version", FILE_NAME);
            }
        } catch (IOException e) {
            Constants.LOG.error("Could not read {}, falling back to defaults", FILE_NAME, e);
        }
    }

    private static void apply(Map<String, String> values) {
        markedPlayerDurationSeconds = readInt(values, "general.marked_player_duration_seconds",
                markedPlayerDurationSeconds, 1, Integer.MAX_VALUE);

        herobrineEnabled = readBool(values, "herobrine.enabled", herobrineEnabled);
        herobrineRarity = readInt(values, "herobrine.rarity", herobrineRarity, 0, Integer.MAX_VALUE);
        herobrineMinDistance = readInt(values, "herobrine.min_distance", herobrineMinDistance, 1, 512);
        herobrineMaxDistance = readInt(values, "herobrine.max_distance", herobrineMaxDistance, 1, 512);
        herobrineMinDistanceForest = readInt(values, "herobrine.min_distance_forest",
                herobrineMinDistanceForest, 1, 512);
        herobrineMaxDistanceForest = readInt(values, "herobrine.max_distance_forest",
                herobrineMaxDistanceForest, 1, 512);

        footstepsEnabled = readBool(values, "footsteps.enabled", footstepsEnabled);
        footstepsRarity = readInt(values, "footsteps.rarity", footstepsRarity, 0, Integer.MAX_VALUE);

        possessionEnabled = readBool(values, "animal_possession.enabled", possessionEnabled);
        possessionRarity = readInt(values, "animal_possession.rarity", possessionRarity, 0, Integer.MAX_VALUE);
        possessionDurationSeconds = readInt(values, "animal_possession.duration_seconds",
                possessionDurationSeconds, 1, Integer.MAX_VALUE);

        // min > max would never find a spawn, so swap rather than fail silently.
        if (herobrineMinDistance > herobrineMaxDistance) {
            Constants.LOG.warn("herobrine.min_distance ({}) is greater than max_distance ({}), swapping them",
                    herobrineMinDistance, herobrineMaxDistance);
            int swap = herobrineMinDistance;
            herobrineMinDistance = herobrineMaxDistance;
            herobrineMaxDistance = swap;
        }
        if (herobrineMinDistanceForest > herobrineMaxDistanceForest) {
            Constants.LOG.warn("herobrine.min_distance_forest ({}) is greater than max_distance_forest ({}), swapping them",
                    herobrineMinDistanceForest, herobrineMaxDistanceForest);
            int swap = herobrineMinDistanceForest;
            herobrineMinDistanceForest = herobrineMaxDistanceForest;
            herobrineMaxDistanceForest = swap;
        }
    }

    private static Map<String, String> parse(List<String> lines) {
        Map<String, String> values = new LinkedHashMap<>();
        String section = "";

        for (String raw : lines) {
            // Nothing in this file is a quoted string, so '#' always starts a comment.
            int comment = raw.indexOf('#');
            String line = (comment >= 0 ? raw.substring(0, comment) : raw).trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
                continue;
            }

            int equals = line.indexOf('=');
            if (equals < 0) {
                Constants.LOG.warn("Ignoring unreadable line in {}: {}", FILE_NAME, line);
                continue;
            }

            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();
            if (!key.isEmpty()) {
                values.put(section.isEmpty() ? key : section + "." + key, value);
            }
        }

        return values;
    }

    private static boolean readBool(Map<String, String> values, String key, boolean fallback) {
        String value = values.get(key);
        if (value == null) {
            missingKeys = true;
            return fallback;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        Constants.LOG.warn("{} should be true or false but was '{}', using {}", key, value, fallback);
        return fallback;
    }

    private static int readInt(Map<String, String> values, String key, int fallback, int min, int max) {
        String value = values.get(key);
        if (value == null) {
            missingKeys = true;
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                int clamped = Math.max(min, Math.min(max, parsed));
                Constants.LOG.warn("{} was {}, outside the allowed range {}..{}, using {}",
                        key, parsed, min, max, clamped);
                return clamped;
            }
            return parsed;
        } catch (NumberFormatException e) {
            Constants.LOG.warn("{} should be a whole number but was '{}', using {}", key, value, fallback);
            return fallback;
        }
    }

    private static void write(Path file) throws IOException {
        String contents = """
                # NotAlone config.
                # Everything here is server-side. On a multiplayer server only the server's copy
                # matters, though the mod still has to be installed on both sides.
                # Delete this file to reset it.

                [general]

                # How long a player stays marked before a new one is picked, in seconds.
                # Default 1200 (20 min).
                marked_player_duration_seconds = %d

                [herobrine]

                # Shows up in the distance and follows the marked player. Look at him and he's gone.
                enabled = %b

                # One-in-N per tick, 20 ticks a second. Bigger means rarer, 0 is off.
                # Default 2000.
                rarity = %d

                # How far away he can show up, in blocks. Defaults 48 and 96.
                min_distance = %d
                max_distance = %d

                # Same in forests, where trees get in the way. Defaults 24 and 96.
                min_distance_forest = %d
                max_distance_forest = %d

                [footsteps]

                # Footsteps behind the marked player. Uses the sound of whatever block they cross.
                enabled = %b

                # One-in-N per tick. Bigger means rarer, 0 is off. Default 4000.
                rarity = %d

                [animal_possession]

                # A sheep, pig or cow nearby gets white eyes and stares at the marked player.
                # Looking at it puts it back to normal.
                enabled = %b

                # One-in-N per tick. Bigger means rarer, 0 is off. Default 2000.
                rarity = %d

                # How long it stays possessed if nobody looks, in seconds. Default 180 (3 min).
                duration_seconds = %d
                """.formatted(
                markedPlayerDurationSeconds,
                herobrineEnabled, herobrineRarity,
                herobrineMinDistance, herobrineMaxDistance,
                herobrineMinDistanceForest, herobrineMaxDistanceForest,
                footstepsEnabled, footstepsRarity,
                possessionEnabled, possessionRarity, possessionDurationSeconds);

        Files.writeString(file, contents, StandardCharsets.UTF_8);
    }
}
