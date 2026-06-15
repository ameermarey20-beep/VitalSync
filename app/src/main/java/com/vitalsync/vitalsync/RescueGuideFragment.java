package com.vitalsync.vitalsync;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseUser;

public class RescueGuideFragment extends Fragment {

    private TextView tvCurrentStatus, tvConditionTitle, tvInstructions, tvGuideHeader;
    private ImageView ivConditionIcon;
    private LinearLayout llCardContent;
    private Button btnEmergencyCall;
    private boolean isAthlete = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_rescue_guide, container, false);

        // Force RTL for Arabic layout
        view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        tvGuideHeader = view.findViewById(R.id.tvGuideHeader);
        tvCurrentStatus = view.findViewById(R.id.tvCurrentStatus);
        tvConditionTitle = view.findViewById(R.id.tvConditionTitle);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        ivConditionIcon = view.findViewById(R.id.ivConditionIcon);
        llCardContent = view.findViewById(R.id.llCardContent);
        btnEmergencyCall = view.findViewById(R.id.btnEmergencyCall);

        tvGuideHeader.setText(R.string.rescue_guide_ar);
        btnEmergencyCall.setText(R.string.emergency_call);

        fetchUserAthleteStatus();
        checkConnectionAndUI();

        btnEmergencyCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:101"));
            startActivity(callIntent);
        });

        return view;
    }

    private void checkConnectionAndUI() {
        boolean isConnected = false;
        int currentBpm = 0;
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            isConnected = activity.isBluetoothConnected();
            currentBpm = activity.getLastBpm();
        }

        if (!isConnected) {
            tvCurrentStatus.setText("الحالة: غير متصل");
            tvConditionTitle.setText("في انتظار الاتصال...");
            tvInstructions.setText("يرجى الاتصال بروبوت Vital Sync عبر البلوتوث أولاً لتفعيل دليل الإنقاذ المباشر.");
            ivConditionIcon.setImageResource(android.R.drawable.ic_lock_lock);
            ivConditionIcon.setColorFilter(Color.GRAY);
            llCardContent.setBackgroundColor(Color.parseColor("#F5F5F5"));
        } else {
            // If connected
            tvCurrentStatus.setText(getString(R.string.current_reading, currentBpm));
            updateGuideUI(currentBpm, isAthlete);
        }
    }

    private void fetchUserAthleteStatus() {
        FirebaseUser user = FireBaseServices.getInstance().getAuth().getCurrentUser();
        if (user != null) {
            FireBaseServices.getInstance().getFirestore().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean athlete = documentSnapshot.getBoolean("isAthlete");
                            if (athlete != null) {
                                isAthlete = athlete;
                            }
                        }
                    });
        }
    }

    private void updateGuideUI(int bpm, boolean isAthlete) {
        if (bpm > 100) {
            if (isAthlete) {
                tvConditionTitle.setText("نبض مرتفع (حالة رياضية)");
                tvInstructions.setText("لا تقلق! بصفتك رياضياً، من الطبيعي جداً أن يرتفع نبض قلبك أثناء أو بعد التمارين الشاقة.\n\n" +
                        "هذا دليل على قوة عضلة القلب واستجابتها للجهد. كل ما عليك فعله هو التوقف عن التمرين، المشي ببطء لتهدئة النبض، وشرب السوائل.");
                ivConditionIcon.setImageResource(android.R.drawable.ic_menu_info_details);
                ivConditionIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.accentElectric));
                llCardContent.setBackgroundColor(Color.parseColor("#F0F9FF"));
            } else {
                tvConditionTitle.setText("تحذير: نبضات قلب مرتفعة!");
                tvInstructions.setText("1. اجلس واسترح فوراً في مكان بارد ومريح.\n" +
                        "2. خذ أنفاساً عميقة وبطيئة.\n" +
                        "3. اشرب بضع رشفات من الماء البارد.\n" +
                        "4. ارخِ أي ملابس ضيقة.\n" +
                        "5. إذا شعرت بألم في الصدر، اتصل بالطوارئ فوراً.");
                ivConditionIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                ivConditionIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.highText));
                llCardContent.setBackgroundColor(Color.parseColor("#FFF5F5"));
            }
        } else if (bpm < 60 && bpm > 0) {
            tvConditionTitle.setText("تنبيه: نبضات قلب منخفضة!");
            tvInstructions.setText("1. استلقِ على ظهرك وقم برفع قدميك قليلاً.\n" +
                    "2. تجنب القيام المفاجئ.\n" +
                    "3. تناول مشروباً دافئاً.\n" +
                    "4. إذا كان الانخفاض مصحوباً بتعب شديد، يرجى طلب المساعدة.");
            ivConditionIcon.setImageResource(android.R.drawable.ic_dialog_info);
            ivConditionIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.lowText));
            llCardContent.setBackgroundColor(Color.parseColor("#F0F4F8"));
        } else if (bpm == 0) {
            tvConditionTitle.setText("في انتظار البيانات...");
            tvInstructions.setText("يرجى التأكد من وضع الإصبع بشكل صحيح على الحساس.");
            ivConditionIcon.setImageResource(android.R.drawable.ic_menu_compass);
            ivConditionIcon.setColorFilter(Color.GRAY);
        } else {
            tvConditionTitle.setText("نبض طبيعي");
            tvInstructions.setText("نبضات قلبك ضمن النطاق الطبيعي. استمر في الحفاظ على نمط حياة صحي.");
            ivConditionIcon.setImageResource(android.R.drawable.ic_menu_compass);
            ivConditionIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.normalText));
            llCardContent.setBackgroundColor(Color.parseColor("#F6FFF8"));
        }
    }
}
