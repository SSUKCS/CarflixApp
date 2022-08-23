package com.example.carflix;

import android.app.Dialog;
import android.content.Context;
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
    public void setText(String text){
     loadingText.setText(text);
    }
    public void setTextColor(int color){
        loadingText.setTextColor(color);
    }
}
