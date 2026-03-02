package cn.rongcloud.im.newdesign.search;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 搜索类型
 *
 * @author rongcloud
 * @since 5.12.2
 */
public class SearchType {
    /** 搜索好友 */
    public static final int FRIEND = 1;

    /** 搜索群组 */
    public static final int GROUP = 2;

    /** 搜索会话 */
    public static final int CONVERSATION = 4;

    /** 搜索全部（好友、群组、会话） */
    public static final int ALL = FRIEND | GROUP | CONVERSATION;

    @IntDef(
            flag = true,
            value = {FRIEND, GROUP, CONVERSATION, ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    /**
     * 检查是否包含指定类型
     *
     * @param searchTypes 搜索类型组合
     * @param type 要检查的类型
     * @return true 如果包含
     */
    public static boolean contains(@Type int searchTypes, @Type int type) {
        return (searchTypes & type) == type;
    }
}
