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
import com.example.appbanhang.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemListener onCartItemListener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
    }

    public void setOnCartItemListener(OnCartItemListener listener) {
        this.onCartItemListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        
        // Set product data
        holder.txtProductName.setText(cartItem.getProduct().getName());
        holder.txtProductSize.setText("Size: " + cartItem.getSelectedSize());
        holder.txtProductPrice.setText(String.format("Rp. %.0f", 
            cartItem.getProduct().getPrice()));
        holder.txtQuantity.setText(String.valueOf(cartItem.getQuantity()));
        
        ImageManager.getInstance().loadThumbnail(cartItem.getProduct().getImageUrl(), holder.imgProduct);
        
        // Quantity buttons
        holder.btnMinus.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                int newQuantity = cartItem.getQuantity() - 1;
                cartItem.setQuantity(newQuantity);
                holder.txtQuantity.setText(String.valueOf(newQuantity));
                if (onCartItemListener != null) {
                    onCartItemListener.onQuantityChanged(cartItem, newQuantity);
                }
            }
        });
        
        holder.btnPlus.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            cartItem.setQuantity(newQuantity);
            holder.txtQuantity.setText(String.valueOf(newQuantity));
            if (onCartItemListener != null) {
                onCartItemListener.onQuantityChanged(cartItem, newQuantity);
            }
        });
        
        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            if (onCartItemListener != null) {
                onCartItemListener.onItemRemoved(cartItem);
            }
            notifyItemRemoved(adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName;
        TextView txtProductSize;
        TextView txtProductPrice;
        TextView txtQuantity;
        Button btnMinus;
        Button btnPlus;
        Button btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtProductSize = itemView.findViewById(R.id.txt_product_size);
            txtProductPrice = itemView.findViewById(R.id.txt_product_price);
            txtQuantity = itemView.findViewById(R.id.txt_quantity);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
