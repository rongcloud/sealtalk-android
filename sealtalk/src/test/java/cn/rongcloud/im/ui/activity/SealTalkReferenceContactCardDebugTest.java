package cn.rongcloud.im.ui.activity;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class SealTalkReferenceContactCardDebugTest {

    @Test
    public void referenceContactCardCustomContentSwitchDefaultsToDisabled() {
        assertFalse(
                SealTalkDebugTestActivity.DEFAULT_REFERENCE_CONTACT_CARD_CUSTOM_CONTENT_ENABLED);
    }
}
