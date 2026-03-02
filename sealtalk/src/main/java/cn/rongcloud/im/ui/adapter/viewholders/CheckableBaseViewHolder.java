package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import io.rong.imkit.config.IMKitThemeManager;

public class CheckableBaseViewHolder<T> extends BaseViewHolder<T> {

    public CheckableBaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void update(T t) {
        // 继承刷新UI
    }

    public void updateCheck(ImageView checkBox, CheckType checkType) {
        switch (checkType) {
            case NONE:
                checkBox.setImageResource(
                        IMKitThemeManager.getAttrResId(
                                checkBox.getContext(),
                                io.rong.imkit.R.attr.rc_group_member_unselect_img));
                break;
            case CHECKED:
                checkBox.setImageResource(
                        IMKitThemeManager.getAttrResId(
                                checkBox.getContext(),
                                io.rong.imkit.R.attr.rc_group_member_select_img));
                break;
            case DISABLE:
                checkBox.setImageResource(
                        IMKitThemeManager.getAttrResId(
                                checkBox.getContext(),
                                io.rong.imkit.R.attr.rc_group_member_disable_select_img));
                break;
            default:
                break;
        }
    }
}
