package com.example.appbanhang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.R;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.ProductDisplayUtils;

import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    public interface AdminProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private final List<Product> products;
    private final Context context;
    private final AdminProductActionListener listener;

    public AdminProductAdapter(List<Product> products, Context context, AdminProductActionListener listener) {
        this.products = products;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.txtName.setText(product.getName());
        holder.txtMeta.setText("Danh mục: " + ProductDisplayUtils.category(product.getCategory())
                + " - Thương hiệu: " + safe(product.getBrand())
                + " - Tồn: " + product.getStock());
        holder.txtPrice.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", product.getPrice()));
        ImageManager.getInstance().loadThumbnail(product.getImageUrl(), holder.imgProduct);
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Chưa nhập" : value;
    }

    static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName;
        TextView txtMeta;
        TextView txtPrice;
        Button btnEdit;
        Button btnDelete;

        AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_admin_product);
            txtName = itemView.findViewById(R.id.txt_admin_product_name);
            txtMeta = itemView.findViewById(R.id.txt_admin_product_meta);
            txtPrice = itemView.findViewById(R.id.txt_admin_product_price);
            btnEdit = itemView.findViewById(R.id.btn_admin_edit);
            btnDelete = itemView.findViewById(R.id.btn_admin_delete);
        }
    }
}
