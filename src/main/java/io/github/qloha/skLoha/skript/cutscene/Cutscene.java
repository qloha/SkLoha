package io.github.qloha.skLoha.skript.cutscene;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.qloha.skLoha.SkLoha;

public class Cutscene {

    private final String name;
    private final List<Location> waypoints = new ArrayList<>();
    private Movement movement = Movement.TELEPORT;
    private long intervalTicks = 20L; // default 1 second

    // track running tasks per-player
    private final java.util.Map<UUID, BukkitRunnable> running = new java.util.HashMap<>();

    public Cutscene(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Location> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(Location loc) {
        if (loc == null) return;
        waypoints.add(loc.clone());
    }

    public Movement getMovement() {
        return movement;
    }

    public void setMovement(Movement movement) {
        if (movement != null) this.movement = movement;
    }

    public long getIntervalTicks() {
        return intervalTicks;
    }

    public void setIntervalSeconds(double seconds) {
        if (seconds <= 0) this.intervalTicks = 0;
        else this.intervalTicks = Math.max(0L, (long) (seconds * 20.0));
    }

    /**
     * Play the cutscene for a player. This will cancel any existing running cutscene for that player.
     */
    public boolean play(Player player) {
        if (player == null || !player.isOnline()) return false;
        if (waypoints.isEmpty()) return false;

        // cancel existing
        BukkitRunnable existing = running.remove(player.getUniqueId());
        if (existing != null) existing.cancel();

        // We'll use a single per-player tick task that handles both TELEPORT and GLIDE modes.
        long ticksForSegment = Math.max(0L, intervalTicks);
        // if intervalTicks == 0, treat as immediate teleport through all points

        BukkitRunnable task = new BukkitRunnable() {
            int index = 0; // current waypoint index we are aiming at or interpolating from
            long progressTick = 0; // ticks progressed within current segment
            boolean started = false;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    running.remove(player.getUniqueId());
                    return;
                }

                int size = waypoints.size();
                if (size == 0) {
                    cancel();
                    running.remove(player.getUniqueId());
                    return;
                }

                // If interval is zero or movement is teleport with interval 0 -> teleport all immediately
                if (ticksForSegment == 0L && movement == Movement.TELEPORT) {
                    for (Location l : waypoints) {
                        if (l != null) player.teleport(l);
                    }
                    cancel();
                    running.remove(player.getUniqueId());
                    return;
                }

                // If not started, position player at first waypoint immediately
                if (!started) {
                    Location first = waypoints.get(0);
                    if (first != null) player.teleport(first);
                    started = true;
                    index = 0;
                    progressTick = 0;
                    // If there's only one waypoint, finish
                    if (size <= 1) {
                        cancel();
                        running.remove(player.getUniqueId());
                        return;
                    }
                }

                if (movement == Movement.TELEPORT) {
                    // Perform teleports every `ticksForSegment` ticks. We'll run this task every tick and count.
                    progressTick++;
                    if (progressTick >= ticksForSegment) {
                        progressTick = 0;
                        index++;
                        if (index >= size) {
                            cancel();
                            running.remove(player.getUniqueId());
                            return;
                        }
                        Location target = waypoints.get(index);
                        if (target != null) player.teleport(target);
                    }
                } else if (movement == Movement.GLIDE) {
                    // Glide from waypoint[index] to waypoint[index+1] over `ticksForSegment` ticks.
                    // Ensure there's a next waypoint
                    if (index >= size - 1) {
                        // reached end
                        cancel();
                        running.remove(player.getUniqueId());
                        return;
                    }
                    Location from = waypoints.get(index);
                    Location to = waypoints.get(index + 1);
                    long segTicks = Math.max(1L, ticksForSegment); // at least 1 tick to interpolate
                    double progress = (double) progressTick / (double) segTicks;
                    if (progress >= 1.0) {
                        // finalize segment
                        if (to != null) player.teleport(to);
                        // move to next segment
                        index++;
                        progressTick = 0;
                        // if we've reached the last waypoint after incrementing, finish
                        if (index >= size - 1) {
                            cancel();
                            running.remove(player.getUniqueId());
                            return;
                        }
                        return; // wait next tick to start interpolating next segment
                    }
                    // interpolate position
                    if (from == null || to == null) {
                        // if invalid, skip to next
                        index++;
                        progressTick = 0;
                        return;
                    }
                    double x = from.getX() + (to.getX() - from.getX()) * progress;
                    double y = from.getY() + (to.getY() - from.getY()) * progress;
                    double z = from.getZ() + (to.getZ() - from.getZ()) * progress;
                    float yaw = (float) (from.getYaw() + (to.getYaw() - from.getYaw()) * progress);
                    float pitch = (float) (from.getPitch() + (to.getPitch() - from.getPitch()) * progress);
                    Location cur = new Location(from.getWorld(), x, y, z, yaw, pitch);
                    player.teleport(cur);
                    progressTick++;
                } else {
                    // Unknown movement, finish
                    cancel();
                    running.remove(player.getUniqueId());
                }
            }
        };

        running.put(player.getUniqueId(), task);
        // schedule task every tick so we can interpolate smoothly; TELEPORT mode uses internal counter
        task.runTaskTimer(SkLoha.getInstance(), 0L, 1L);
        return true;
    }
}
