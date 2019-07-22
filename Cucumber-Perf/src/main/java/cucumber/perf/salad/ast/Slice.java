package cucumber.perf.salad.ast;

import java.util.List;

import gherkin.ast.DataTable;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;

/**
 * Slice represents a DataTable with header and single row.
 * Used in replacing existing step values.
 * @author Matt Pinardi
 */
public class Slice extends DataTable {

	public Slice(List<TableRow> rows) {
		super(rows);
	}
	
	/**
	 * Replaces any parameter values that the slice contains.
	 * @param value The step string.
	 * @return A replaced step string.
	 */
	public String replaceParameter(String value) {
		int loc = findParameter(value);
		if (loc >= 0) {
			value = value.replace("\"" + this.getRows().get(0).getCells().get(loc).getValue() + "\"",
					"\"" + this.getRows().get(1).getCells().get(loc).getValue() + "\"");
		}
		return value;
	}

	public boolean hasParameter(String value) {
		return findParameter(value) >= 0 ? true : false;
	}

	private int findParameter(String value) {
		int i = 0;
		if (this.getRows().size() > 0) {
			for (TableCell c : this.getRows().get(0).getCells()) {
				if (value.contains("\"" + c.getValue() + "\"")) {
					if (this.getRows().get(1).getCells().size() >= i) {
						return i;
					}
				}
				i++;
			}
		}
		return -1;
	}
}
