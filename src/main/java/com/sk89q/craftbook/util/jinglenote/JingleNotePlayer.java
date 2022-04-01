// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server Copyright (C) 2010 sk89q <http://www.sk89q.com> All rights reserved.
 */

package com.sk89q.craftbook.util.jinglenote;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.craftbook.util.jinglenote.JingleSequencer.Note;

public abstract class JingleNotePlayer implements Runnable {

    protected final String player;
    private JingleSequencer sequencer;
    protected SearchArea area;

    /**
     * Constructs a new JingleNotePlayer
     * 
     * @param player The player who is hearing this's name.
     * @param seq The JingleSequencer to play.
     * @param area The SearchArea for this player. (optional)
     */
    public JingleNotePlayer(String player, JingleSequencer seq, SearchArea area) {

        this.player = player;
        sequencer = seq;
        this.area = area;
    }

    @Override
    public void run() {

        if(sequencer == null)
            return;
        try {
            try {
                sequencer.play(this);
            } catch (Throwable t) {
                CraftBookBukkitUtil.printStacktrace(t);
            }

            while(isPlaying()){
                Thread.sleep(10L);
            }
        } catch (InterruptedException e) {
            CraftBookBukkitUtil.printStacktrace(e);
        } finally {
            CraftBookPlugin.logDebugMessage("Finished playing for: " + player, "midi.stop");
            stop();
        }
    }

    public boolean isPlaying() {

        return sequencer != null && (sequencer.isPlaying() || !sequencer.hasPlayedBefore());
    }

    public String getPlayer() {

        return player;
    }

    public void stop() {

        if (sequencer != null) {
            sequencer.stop(this);
            sequencer = null;
        }
    }

    public abstract void play(Note note);
}