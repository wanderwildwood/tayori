package com.fsck.k9.ui.messageview;


import android.net.Uri;

import com.fsck.k9.mailstore.AttachmentViewInfo;


public interface AttachmentViewCallback {
    void onViewAttachment(AttachmentViewInfo attachment);
    void onSaveAttachment(AttachmentViewInfo attachment);

    /** Saves a picture from a web address; the reader chooses where it goes. */
    default void onSaveRemoteImage(Uri imageUri) {
    }

    /** Sends a boarding pass or ticket to Wallet; offered only where Wallet can take it. */
    default void onAddToWallet(AttachmentViewInfo attachment) {
    }
}
