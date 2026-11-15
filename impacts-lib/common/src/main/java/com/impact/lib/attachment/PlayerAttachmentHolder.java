package com.impact.lib.attachment;

import java.util.Map;

/** Implemented on every player by Lib's mixin: the player's attachment data, keyed by attachment ID. */
public interface PlayerAttachmentHolder {
    Map<String, PlayerAttachmentData> impactslib$attachments();
}
