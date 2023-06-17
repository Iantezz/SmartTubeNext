package com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers;

import com.liskovsoft.mediaserviceinterfaces.CommentsService;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemMetadata;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.sharedutils.rx.RxHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.PlayerEventListenerHelper;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.controllers.SuggestionsController.MetadataListener;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.CommentsReceiver;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.CommentsReceiverImpl;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import com.liskovsoft.youtubeapi.service.YouTubeMediaService;
import io.reactivex.disposables.Disposable;

public class CommentsController extends PlayerEventListenerHelper implements MetadataListener {
    private static final String TAG = CommentsController.class.getSimpleName();
    private CommentsService mCommentsService;
    private Disposable mCommentsAction;
    private String mLiveChatKey;
    private String mCommentsKey;

    @Override
    public void onInit() {
        mCommentsService = YouTubeMediaService.instance().getCommentsService();
    }

    @Override
    public void onMetadata(MediaItemMetadata metadata) {
        mLiveChatKey = metadata != null && metadata.getLiveChatKey() != null ? metadata.getLiveChatKey() : null;
        mCommentsKey = metadata != null && metadata.getCommentsKey() != null ? metadata.getCommentsKey() : null;
    }

    private void openCommentsDialog() {
        disposeActions();

        if (mCommentsKey == null) {
            return;
        }

        getPlayer().showControls(false);

        String title = getPlayer().getVideo().getTitle();

        CommentsReceiver commentsReceiver = new CommentsReceiverImpl(getActivity()) {
            @Override
            public void onLoadMore(String nextCommentsKey) {
                loadComments(this, nextCommentsKey);
            }

            @Override
            public void onStart() {
                loadComments(this, mCommentsKey);
            }

            @Override
            public void onCommentClicked(String nestedCommentsKey) {
                if (nestedCommentsKey == null) {
                    return;
                }

                CommentsReceiver nestedReceiver = new CommentsReceiverImpl(getActivity()) {
                    @Override
                    public void onLoadMore(String nextCommentsKey) {
                        loadComments(this, nextCommentsKey);
                    }

                    @Override
                    public void onStart() {
                        loadComments(this, nestedCommentsKey);
                    }
                };

                showDialog(nestedReceiver, title);
            }
        };

        showDialog(commentsReceiver, title);
    }

    @Override
    public void onChatClicked(boolean enabled) {
        if (mCommentsKey != null && mLiveChatKey == null) {
            openCommentsDialog();
        }
    }

    @Override
    public void onEngineReleased() {
        disposeActions();
    }

    @Override
    public void onFinish() {
        disposeActions();
    }

    private void disposeActions() {
        RxHelper.disposeActions(mCommentsAction);
    }

    private void loadComments(CommentsReceiver receiver, String commentsKey) {
        disposeActions();

        mCommentsAction = mCommentsService.getCommentsObserve(commentsKey)
                .subscribe(
                        receiver::addCommentGroup,
                        error -> {
                            Log.e(TAG, error.getMessage());
                            receiver.addCommentGroup(null); // remove loading message
                        }
                );
    }

    private void showDialog(CommentsReceiver receiver, String title) {
        AppDialogPresenter appDialogPresenter = AppDialogPresenter.instance(getActivity());

        appDialogPresenter.appendCommentsCategory(title, UiOptionItem.from(title, receiver));
        appDialogPresenter.showDialog();
    }
}