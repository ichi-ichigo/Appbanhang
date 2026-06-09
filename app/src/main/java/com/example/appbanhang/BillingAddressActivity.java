package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BillingAddressActivity extends AppCompatActivity {

    private EditText etFullName, etAddress, etPostCode;
    private Spinner spProvince, spCountry, spShipping;
    private Button btnContinue, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_address);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.et_full_name);
        etAddress = findViewById(R.id.et_address);
        etPostCode = findViewById(R.id.et_postal_code);
        spProvince = findViewById(R.id.spinner_province);
        spCountry = findViewById(R.id.spinner_country_2);
        spShipping = findViewById(R.id.spinner_shipping_option);
        btnContinue = findViewById(R.id.btn_continue);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            if (validateInput()) {
                Intent intent = new Intent(BillingAddressActivity.this, PaymentActivity.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private boolean validateInput() {
        if (etFullName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ và tên", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etPostCode.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã bưu điện", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
