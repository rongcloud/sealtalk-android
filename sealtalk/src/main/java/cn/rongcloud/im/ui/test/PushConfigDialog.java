package cn.rongcloud.im.ui.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.rongcloud.im.R;

public class PushConfigDialog extends Dialog {

    public static int TYPE_REMOVE = 0x1456;
    public static int TYPE_GET = 0x1457;
    protected Context mContext;
    protected WindowManager.LayoutParams mLayoutParams;
    private int mType;
    private TextView mTvSure;
    private TextView mTvCancel;
    private EditText etId;
    private EditText etTitle;
    private EditText etContent;
    private EditText etData;
    private EditText etHW;
    private EditText etImageUrlHW;
    private EditText etMi;
    private EditText etImageUrlMi;
    private EditText etOppo;
    private EditText etThreadId;
    private EditText etApnId;
    private EditText edFcm;
    private EditText etChannelIdFcm;
    private EditText edTemplateId;
    private EditText edImageUrl;
    private EditText edCategory;
    private EditText edRichMediaUri;
    private EditText edInterruptionLevel;

    private CheckBox cbVivo;
    private CheckBox cbDisableTitle;
    private CheckBox cbForceDetail;

    private EditText etOhosCategory;
    private EditText etOhosImageUrl;

    private LinearLayout llValue;
    private LinearLayout llCheck;
    private LinearLayout llExtra;
    private EditText etHWImportance;

    public PushConfigDialog(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(R.drawable.transparent_bg);
        mContext = context;
        Window window = this.getWindow();
        mLayoutParams = window.getAttributes();
        mLayoutParams.alpha = 1f;
        window.setAttributes(mLayoutParams);
        if (mLayoutParams != null) {
            mLayoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            mLayoutParams.gravity = Gravity.CENTER;
        }
        View dialogView =
                LayoutInflater.from(getContext()).inflate(R.layout.dialog_push_config, null);
        mTvSure = dialogView.findViewById(R.id.tv_sure);
        mTvCancel = dialogView.findViewById(R.id.tv_cancle);
        etId = dialogView.findViewById(R.id.et_id);
        etTitle = dialogView.findViewById(R.id.et_title);
        etContent = dialogView.findViewById(R.id.et_content);
        etContent = dialogView.findViewById(R.id.et_content);
        etData = dialogView.findViewById(R.id.et_data);
        etHW = dialogView.findViewById(R.id.et_hw);
        etImageUrlHW = dialogView.findViewById(R.id.et_hw_image_url);
        etHWImportance = dialogView.findViewById(R.id.et_importance_hw);
        etMi = dialogView.findViewById(R.id.et_mi);
        etImageUrlMi = dialogView.findViewById(R.id.et_mi_image_url);
        etOppo = dialogView.findViewById(R.id.et_oppo);
        etThreadId = dialogView.findViewById(R.id.et_thread_id);
        etApnId = dialogView.findViewById(R.id.et_apns_id);
        edCategory = dialogView.findViewById(R.id.et_category);
        edRichMediaUri = dialogView.findViewById(R.id.et_richMediaUri);
        edInterruptionLevel = dialogView.findViewById(R.id.et_interruptionLevel);
        edTemplateId = dialogView.findViewById(R.id.et_template_id);

        cbVivo = dialogView.findViewById(R.id.cb_vivo);
        cbDisableTitle = dialogView.findViewById(R.id.cb_is_disable_title);
        cbForceDetail = dialogView.findViewById(R.id.cb_is_show_detail);
        edFcm = dialogView.findViewById(R.id.et_fcm);
        etChannelIdFcm = dialogView.findViewById(R.id.et_fcm_channel);
        edImageUrl = dialogView.findViewById(R.id.et_image_url);

        etOhosCategory = dialogView.findViewById(R.id.et_ohos_category);
        etOhosImageUrl = dialogView.findViewById(R.id.et_ohos_imageurl);

        setContentView(dialogView);
    }

    public PushConfigDialog(Activity context) {
        super(context);
        initView(context);
    }

    public PushConfigDialog(Context context, int type) {
        super(context);
        mType = type;
        initView(context);
    }

    public EditText getEdImageUrl() {
        return edImageUrl;
    }

    public void setEdImageUrl(EditText edImageUrl) {
        this.edImageUrl = edImageUrl;
    }

    public EditText getEdFcm() {
        return edFcm;
    }

    public void setEdFcm(EditText edFcm) {
        this.edFcm = edFcm;
    }

    public EditText getEtId() {
        return etId;
    }

    public EditText getEtTitle() {
        return etTitle;
    }

    public EditText getEtContent() {
        return etContent;
    }

    public EditText getEtData() {
        return etData;
    }

    public EditText getEtHW() {
        return etHW;
    }

    public EditText getEtMi() {
        return etMi;
    }

    public EditText getEtOppo() {
        return etOppo;
    }

    public EditText getEtThreadId() {
        return etThreadId;
    }

    public EditText getEtApnId() {
        return etApnId;
    }

    public EditText getEdTemplateId() {
        return edTemplateId;
    }

    public EditText getEdCategory() {
        return edCategory;
    }

    public void setEdCategory(EditText edCategory) {
        this.edCategory = edCategory;
    }

    public EditText getEdRichMediaUri() {
        return edRichMediaUri;
    }

    public void setEdRichMediaUri(EditText edRichMediaUri) {
        this.edRichMediaUri = edRichMediaUri;
    }

    public EditText getEdInterruptionLevel() {
        return edInterruptionLevel;
    }

    public void setEdInterruptionLevel(EditText edInterruptionLevel) {
        this.edInterruptionLevel = edInterruptionLevel;
    }

    public CheckBox getCbVivo() {
        return cbVivo;
    }

    public CheckBox getCbDisableTitle() {
        return cbDisableTitle;
    }

    public CheckBox getCbForceDetail() {
        return cbForceDetail;
    }

    public TextView getSureView() {
        return mTvSure;
    }

    public void setSure(String strSure) {
        this.mTvSure.setText(strSure);
    }

    public TextView getCancelView() {
        return mTvCancel;
    }

    public void setCancel(String strCancel) {
        this.mTvCancel.setText(strCancel);
    }

    public EditText getEtHWImportance() {
        return etHWImportance;
    }

    public void setEtHWImportance(EditText etHWImportance) {
        this.etHWImportance = etHWImportance;
    }

    public EditText getEtImageUrlHW() {
        return etImageUrlHW;
    }

    public void setEtImageUrlHW(EditText etImageUrlHW) {
        this.etImageUrlHW = etImageUrlHW;
    }

    public EditText getEtImageUrlMi() {
        return etImageUrlMi;
    }

    public void setEtImageUrlMi(EditText etImageUrlMi) {
        this.etImageUrlMi = etImageUrlMi;
    }

    public EditText getEtChannelIdFcm() {
        return etChannelIdFcm;
    }

    public void setEtChannelIdFcm(EditText etChannelIdFcm) {
        this.etChannelIdFcm = etChannelIdFcm;
    }

    public EditText getEtOhosCategory() {
        return etOhosCategory;
    }

    public void setEtOhosCategory(EditText etOhosCategory) {
        this.etOhosCategory = etOhosCategory;
    }

    public EditText getEtOhosImageUrl() {
        return etOhosImageUrl;
    }

    public void setEtOhosImageUrl(EditText etOhosImageUrl) {
        this.etOhosImageUrl = etOhosImageUrl;
    }
}
