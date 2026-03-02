package cn.rongcloud.im.im;

import cn.rongcloud.im.im.plugin.CustomAudioPlugin;
import cn.rongcloud.im.im.plugin.CustomVideoPlugin;
import io.rong.callkit.AudioPlugin;
import io.rong.callkit.VideoPlugin;
import io.rong.imkit.conversation.extension.DefaultExtensionConfig;
import io.rong.imkit.conversation.extension.component.plugin.FilePlugin;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imkit.feature.destruct.DestructPlugin;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.sight.SightPlugin;
import java.util.List;

public class SealExtensionConfig extends DefaultExtensionConfig {
    @Override
    public List<IPluginModule> getPluginModules(
            Conversation.ConversationType conversationType, String targetId) {
        List<IPluginModule> pluginList = super.getPluginModules(conversationType, targetId);
        IPluginModule sightPlugin = null,
                filePlugin = null,
                audioPlugin = null,
                videoPlugin = null,
                destructPlugin = null;
        int audioPluginIndex = -1;
        int videoPluginIndex = -1;

        for (int i = 0; i < pluginList.size(); i++) {
            IPluginModule pluginModule = pluginList.get(i);
            if (pluginModule instanceof SightPlugin) {
                sightPlugin = pluginModule;
            } else if (pluginModule instanceof FilePlugin) {
                filePlugin = pluginModule;
            } else if (pluginModule instanceof AudioPlugin) {
                audioPlugin = pluginModule;
                audioPluginIndex = i;
            } else if (pluginModule instanceof VideoPlugin) {
                videoPlugin = pluginModule;
                videoPluginIndex = i;
            } else if (pluginModule instanceof DestructPlugin) {
                destructPlugin = pluginModule;
            }
        }

        // 替换默认的 AudioPlugin 为 CustomAudioPlugin
        if (audioPlugin != null && audioPluginIndex >= 0) {
            pluginList.remove(audioPluginIndex);
            pluginList.add(audioPluginIndex, new CustomAudioPlugin());
            audioPlugin = pluginList.get(audioPluginIndex);
            // 更新 videoPluginIndex，因为可能受到影响
            if (videoPluginIndex > audioPluginIndex) {
                // videoPluginIndex 不变，因为是在同一位置替换
            }
        }

        // 替换默认的 VideoPlugin 为 CustomVideoPlugin
        if (videoPlugin != null && videoPluginIndex >= 0) {
            pluginList.remove(videoPluginIndex);
            pluginList.add(videoPluginIndex, new CustomVideoPlugin());
            videoPlugin = pluginList.get(videoPluginIndex);
        }
        if (sightPlugin != null && pluginList.size() > 1) {
            pluginList.remove(sightPlugin);
            pluginList.add(1, sightPlugin);
        }
        if (filePlugin != null && pluginList.size() > 4) {
            pluginList.remove(filePlugin);
            pluginList.add(3, filePlugin);
        }
        if (targetId.equals(RongIMClient.getInstance().getCurrentUserId())) {
            if (audioPlugin != null) {
                pluginList.remove(audioPlugin);
            }
            if (videoPlugin != null) {
                pluginList.remove(videoPlugin);
            }
            if (destructPlugin != null) {
                pluginList.remove(destructPlugin);
            }
        }
        return pluginList;
    }
}
