

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.bjtmtech.seotracker.R
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.loading_dialog.view.*

class LoadingDialog(val mActivity: Fragment){
    private lateinit var isDialog:AlertDialog
    val handle = android.os.Handler()
    fun startLoading(){
//        Set View
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)

//        dialogView.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        Set Dialog

        val builder = AlertDialog.Builder(mActivity.requireContext())
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isDialog.show()
        handle.postDelayed(
            {
                isDismiss()
//                FancyToast.makeText(builder.context, "Loading time too long, time-out!", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show()
            }, 8000
        )
    }

    fun isDismiss(){
        isDialog.dismiss()

    }
}