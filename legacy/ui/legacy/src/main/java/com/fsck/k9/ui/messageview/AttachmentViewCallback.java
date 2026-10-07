package com.fsck.k9.ui.messageview;


import com.fsck.k9.mailstore.AttachmentViewInfo;


public interface AttachmentViewCallback {
    void onViewAttachment(AttachmentViewInfo attachment);
    void onSaveAttachment(AttachmentViewInfo attachment);

    /** Sends a boarding pass or ticket to Wallet; offered only where Wallet can take it. */
    default void onAddToWallet(AttachmentViewInfo attachment) {
    }
}
