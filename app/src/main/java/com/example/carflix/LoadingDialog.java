package com.example.carflix;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

class LoadingDialog extends Dialog
{
    TextView loadingText;
    public LoadingDialog(Context context)
    {
        super(context);
        setContentView(R.layout.loading_dialog);
        loadingText = findViewById(R.id.loadingText);

        setCancelable(false);
    }
    public interface DialogBackPressed{
        void onBackPressed();
    }
    private DialogBackPressed dialogBackPressed;
    public void registerBackPressed(DialogBackPressed dialogBackPressed){
        this.dialogBackPressed = dialogBackPressed;
    }
    public void setText(String text){
     loadingText.setText(text);
    }
    public void setTextColor(int color){
        loadingText.setTextColor(color);
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(dialogBackPressed != null)
            dialogBackPressed.onBackPressed();
        if(this.isShowing())this.cancel();
    }
}
