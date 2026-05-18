package cn.rongcloud.im.ui.activity;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SealTalkDebugTestActivityTest {
    @Test
    public void defaultQuoteV2WhiteListIncludesGifMessage() throws Exception {
        Method method =
                SealTalkDebugTestActivity.class.getDeclaredMethod("getDefaultQuoteV2WhiteList");
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> whiteList = (List<String>) method.invoke(null);

        assertTrue(whiteList.contains("RC:GIFMsg"));
    }

    @Test
    public void legacyDefaultQuoteV2WhiteListMigratesToGifMessage() throws Exception {
        Method method =
                SealTalkDebugTestActivity.class.getDeclaredMethod(
                        "normalizeQuoteV2WhiteList", List.class);
        method.setAccessible(true);
        List<String> legacyDefault =
                Arrays.asList(
                        "RC:TxtMsg",
                        "RC:ImgMsg",
                        "RC:SightMsg",
                        "RC:VcMsg",
                        "RC:HQVCMsg",
                        "RC:FileMsg",
                        "RC:LBSMsg");

        @SuppressWarnings("unchecked")
        List<String> whiteList = (List<String>) method.invoke(null, legacyDefault);

        assertTrue(whiteList.contains("RC:GIFMsg"));
    }

    @Test
    public void olderLegacyDefaultQuoteV2WhiteListMigratesToHighQualityVoice() throws Exception {
        Method method =
                SealTalkDebugTestActivity.class.getDeclaredMethod(
                        "normalizeQuoteV2WhiteList", List.class);
        method.setAccessible(true);
        List<String> olderLegacyDefault =
                Arrays.asList(
                        "RC:TxtMsg",
                        "RC:ImgMsg",
                        "RC:SightMsg",
                        "RC:VcMsg",
                        "RC:FileMsg",
                        "RC:LBSMsg");

        @SuppressWarnings("unchecked")
        List<String> whiteList = (List<String>) method.invoke(null, olderLegacyDefault);

        assertTrue(whiteList.contains("RC:HQVCMsg"));
    }
}
