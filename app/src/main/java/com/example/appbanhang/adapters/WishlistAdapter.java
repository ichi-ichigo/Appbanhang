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

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {
    
    private List<Product> wishlistItems;
    private Context context;
    private OnWishlistListener onWishlistListener;

    public interface OnWishlistListener {
        void onProductClick(Product product);
        void onBuyClick(Product product);
        void onRemoveClick(Product product);
    }

    public WishlistAdapter(List<Product> wishlistItems, Context context) {
        this.wishlistItems = wishlistItems;
        this.context = context;
    }

    public void setOnWishlistListener(OnWishlistListener listener) {
        this.onWishlistListener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = wishlistItems.get(position);
        
        // Set product data
        holder.txtProductName.setText(product.getName());
        holder.txtProductCategory.setText(ProductDisplayUtils.category(product.getCategory()));
        holder.txtProductPrice.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", product.getPrice()));
        
        ImageManager.getInstance().loadThumbnail(product.getImageUrl(), holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            if (onWishlistListener != null) {
                onWishlistListener.onProductClick(product);
            }
        });
        
        // Buy button
        holder.btnBuy.setOnClickListener(v -> {
            if (onWishlistListener != null) {
                onWishlistListener.onBuyClick(product);
            }
        });
        
        // Item click to remove
        holder.itemView.setOnLongClickListener(v -> {
            if (onWishlistListener != null) {
                onWishlistListener.onRemoveClick(product);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    public void removeItem(int position) {
        wishlistItems.remove(position);
        notifyItemRemoved(position);
    }

    public static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName;
        TextView txtProductCategory;
        TextView txtProductPrice;
        Button btnBuy;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtProductCategory = itemView.findViewById(R.id.txt_product_category);
            txtProductPrice = itemView.findViewById(R.id.txt_product_price);
            btnBuy = itemView.findViewById(R.id.btn_buy);
        }
    }
}
