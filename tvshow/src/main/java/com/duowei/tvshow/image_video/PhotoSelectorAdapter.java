package com.duowei.tvshow.image_video;


import android.app.Activity;
import android.content.Context;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.duowei.tvshow.R;
import com.squareup.picasso.Picasso;

/**
 * 图片显示适配器
 */
public class PhotoSelectorAdapter extends BaseAdapter {
    /**
     * 图片路径实体
     */
	ImageDir imageDir;
    /**
     * 上下文
     */
	Context context;
    /**
     * 反射器
     */
	LayoutInflater inflator;
    /**
     * 点击事件监听
     */
	public boolean []isCheck;

	public PhotoSelectorAdapter(Activity context, ImageDir imageDir) {
		this.imageDir = imageDir;
		this.context = context;
		this.inflator = LayoutInflater.from(context);
		isCheck=new boolean[imageDir.getFiles().size()+1];
		for(int i=0;i<imageDir.getFiles().size()+1;i++){
			isCheck[i]=false;
		}
	}

    //加1  因为第一个显示的是相机
	@Override
	public int getCount() {
		return imageDir.getFiles().size() + 1;
	}

	@Override
	public String getItem(int position) {
		return imageDir.getFiles().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		final ViewHodler viewHolder;
		if (convertView == null) {
			convertView = inflator.inflate(R.layout.grid_item_photo, null);
			viewHolder = new ViewHodler();
			viewHolder.relativeLayout=(RelativeLayout)convertView.findViewById(R.id.relative);
			viewHolder.photoView = (ImageView) convertView.findViewById(R.id.img_photo);
            viewHolder.dagou=(ImageView) convertView.findViewById(R.id.img_dagou);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHodler) convertView.getTag();
		}

		if (position == 0) {
            //第一个显示相机
//			viewHolder.photoView.setImageResource(R.mipmap.compose_photo_photograph);
//			viewHolder.photoView.setScaleType(ScaleType.CENTER_INSIDE);
//			viewHolder.chSelect.setVisibility(View.GONE);
		} else {
			viewHolder.photoView.setScaleType(ScaleType.CENTER_CROP);
			String path = getItem(position - 1);
			if(isCheck[position]==false){
				viewHolder.dagou.setVisibility(View.GONE);
			}else if(isCheck[position]==true){
				viewHolder.dagou.setVisibility(View.VISIBLE);
			}

			if (imageDir.getType() == ImageDir.Type.VEDIO) {
//				viewHolder.photoView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND));
			} else {
                //减1 因为第一个显示的是相机  getItem(position - 1)表示当前位置显示的图片路径
				Glide.with(context).load(getItem(position - 1)).placeholder(R.mipmap.bg).fitCenter().into(viewHolder.photoView);
            }
		}
		return convertView;
	}

	public static class ViewHodler {
		RelativeLayout relativeLayout;
		ImageView photoView;
        ImageView dagou;
	}
}
