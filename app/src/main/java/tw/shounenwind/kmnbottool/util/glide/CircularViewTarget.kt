package tw.shounenwind.kmnbottool.util.glide

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView
import com.bumptech.glide.request.target.BitmapImageViewTarget

class CircularViewTarget(private val mContext: Context, private val imageView: ImageView)
    : BitmapImageViewTarget(imageView) {
    override fun setResource(resource: Bitmap?) {
        val circularBitmapDrawable =
                RoundedBitmapDrawableFactory.create(mContext.resources, resource)
        circularBitmapDrawable.isCircular = true
        imageView.setImageDrawable(circularBitmapDrawable)
    }
}