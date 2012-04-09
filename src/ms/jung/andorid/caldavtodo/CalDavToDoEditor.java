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

public class CalDavToDoEditor extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Window specific stuff
        setContentView(R.layout.editor);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);      
        LayoutParams params = getWindow().getAttributes(); 
        params.width = LayoutParams.FILL_PARENT; 
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        
        // get data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
       		String todo = (String)extras.get("todo");
			EditText tv = (EditText) findViewById(R.id.editorTodoText);
			tv.setText(todo);
        }
        
        Button okButton = (Button)findViewById(R.id.editorOkButton);
        okButton.setOnClickListener
        (
        	new OnClickListener()
        	{
        		public void onClick(View v)
        		{
        			EditText tv = (EditText) findViewById(R.id.editorTodoText);
        			Intent intent = getIntent();
       			    intent.putExtra("todo", tv.getText().toString());
       			    intent.putExtra("state", "edit");
        			setResult(RESULT_OK, intent);
        			finish();
        		}
        	}
        );
        Button deleteButton = (Button)findViewById(R.id.editorDeleteButton);
        deleteButton.setOnClickListener
        (
        	new OnClickListener() 
        	{
        		public void onClick(View v)
        		{
        			Intent intent = getIntent();
       			    intent.putExtra("state", "delete");
        			setResult(RESULT_OK, intent);
        			finish();
        		}
        	}
        );
    }
}