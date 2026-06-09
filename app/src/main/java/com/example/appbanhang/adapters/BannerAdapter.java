package com.example.appbanhang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appbanhang.R;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    
    private List<Banner> bannerList;
    private Context context;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }

    public BannerAdapter(List<Banner> bannerList, Context context) {
        this.bannerList = bannerList;
        this.context = context;
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        
        // Load ảnh banner
        ImageManager.getInstance()
                .loadBannerImage(banner.getImageUrl(), holder.bannerImage);
        
        holder.bannerTitle.setText(banner.getTitle());
        if (banner.getSubtitle() != null) {
            holder.bannerSubtitle.setText(banner.getSubtitle());
        }
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(banner);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView bannerTitle, bannerSubtitle;

        BannerViewHolder(View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
            bannerTitle = itemView.findViewById(R.id.banner_title);
            bannerSubtitle = itemView.findViewById(R.id.banner_subtitle);
        }
    }
}
