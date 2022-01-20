

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.bjtmtech.seotracker.R
import java.util.logging.Handler

class LoadingActivity(val myActivity: Activity) {
    private lateinit var isDialog: AlertDialog
    val handle = android.os.Handler()

    fun startLoading(){
//        Set View
        val inflater = myActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
//        dialogView.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        Set Dialog

        val builder = AlertDialog.Builder(myActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isDialog.show()

        handle.postDelayed(
            {
                isDismiss()
            }, 8000
        )
    }

    fun isDismiss(){
        isDialog.dismiss()

    }
}