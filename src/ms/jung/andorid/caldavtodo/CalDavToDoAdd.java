package ms.jung.andorid.caldavtodo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

public class CalDavToDoAdd extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Window specific stuff
        setContentView(R.layout.add);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);      
        LayoutParams params = getWindow().getAttributes(); 
        params.width = LayoutParams.FILL_PARENT; 
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        
        Button okButton = (Button)findViewById(R.id.addOkButton);
        okButton.setOnClickListener
        (
        	new OnClickListener()
        	{
        		public void onClick(View v)
        		{
        			EditText tv = (EditText) findViewById(R.id.addTodoText);
        			Intent intent = getIntent();
       			    intent.putExtra("todo", tv.getText().toString());
        			setResult(RESULT_OK, intent);
        			finish();
        		}
        	}
        );
    }
}