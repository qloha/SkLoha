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

        if (movement == Movement.TELEPORT || intervalTicks == 0L) {
            // simple teleport sequence using a scheduled task to avoid blocking
            BukkitRunnable task = new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        running.remove(player.getUniqueId());
                        return;
                    }
                    if (index >= waypoints.size()) {
                        cancel();
                        running.remove(player.getUniqueId());
                        return;
                    }
                    player.teleport(waypoints.get(index));
                    index++;
                }
            };
            running.put(player.getUniqueId(), task);
            task.runTaskTimer(SkLoha.getInstance(), 0L, Math.max(1L, intervalTicks));
            return true;
        } else if (movement == Movement.GLIDE) {
            // glide between waypoints by interpolating positions per tick
            BukkitRunnable task = new BukkitRunnable() {
                int index = 0;
                BukkitRunnable interp = null;

                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        running.remove(player.getUniqueId());
                        return;
                    }
                    if (index >= waypoints.size() - 1) {
                        // final teleport to last and finish
                        player.teleport(waypoints.get(waypoints.size() - 1));
                        cancel();
                        running.remove(player.getUniqueId());
                        return;
                    }
                    Location from = player.getLocation();
                    Location to = waypoints.get(index + 1);
                    long ticks = Math.max(1L, intervalTicks);
                    // start interpolation task
                    interp = new BukkitRunnable() {
                        long tick = 0;

                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                cancel();
                                return;
                            }
                            double progress = (double) tick / (double) ticks;
                            if (progress >= 1.0) {
                                player.teleport(to);
                                cancel();
                                return;
                            }
                            double x = from.getX() + (to.getX() - from.getX()) * progress;
                            double y = from.getY() + (to.getY() - from.getY()) * progress;
                            double z = from.getZ() + (to.getZ() - from.getZ()) * progress;
                            Location cur = new Location(from.getWorld(), x, y, z, from.getYaw(), from.getPitch());
                            player.teleport(cur);
                            tick++;
                        }
                    };
                    interp.runTaskTimer(SkLoha.getInstance(), 0L, 1L);
                    index++;
                }
            };
            running.put(player.getUniqueId(), task);
            task.runTaskTimer(SkLoha.getInstance(), 0L, Math.max(1L, intervalTicks));
            return true;
        }
        return false;
    }
}
