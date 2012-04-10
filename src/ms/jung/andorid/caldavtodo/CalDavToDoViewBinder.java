package ms.jung.andorid.caldavtodo;


import android.database.Cursor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/// For special values, which are supposed to go to non-text fields we have this special ViewBinder
class CalDavToDoViewBinder implements SimpleCursorAdapter.ViewBinder
{

    public boolean setViewValue(View view, Cursor cursor, int columnIndex)
    {
        int viewId = view.getId();
        
        if(viewId == R.id.checkBox)
        {
                CheckBox cb = (CheckBox) view; 
                
				if(cursor.getInt(cursor.getColumnIndexOrThrow(CalDavToDoProvider.STATE)) == 1)
				{
					cb.setChecked(true); 
				}
				else 
				{
					cb.setChecked(false);
				}
				
				cb.setText(cursor.getString(cursor.getColumnIndexOrThrow(CalDavToDoProvider.TODO)));
				
				return true;

        }
        else if(viewId == R.id.colorBar)
        {
				int color = cursor.getInt(cursor.getColumnIndexOrThrow(CalDavToDoProvider.COLOR));

				TextView colorBar = (TextView)view;
				colorBar.setBackgroundColor(color);

				return true;	
        }
        
        return false;
    }
}