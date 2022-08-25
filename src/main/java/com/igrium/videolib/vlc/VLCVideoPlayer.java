package com.igrium.videolib.vlc;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;

import javax.annotation.Nullable;

import com.igrium.videolib.api.VideoPlayer;
import com.igrium.videolib.api.playback.ControlsInterface;
import com.igrium.videolib.api.playback.MediaInterface;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCVideoPlayer implements VideoPlayer {
    protected final Identifier id;
    protected final VLCVideoManager manager;

    protected EmbeddedMediaPlayer mediaPlayer;
    protected OpenGLVideoSurface surface;

    protected VLCMediaInterface mediaInterface = new VLCMediaInterface();
    protected VLCControlsInterface controlsInterface = new VLCControlsInterface();

    private boolean textureRegistered = false;

    @Nullable
    private VLCVideoHandle currentMedia;
    

    protected VLCVideoPlayer(Identifier id, VLCVideoManager manager) {
        this.id = id;
        this.manager = manager;
    }

    protected void init() {
        if (mediaPlayer != null) return;

        mediaPlayer = manager.getFactory().mediaPlayers().newEmbeddedMediaPlayer();
        surface = new OpenGLVideoSurface();
        mediaPlayer.videoSurface().set(surface);
    }

    public final Identifier getId() {
        return id;
    }
    
    public Identifier getTexture() {
        Identifier texId = VideoPlayer.getTextureId(getId());
        if (!textureRegistered) registerTexture(texId);
        return texId;
    }

    // We can't do this in constructor because the texture manager isn't created yet.
    protected void registerTexture(Identifier texId) {
        if (textureRegistered) return;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> registerTexture(texId));
            return;
        }

        MinecraftClient.getInstance().getTextureManager().registerTexture(texId, surface.getTexture());
        textureRegistered = true;
    }


    @Override
    public void close() {
        getControlsInterface().stop();
        surface.texture.close();        
    }

    @Override
    public MediaInterface<VLCVideoHandle> getMediaInterface() {
        return mediaInterface;
    }

    @Override
    public ControlsInterface getControlsInterface() {
        return controlsInterface;
    }

    public class VLCMediaInterface implements MediaInterface<VLCVideoHandle> {

        @Override
        public boolean load(VLCVideoHandle handle) {
            currentMedia = handle;
            return mediaPlayer.media().prepare(handle.getMrl());
        }

        @Override
        public boolean play(VLCVideoHandle handle) {
            currentMedia = handle;
            return mediaPlayer.media().play(handle.getMrl());
        }

        @Override
        public boolean hasMedia() {
            return currentMedia != null;
        }

        @Override
        public Optional<VLCVideoHandle> currentMedia() {
            return Optional.ofNullable(currentMedia);
        }

        @Override
        public VLCVideoHandle getHandle(Identifier id) {
            return manager.getHandle(id);
        }

        @Override
        public VLCVideoHandle getHandle(URI uri) throws MalformedURLException {
            return new VLCUtils.VLCUrlHandle(uri);
        }
    }

    public class VLCControlsInterface implements ControlsInterface {

        @Override
        public void play() {
            mediaPlayer.controls().play();           
        }

        @Override
        public void stop() {
            mediaPlayer.controls().stop();
            currentMedia = null;
        }

        @Override
        public void setPause(boolean pause) {
            mediaPlayer.controls().setPause(pause);
        }

        @Override
        public void setTime(long time) {
            mediaPlayer.controls().setTime(time);
        }

        @Override
        public long getTime() {
            return mediaPlayer.status().time();
        }

        @Override
        public long getLength() {
            return mediaPlayer.status().length();
        }

        @Override
        public void setRepeat(boolean repeat) {
            mediaPlayer.controls().setRepeat(repeat);
        }

        @Override
        public boolean repeat() {
            return mediaPlayer.controls().getRepeat();
        }
        
    }
}
