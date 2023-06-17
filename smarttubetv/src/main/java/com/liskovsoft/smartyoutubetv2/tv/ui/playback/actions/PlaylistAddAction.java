package com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions;

import android.content.Context;
import com.liskovsoft.smartyoutubetv2.tv.R;

public class PlaylistAddAction extends TwoStateAction {
    public PlaylistAddAction(Context context) {
        super(context, R.id.action_playlist_add, R.drawable.action_playlist_add);

        String[] labels = new String[2];
        // Note, labels denote the action taken when clicked
        labels[INDEX_OFF] = context.getString(R.string.action_playlist_add);
        labels[INDEX_ON] = context.getString(R.string.action_playlist_remove);
        setLabels(labels);
    }
}
