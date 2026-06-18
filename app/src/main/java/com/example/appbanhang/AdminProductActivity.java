package com.example.appbanhang;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.AdminProductAdapter;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class AdminProductActivity extends AppCompatActivity {

    private EditText edtName, edtCategory, edtBrand, edtPrice, edtStock, edtImage, edtDescription;
    private TextView txtSummary, txtFormTitle, txtEmpty;
    private Button btnSave, btnClear, btnSeed;
    private RecyclerView recyclerProducts;
    private final List<Product> products = new ArrayList<>();
    private AdminProductAdapter adapter;
    private FirestoreRepository firestoreRepository;
    private Product editingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestoreRepository = FirestoreRepository.getInstance();
        initializeViews();
        setupRecycler();
        setupListeners();
        fetchProducts();
    }

    private void initializeViews() {
        edtName = findViewById(R.id.edt_product_name);
        edtCategory = findViewById(R.id.edt_product_category);
        edtBrand = findViewById(R.id.edt_product_brand);
        edtPrice = findViewById(R.id.edt_product_price);
        edtStock = findViewById(R.id.edt_product_stock);
        edtImage = findViewById(R.id.edt_product_image);
        edtDescription = findViewById(R.id.edt_product_description);
        txtSummary = findViewById(R.id.txt_admin_summary);
        txtFormTitle = findViewById(R.id.txt_admin_form_title);
        txtEmpty = findViewById(R.id.txt_admin_empty);
        btnSave = findViewById(R.id.btn_admin_save);
        btnClear = findViewById(R.id.btn_admin_clear);
        btnSeed = findViewById(R.id.btn_admin_seed);
        recyclerProducts = findViewById(R.id.recycler_admin_products);
        findViewById(R.id.btn_admin_back).setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new AdminProductAdapter(products, this, new AdminProductAdapter.AdminProductActionListener() {
            @Override
            public void onEdit(Product product) {
                startEditing(product);
            }

            @Override
            public void onDelete(Product product) {
                confirmDelete(product);
            }
        });
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProduct());
        btnClear.setOnClickListener(v -> clearForm());
        btnSeed.setOnClickListener(v -> seedLocalProductsToFirebase());
    }

    private void fetchProducts() {
        txtSummary.setText("Đang tải dữ liệu Firebase...");
        firestoreRepository.fetchProducts(new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> firebaseProducts) {
                products.clear();
                products.addAll(firebaseProducts);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String errorMessage) {
                txtSummary.setText("Không tải được sản phẩm");
                Toast.makeText(AdminProductActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void saveProduct() {
        Product product = readProductFromForm();
        if (product == null) {
            return;
        }

        btnSave.setEnabled(false);
        FirestoreRepository.OperationCallback callback = new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                btnSave.setEnabled(true);
                Toast.makeText(AdminProductActivity.this, "Đã lưu sản phẩm", Toast.LENGTH_SHORT).show();
                clearForm();
                fetchProducts();
            }

            @Override
            public void onError(String errorMessage) {
                btnSave.setEnabled(true);
                Toast.makeText(AdminProductActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        };

        if (editingProduct == null) {
            firestoreRepository.addProduct(product, callback);
        } else {
            firestoreRepository.updateProduct(product, callback);
        }
    }

    private Product readProductFromForm() {
        String name = textOf(edtName);
        String category = textOf(edtCategory);
        String brand = textOf(edtBrand);
        String imageUrl = textOf(edtImage);
        String description = textOf(edtDescription);

        if (name.isEmpty()) {
            edtName.setError("Nhập tên sản phẩm");
            return null;
        }
        if (imageUrl.isEmpty()) {
            edtImage.setError("Nhập link ảnh sản phẩm");
            return null;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(textOf(edtPrice));
        } catch (NumberFormatException e) {
            edtPrice.setError("Giá không hợp lệ");
            return null;
        }
        try {
            stock = textOf(edtStock).isEmpty() ? 0 : Integer.parseInt(textOf(edtStock));
        } catch (NumberFormatException e) {
            edtStock.setError("Tồn kho không hợp lệ");
            return null;
        }

        int id = editingProduct == null ? nextProductId() : editingProduct.getId();
        Product product = new Product(id, name, category, price, imageUrl, description, 5.0, brand);
        product.setStock(stock);
        product.setThumbnailUrl(imageUrl);
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(imageUrl);
        product.setImageUrls(imageUrls);
        product.setNew(editingProduct == null || editingProduct.isNew());
        if (editingProduct != null) {
            product.setDiscount(editingProduct.getDiscount());
            product.setPromotion(editingProduct.getPromotion());
            product.setColor(editingProduct.getColor());
            product.setReviewCount(editingProduct.getReviewCount());
            product.setRating(editingProduct.getRating());
        }
        return product;
    }

    private int nextProductId() {
        int maxId = 0;
        for (Product product : products) {
            maxId = Math.max(maxId, product.getId());
        }
        return maxId + 1;
    }

    private void startEditing(Product product) {
        editingProduct = product;
        txtFormTitle.setText("Sửa sản phẩm #" + product.getId());
        btnSave.setText("Cập nhật sản phẩm");
        edtName.setText(product.getName());
        edtCategory.setText(product.getCategory());
        edtBrand.setText(product.getBrand());
        edtPrice.setText(String.valueOf(product.getPrice()));
        edtStock.setText(String.valueOf(product.getStock()));
        edtImage.setText(product.getImageUrl());
        edtDescription.setText(product.getDescription());
    }

    private void clearForm() {
        editingProduct = null;
        txtFormTitle.setText("Thêm sản phẩm mới");
        btnSave.setText("Lưu sản phẩm");
        edtName.setText("");
        edtCategory.setText("");
        edtBrand.setText("");
        edtPrice.setText("");
        edtStock.setText("");
        edtImage.setText("");
        edtDescription.setText("");
    }

    private void confirmDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn chắc chắn muốn xóa \"" + product.getName() + "\" khỏi Firebase?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .show();
    }

    private void deleteProduct(Product product) {
        firestoreRepository.deleteProduct(product.getId(), new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminProductActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                if (editingProduct != null && editingProduct.getId() == product.getId()) {
                    clearForm();
                }
                fetchProducts();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AdminProductActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void seedLocalProductsToFirebase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Product> localProducts = dbHelper.getAllProducts();
        if (localProducts.isEmpty()) {
            localProducts = DataProvider.getProducts();
        }
        final List<Product> productsToSeed = localProducts;

        btnSeed.setEnabled(false);
        firestoreRepository.seedProductsToFirebase(productsToSeed, new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                btnSeed.setEnabled(true);
                Toast.makeText(AdminProductActivity.this, "Đã chuyển dữ liệu lên Firebase", Toast.LENGTH_SHORT).show();
                fetchProducts();
            }

            @Override
            public void onError(String errorMessage) {
                btnSeed.setEnabled(true);
                Toast.makeText(AdminProductActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyState() {
        txtSummary.setText(products.size() + " sản phẩm trong Firebase");
        boolean isEmpty = products.isEmpty();
        txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private String textOf(EditText editText) {
        return editText.getText().toString().trim();
    }
}
