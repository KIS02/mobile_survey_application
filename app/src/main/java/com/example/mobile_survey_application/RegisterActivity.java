package com.example.mobile_survey_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import model.CategoryResponse;
import model.RegisterRequest;
import viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_TEMP_TOKEN = "extra_temp_token";
    public static final String EXTRA_NAME = "extra_name";

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
    private final List<Long> selectedCategoryIds = new ArrayList<>();
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



        btnRegister.setOnClickListener(v -> submitRegister());
    }
    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnRegister.setEnabled(!isLoading);
        });

        authViewModel.getRegisterResult().observe(this, response -> {
            Toast.makeText(this, "회원가입 완료! 홈으로 이동합니다.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        authViewModel.getErrorMessage().observe(this, message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );

        // 카테고리 버튼기능 Fragment + ViewModel 통합
        observeBtnCategory();

    }

    // 카테고리 기능
    private void observeBtnCategory(){
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

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new FragmentCategoryChange())
                        .addToBackStack(null)
                        .commit();

                authViewModel.doneNavigateToCategoryChange();
            }
        });
    }

    private void renderCategoryCheckBoxes(List<CategoryResponse> categories) {


        // 우선 바로 다음으로 fragment를 띄우는 기능으로 전환하였습니다.
        /*
        categoryList.clear();
        categoryList.addAll(categories);
        llCategories.removeAllViews();

        for (CategoryResponse category : categories) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(category.getName());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedCategoryIds.size() >= 5) {
                        checkBox.setChecked(false);
                        Toast.makeText(this, "최대 5개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedCategoryIds.add(category.getId());
                } else {
                    selectedCategoryIds.remove(category.getId());
                }
                updateSelectedCategoryText();
            });
            llCategories.addView(checkBox);
        }
         */
    }

    private void updateSelectedCategoryText() {
        if (selectedCategoryIds.isEmpty()) {
            btnCategoriesText.setText("선택된 카테고리: 없음");
            return;
        }
        StringBuilder sb = new StringBuilder("선택된 카테고리: ");
        for (int i = 0; i < selectedCategoryIds.size(); i++) {
            Long id = selectedCategoryIds.get(i);
            for (CategoryResponse c : categoryList) {
                if (c.getId().equals(id)) {
                    sb.append(c.getName());
                    if (i < selectedCategoryIds.size() - 1) sb.append(", ");
                    break;
                }
            }
        }
        btnCategoriesText.setText(sb.toString());
    }

    private void submitRegister() {
        String name = etName.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String region = etRegion.getText().toString().trim();
        String occupation = etOccupation.getText().toString().trim();

        if (name.isEmpty() || telephone.isEmpty() || nickname.isEmpty()
                || birthDate.isEmpty() || region.isEmpty() || occupation.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String gender = (selectedGenderId == R.id.rb_male) ? "MALE" : "FEMALE";

        RegisterRequest request = new RegisterRequest(
                name, telephone, nickname, gender, birthDate, region, occupation,
                new ArrayList<>(selectedCategoryIds)
        );

        authViewModel.register(tempToken, request);
    }
}
