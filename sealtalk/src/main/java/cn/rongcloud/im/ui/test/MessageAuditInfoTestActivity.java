package cn.rongcloud.im.ui.test;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;

public class MessageAuditInfoTestActivity extends TitleBaseActivity
        implements View.OnClickListener {
    private EditText switchEditText;
    private EditText projectEditText;
    private EditText strategyEditText;
    private Button setButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditinfo_set);
        getTitleBar().setTitle("消息审核设置");
        switchEditText = findViewById(R.id.et_audit_set_switch);
        projectEditText = findViewById(R.id.et_audit_set_project);
        strategyEditText = findViewById(R.id.et_audit_set_strategy);
        setButton = findViewById(R.id.btn_audit_set);
        setButton.setOnClickListener(this);

        loadLastConfig();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_audit_set) {
            String switchText = switchEditText.getText().toString();
            String projectText = projectEditText.getText().toString();
            String strategyText = strategyEditText.getText().toString();
            saveConfig(switchText, projectText, strategyText);
        }
    }

    private void loadLastConfig() {
        SharedPreferences sp = getSharedPreferences("audit_config", MODE_PRIVATE);
        int switchInt = sp.getInt("switch", 0);
        String project = sp.getString("project", "");
        String strategy = sp.getString("strategy", "");
        switchEditText.setText(String.valueOf(switchInt));
        projectEditText.setText(project);
        strategyEditText.setText(strategy);
    }

    private void saveConfig(String switchText, String projectText, String strategyText) {
        int switchInt = Integer.parseInt(switchText);
        SharedPreferences.Editor edit = getSharedPreferences("audit_config", MODE_PRIVATE).edit();
        edit.putInt("switch", switchInt);
        edit.putString("project", projectText);
        edit.putString("strategy", strategyText);
        edit.commit();
        Toast.makeText(
                        this,
                        "保存成功 允许审核："
                                + switchInt
                                + ", project："
                                + projectText
                                + ", strategy："
                                + strategyText,
                        Toast.LENGTH_SHORT)
                .show();
    }
}
