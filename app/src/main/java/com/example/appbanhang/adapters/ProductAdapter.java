package com.example.appbanhang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.R;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    
    private List<Product> productList;
    private Context context;
    private OnProductClickListener onProductClickListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onFavoriteClick(Product product, boolean isFavorite);
    }

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_up));
        
        // Set product data
        holder.txtProductName.setText(product.getName());
        holder.txtProductCategory.setText(product.getCategory());
        holder.txtProductPrice.setText(String.format("Rp. %.0f", product.getPrice()));
        holder.txtRating.setText(String.valueOf(product.getRating()));
        
        // Load product image using Glide
        ImageManager.getInstance().loadImageWithAnimation(product.getImageUrl(), holder.imgProduct);
        
        // Set badge/promotion label
        if (product.getPromotion() != null && !product.getPromotion().isEmpty()) {
            holder.badgeLabel.setVisibility(View.VISIBLE);
            holder.badgeLabel.setText(product.getPromotion());
        } else if (product.isNew()) {
            holder.badgeLabel.setVisibility(View.VISIBLE);
            holder.badgeLabel.setText("NEW");
        } else {
            holder.badgeLabel.setVisibility(View.GONE);
        }
        
        // Set favorite button
        updateFavoriteButton(holder.btnFavorite, product.isFavorite());
        
        holder.btnFavorite.setOnClickListener(v -> {
            product.setFavorite(!product.isFavorite());
            updateFavoriteButton(holder.btnFavorite, product.isFavorite());
            if (onProductClickListener != null) {
                onProductClickListener.onFavoriteClick(product, product.isFavorite());
            }
        });
        
        // Product item click
        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void updateFavoriteButton(Button button, boolean isFavorite) {
        if (isFavorite) {
            button.setText("❤️");
        } else {
            button.setText("🤍");
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName;
        TextView txtProductCategory;
        TextView txtProductPrice;
        TextView txtRating;
        Button btnFavorite;
        TextView badgeLabel;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtProductCategory = itemView.findViewById(R.id.txt_product_category);
            txtProductPrice = itemView.findViewById(R.id.txt_product_price);
            txtRating = itemView.findViewById(R.id.txt_rating);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            badgeLabel = itemView.findViewById(R.id.badge_label);
        }
    }
}
