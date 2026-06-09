package com.example.appbanhang.managers;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.appbanhang.R;

public class ImageManager {
    private static ImageManager instance;

    private ImageManager() {}

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    /**
     * Load ảnh từ URL vào ImageView
     * @param imageUrl URL của ảnh
     * @param imageView ImageView để hiển thị ảnh
     */
    public void loadImage(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }
        
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .placeholder(R.drawable.ic_launcher_foreground)
             .error(R.drawable.ic_launcher_foreground)
             .centerCrop()
             .into(imageView);
    }

    /**
     * Load ảnh với animation fade transition
     * @param imageUrl URL của ảnh
     * @param imageView ImageView để hiển thị ảnh
     */
    public void loadImageWithAnimation(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }
        
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .transition(DrawableTransitionOptions.withCrossFade())
             .placeholder(R.drawable.ic_launcher_foreground)
             .error(R.drawable.ic_launcher_foreground)
             .centerCrop()
             .into(imageView);
    }

    /**
     * Load ảnh thumbnail với size nhỏ
     * @param imageUrl URL của ảnh
     * @param imageView ImageView để hiển thị ảnh
     */
    public void loadThumbnail(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }
        
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .placeholder(R.drawable.ic_launcher_foreground)
             .error(R.drawable.ic_launcher_foreground)
             .fitCenter()
             .into(imageView);
    }

    /**
     * Load ảnh banner
     * @param imageUrl URL của ảnh
     * @param imageView ImageView để hiển thị ảnh
     */
    public void loadBannerImage(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
            return;
        }
        
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .placeholder(R.drawable.ic_launcher_foreground)
             .error(R.drawable.ic_launcher_foreground)
             .fitCenter()
             .into(imageView);
    }

    /**
     * Clear memory cache
     */
    public void clearMemoryCache(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * Clear disk cache
     */
    public void clearDiskCache(Context context) {
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }
}
