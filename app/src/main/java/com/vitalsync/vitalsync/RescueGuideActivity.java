package com.vitalsync.vitalsync;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class RescueGuideActivity extends AppCompatActivity {

    private TextView tvCurrentStatus, tvConditionTitle, tvInstructions, tvGuideHeader;
    private ImageView ivConditionIcon;
    private LinearLayout llCardContent;
    private Button btnEmergencyCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescue_guide);

        // Force RTL for Arabic layout if needed, though system usually handles it.
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        tvGuideHeader = findViewById(R.id.tvGuideHeader);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvConditionTitle = findViewById(R.id.tvConditionTitle);
        tvInstructions = findViewById(R.id.tvInstructions);
        ivConditionIcon = findViewById(R.id.ivConditionIcon);
        llCardContent = findViewById(R.id.llCardContent);
        btnEmergencyCall = findViewById(R.id.btnEmergencyCall);

        tvGuideHeader.setText("دليل الإنقاذ");
        btnEmergencyCall.setText("اتصال بالطوارئ (101)");

        int bpm = getIntent().getIntExtra("BPM", 0);
        boolean isAthlete = getIntent().getBooleanExtra("IS_ATHLETE", false);

        tvCurrentStatus.setText("القراءة الحالية: " + bpm + " نبضة/دقيقة");

        updateGuideUI(bpm, isAthlete);

        btnEmergencyCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:101")); 
            startActivity(callIntent);
        });
    }

    private void updateGuideUI(int bpm, boolean isAthlete) {
        if (bpm > 100) {
            if (isAthlete) {
                // Scenario B: HIGH BPM for Athletes
                tvConditionTitle.setText("نبض مرتفع (حالة رياضية)");
                tvInstructions.setText("لا تقلق! بصفتك رياضياً، من الطبيعي جداً أن يرتفع نبض قلبك أثناء أو بعد التمارين الشاقة.\n\n" +
                        "هذا دليل على قوة عضلة القلب واستجابتها للجهد. كل ما عليك فعله هو التوقف عن التمرين، المشي ببطء لتهدئة النبض، وشرب السوائل. استرح لمدة 10 إلى 15 دقيقة وراقب انخفاض النبض تدريجياً ليعود لوضعه الطبيعي.");
                ivConditionIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                ivConditionIcon.setColorFilter(ContextCompat.getColor(this, R.color.accentElectric));
                llCardContent.setBackgroundColor(Color.parseColor("#F0F9FF")); // Light Blue tint
            } else {
                // Scenario A: HIGH BPM for Regular Users
                tvConditionTitle.setText("تحذير: نبضات قلب مرتفعة!");
                tvInstructions.setText("1. اجلس واسترح فوراً في مكان بارد ومريح.\n" +
                        "2. خذ أنفاساً عميقة وبطيئة (شهيق لـ 4 ثوانٍ، كتم النفس لـ 4 ثوانٍ، ثم زفير لـ 4 ثوانٍ) لتهدئة الجهاز العصبي.\n" +
                        "3. اشرب بضع رشفات من الماء البارد.\n" +
                        "4. ارخِ أي ملابس ضيقة حول رقبتك أو صدرك.\n" +
                        "5. إذا شعرت بألم في الصدر، ضيق تنفس، أو دوخة مستمرة، اضغط على زر الطوارئ بالأسفل فوراً.");
                ivConditionIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                ivConditionIcon.setColorFilter(ContextCompat.getColor(this, R.color.highText));
                llCardContent.setBackgroundColor(Color.parseColor("#FFF5F5")); // Light Red tint
            }
        } else if (bpm < 60 && bpm > 0) {
            // Scenario C: LOW BPM
            tvConditionTitle.setText("تنبيه: نبضات قلب منخفضة!");
            tvInstructions.setText("1. استلقِ على ظهرك وقم برفع قدميك قليلاً عن مستوى الأرض لزيادة تدفق الدم إلى الدماغ.\n" +
                    "2. تجنب القيام المفاجئ أو الحركات السريعة لمنع حدوث دوخة أو إغماء.\n" +
                    "3. تناول مشروباً دافئاً أو وجبة خفيفة مالحة إذا كنت بكامل وعيك.\n" +
                    "4. إذا كان الانخفاض مصحوباً بتعب شديد، ضبابية في الرؤية، أو غثيان، يرجى طلب المساعدة فوراً.");
            ivConditionIcon.setImageResource(android.R.drawable.ic_dialog_info);
            ivConditionIcon.setColorFilter(ContextCompat.getColor(this, R.color.lowText));
            llCardContent.setBackgroundColor(Color.parseColor("#F0F4F8")); // Light Gray/Blue tint
        } else if (bpm == 0) {
            tvConditionTitle.setText("في انتظار البيانات...");
            tvInstructions.setText("يرجى التأكد من وضع الإصبع بشكل صحيح على الحساس.");
        } else {
            // Normal Condition
            tvConditionTitle.setText("نبض طبيعي");
            tvInstructions.setText("نبضات قلبك ضمن النطاق الطبيعي. استمر في الحفاظ على نمط حياة صحي.");
            ivConditionIcon.setImageResource(android.R.drawable.ic_menu_compass);
            ivConditionIcon.setColorFilter(ContextCompat.getColor(this, R.color.normalText));
            llCardContent.setBackgroundColor(Color.parseColor("#F6FFF8")); // Light Green tint
        }
    }
}
