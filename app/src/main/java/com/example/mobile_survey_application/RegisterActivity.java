package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobile_survey_application.fragment.FragmentCategoryChange;

import java.util.ArrayList;
import java.util.List;

import model.CategoryResponse;
import model.RegisterRequest;
import viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_TEMP_TOKEN = "extra_temp_token";
    public static final String EXTRA_NAME = "extra_name";
    private static final String TAG_CATEGORY_DEBUG = "CATEGORY_DEBUG";

    private AuthViewModel authViewModel;

    private EditText etName, etTelephone, etNickname, etBirthDate, etRegion, etOccupation;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private LinearLayout llCategories;
    private LinearLayout btnCategories;
    private TextView btnCategoriesText;
    private Button btnRegister;
    private ProgressBar progressBar;

    private String tempToken;
    private final List<CategoryResponse> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tempToken = getIntent().getStringExtra(EXTRA_TEMP_TOKEN);
        String prefillName = getIntent().getStringExtra(EXTRA_NAME);

        etName = findViewById(R.id.et_name);
        etTelephone = findViewById(R.id.et_telephone);
        etNickname = findViewById(R.id.et_nickname);
        etBirthDate = findViewById(R.id.et_birth_date);
        etRegion = findViewById(R.id.et_region);
        etOccupation = findViewById(R.id.et_occupation);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        llCategories = findViewById(R.id.ll_categories);
        //tvSelectedCategories = findViewById(R.id.tv_selected_categories); 이거 대신 아래코드사용으로 변경
        btnCategories = findViewById(R.id.btnCategory);
        btnCategoriesText = findViewById(R.id.btnCategoryText);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);

        if (prefillName != null) {
            etName.setText(prefillName);
        }

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.loadCategories();

        observeViewModel();
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            View fragmentContainer = findViewById(R.id.fragment_container);
            if (fragmentContainer != null
                    && getSupportFragmentManager().getBackStackEntryCount() == 0) {
                fragmentContainer.setVisibility(View.GONE);
            }
        });



        btnRegister.setOnClickListener(v -> submitRegister());
    }
    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
        });

        authViewModel.getRegisterResult().observe(this, response -> {
            Toast.makeText(this, "회원가입 완료! 홈으로 이동합니다.", Toast.LENGTH_SHORT).show();

            Log.d("REGISTER_DEBUG", "register success navigate HomeActivity");
            Log.d("REGISTER_DEBUG", "register success clear auth stack");
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        authViewModel.getErrorMessage().observe(this, message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
        authViewModel.getSelectedCategoryIds().observe(this, ids -> {
            updateSelectedCategoryText();
        });

        authViewModel.getCategories().observe(this, categories -> {
            categoryList.clear();
            if (categories != null) {
                categoryList.addAll(categories);
            }
            updateSelectedCategoryText();
        });

        // 카테고리 버튼기능 Fragment + ViewModel 통합
        observeBtnCategory();

    }

    // 카테고리 기능
    private void observeBtnCategory() {
        View btnCategory = findViewById(R.id.btnCategory);

        btnCategory.setOnClickListener(v -> {
            Toast.makeText(this, "카테고리 클릭됨", Toast.LENGTH_SHORT).show();
            authViewModel.onCategoryClick();
        });

        authViewModel.getNavigateToCategoryChange().observe(this, shouldNavigate -> {
            if (Boolean.TRUE.equals(shouldNavigate)) {

                View fragmentContainer = findViewById(R.id.fragment_container);

                if (fragmentContainer == null) {
                    Toast.makeText(this, "fragment_container를 못 찾음", Toast.LENGTH_SHORT).show();
                    authViewModel.doneNavigateToCategoryChange();
                    return;
                }

                fragmentContainer.setVisibility(View.VISIBLE);

                Bundle bundle = new Bundle();
                bundle.putString("mode", FragmentCategoryChange.MODE_REGISTER);

                FragmentCategoryChange fragment = new FragmentCategoryChange();
                fragment.setArguments(bundle);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();

                authViewModel.doneNavigateToCategoryChange();
            }
        });
    }

    private void updateSelectedCategoryText() {
        List<Long> selectedCategoryIds = authViewModel.getSelectedCategoryIds().getValue();

        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            btnCategoriesText.setText("선택된 카테고리: 없음");
            return;
        }

        StringBuilder sb = new StringBuilder("선택된 카테고리: ");

        for (int i = 0; i < selectedCategoryIds.size(); i++) {
            Long id = selectedCategoryIds.get(i);

            sb.append(getCategoryNameById(id));

            if (i < selectedCategoryIds.size() - 1) {
                sb.append(", ");
            }
        }

        btnCategoriesText.setText(sb.toString());
    }

    private String getCategoryNameById(Long id) {
        for (CategoryResponse category : categoryList) {
            if (category.getId().equals(id)) {
                return category.getName();
            }
        }

        return "알 수 없음";
    }

    private void submitRegister() {
        String name = etName.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String location = etRegion.getText().toString().trim();
        String region = location;
        String occupation = etOccupation.getText().toString().trim();

        if (name.isEmpty() || telephone.isEmpty() || nickname.isEmpty()
                || birthDate.isEmpty() || location.isEmpty() || occupation.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String gender = (selectedGenderId == R.id.rb_male) ? "MALE" : "FEMALE";

        List<Long> selectedCategoryIds = authViewModel.getSelectedCategoryIds().getValue();

        if (selectedCategoryIds == null || selectedCategoryIds.size() != 3) {
            Toast.makeText(this, "카테고리를 3개 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(
                name, telephone, nickname, gender, birthDate, location, region, occupation,
                new ArrayList<>(selectedCategoryIds)
        );
        Log.d("REGISTER_DEBUG", "register request categoryIds=" + selectedCategoryIds);
        Log.d("REGISTER_DEBUG", "register request name exists=" + !name.isEmpty());
        Log.d("REGISTER_DEBUG", "register request phone exists=" + !telephone.isEmpty());
        Log.d("REGISTER_DEBUG", "register request nickname exists=" + !nickname.isEmpty());
        Log.d("REGISTER_DEBUG", "register request birthDate=" + birthDate);
        Log.d("REGISTER_DEBUG", "register request gender=" + gender);
        Log.d("REGISTER_DEBUG", "register request location exists=" + !location.isEmpty());
        Log.d("REGISTER_DEBUG", "register request region exists=" + !region.isEmpty());
        Log.d("REGISTER_DEBUG", "register request occupation exists=" + !occupation.isEmpty());
        Log.d("REGISTER_DEBUG", "register request tempToken exists="
                + (tempToken != null && !tempToken.isEmpty()));
        Log.d(TAG_CATEGORY_DEBUG, "signup category selectedIds=" + selectedCategoryIds);
        authViewModel.register(tempToken, request);
    }
}
