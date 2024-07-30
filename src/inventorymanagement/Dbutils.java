package inventorymanagement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
public class Dbutils {
	public static TableModel resultSetToTableModel(ResultSet rs) {
		try {
			ResultSetMetaData metadata=rs.getMetaData();
			int numberofcolumns=metadata.getColumnCount();
			Vector columnnames=new Vector();
			for (int column=0;column<numberofcolumns;column++) {
				columnnames.addElement(metadata.getColumnLabel(column+1));
			}
			Vector rows=new Vector();
			while(rs.next())
			{
				Vector newrow=new Vector();
				for(int i=1;i<=numberofcolumns;i++) {
					newrow.addElement(rs.getObject(i));
				}
				rows.addElement(newrow);
				
			}
			return new DefaultTableModel(rows,columnnames);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
