package com.igrium.videolib.vlc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.igrium.videolib.api.VideoManager;
import com.igrium.videolib.util.FileVideoLoader;
import com.igrium.videolib.vlc.VLCUtils.VLCFileHandle;

import net.minecraft.util.Identifier;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.NativeLibraryMappingException;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

/**
 * A media playback implementation that relies on VLCJ.
 */
public class VLCVideoManager implements VideoManager {
    public static final Set<String> EXTENSIONS = ImmutableSet.of("mp4", "webm", "avi", "mov", "mpeg");

    private MediaPlayerFactory factory;
    private Logger LOGGER = LogManager.getLogger();

    private final Map<Identifier, VLCVideoPlayer> videoPlayers = new HashMap<>();
    private final Map<Identifier, VLCVideoHandle> videos = new HashMap<>();
    
    protected FileVideoLoader<VLCVideoHandle> loader = new FileVideoLoader<>(
            EXTENSIONS::contains, VLCFileHandle::new, videos::putAll);

    public VLCVideoManager() {
        try {
            factory = new MediaPlayerFactory(new NativeDiscovery());
        } catch (NativeLibraryMappingException e) {
            LOGGER.error("Unable to load VLC natives! ", e);
        }
    }

    public boolean hasNatives() {
        return factory != null;
    }

    @Nullable
    public VLCVideoPlayer getPlayer(Identifier id) {
        return videoPlayers.get(id);
    }

    public VLCVideoPlayer getOrCreate(Identifier id) {
        VLCVideoPlayer player = getPlayer(id);
        if (player == null) {
            player = createPlayer(id);
        }
        return player;
    }

    protected VLCVideoPlayer createPlayer(Identifier id) {
        VLCVideoPlayer player = new VLCVideoPlayer(id, this);
        videoPlayers.put(id, player);
        player.init();
        return player;
    }

    public Map<Identifier, VLCVideoHandle> getVideos() {
        return videos;
    }

    public VLCVideoHandle getHandle(Identifier id) {
        return videos.get(id);
    }

    public MediaPlayerFactory getFactory() {
        if (!hasNatives()) {
            throw new IllegalStateException("No natives!");
        }
        return factory;
    }

    @Override
    public FileVideoLoader<? extends VLCVideoHandle> getReloadListener() {
        return loader;
    }

    @Override
    public Collection<String> supportedExtensions() {
        return EXTENSIONS;
    }

    @Override
    public boolean closePlayer(Identifier id) {
        VLCVideoPlayer player = getPlayer(id);
        if (player == null) return false;
        player.close();
        videoPlayers.remove(id);
        return true;
    }

    @Override
    public void close() {
        for (VLCVideoPlayer player : videoPlayers.values()) {
            player.close();
        }
    }

}
