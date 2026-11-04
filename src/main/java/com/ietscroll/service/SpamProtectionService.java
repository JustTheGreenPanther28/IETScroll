package com.ietscroll.service;

import com.ietscroll.general.enums.PrivilegeClass;

/**
 * Checks text content for spam / harmful content.
 * Throws ContentModerationException if content is unsafe.
 */
public interface SpamProtectionService {

    /**
     * Checks the given text using NVIDIA Nemotron Content Safety API.
     * Throws {@link com.ietscroll.exception.ContentModerationException}
     * if the text is flagged as unsafe.
     *
     * @param text the user-supplied text to check
     */
    void checkText(String text);

    /**
     * Consolidated single-batch check for item submissions (name, description, location).
     * Validates zero-emoji policy and combines content into a single AI evaluation to prevent
     * redundant network roundtrips and request timeouts.
     *
     * @param name item name
     * @param description item description
     * @param location item predicted location
     */
    void checkItemSubmission(String name, String description, String location);

    /**
     * Privilege-aware item submission check. The depth of AI moderation
     * scales with the user's trust level:
     * <ul>
     *   <li>{@code TRUSTED}     — emoji check only (skip AI calls)</li>
     *   <li>{@code NORMAL/NEW}  — emoji check + anti-ragging LLM</li>
     *   <li>{@code MAY_BE_SPAM} — full pipeline (emoji + anti-ragging + Nemotron safety)</li>
     * </ul>
     *
     * @param name        item name
     * @param description item description
     * @param location    item predicted location
     * @param privilege   submitting user's privilege class (null treated as NORMAL)
     */
    void checkItemSubmission(String name, String description, String location, PrivilegeClass privilege);
}
