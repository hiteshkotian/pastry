import java.util.Scanner;

/**
 * Class simulating the routing table for the Pastry DHT.
 * 
 * @author Hitesh Chidambar Kotian.
 * @author Peeral Malani.
 * @author Pratik Rasam.
 * 
 */
public class RoutingTable {

	// Number of rows in the routing table.
	private int rows;

	// Number of columns in the routing table.
	private int columns;

	// Configuration Parameter of the system.
	private int b;

	// The table storing the node Id's of the neighbors.
	private String[][] table;

	// Values of each column of the routing table.
	private String[] columnValues;

	// The base of the nodeId's of the system.
	private int power;

	// Node Id of the node which contains the routing table.
	private final String nodeId;

	/**
	 * Constructor for the routing table.
	 * 
	 * @param b
	 *            Configuration Parameter
	 * @param N
	 *            Total number of nodes in the system.
	 * @param nodeId
	 *            Node ID of the node containing the routing table.
	 */
	public RoutingTable(int N, String nodeId, int b) {
		this.power = (int) Math.pow(2, b);
		rows = (int) Math.ceil(Math.log10(N) / Math.log10(power));
		columns = power;
		this.nodeId = nodeId;
		table = new String[rows][columns];
		this.b = b;
		// Compute the column values.
		columnValues = new String[columns];
		setColumnValues();
	}

	/**
	 * Function that computes the values every column represents in the table.
	 */
	public void setColumnValues() {
		for (int i = 0; i < this.columns; i++) {
			columnValues[i] = changeBase(i);
		}
	}

	/**
	 * Function that adds a node in the routing table.
	 * 
	 * @param newId
	 *            Node ID of the new node to be added.
	 * 
	 */
	public void addNode(String newId) {
		// The number of rows would be the degree of match between the node ID
		// of the new node and the node ID of the current Node.
		int row = getDegreeMatch(newId, this.nodeId);

		// If the degree match of the two string is more than the number of
		// rows, then we cannot accommodate the new id in the routing table.
		// Hence we return to the function caller.
		if (row >= this.rows || row >= newId.length()) {
			return;
		}

		// Get the character which is not matching. The next character's value
		// decides the column to which it will be added.
		String nextChar = "" + newId.charAt(row);
		int position = 0;
		for (int i = 0; i < columnValues.length; i++) {
			if (nextChar.equals(columnValues[i])) {
				position = i;
				break;
			}
		}
		if (row >= this.rows || position >= this.columns) {
			return;
		}
		if (this.table[row][position] != null) {
			Scanner scan = new Scanner(this.table[row][position]);
			int power = (int) (Math.pow(2, this.b));
			long current = scan.nextLong(power);
			scan.close();
			scan = new Scanner(newId);
			long newIdVal = scan.nextLong(power);
			scan.close();

			scan = new Scanner(this.nodeId);
			long nodeIdVal = scan.nextLong(power);
			scan.close();

			long diff1 = Math.abs(nodeIdVal - newIdVal);
			long diff2 = Math.abs(nodeIdVal - current);

			if (diff1 < diff2) {
				this.table[row][position] = newId;
			}
		} else {
			this.table[row][position] = newId;
		}

	}

	/**
	 * Function that returns the next node to be visited to reach the key.
	 * 
	 * @param key
	 *            the node id of the destination.
	 * 
	 * @return node ID of the next key.
	 */
	public String getNextNode(String key) {
		// If the node ID is equal to the current node ID, return it.
		if (key.equals(this.nodeId)) {
			return this.nodeId;
		}
		// Otherwise find the key with the highest degree match.
		int degreeMatch = getDegreeMatch(key, this.nodeId);

		if (degreeMatch >= this.rows) {
			degreeMatch = rows - 1;
		}

		String[] selectedRow = this.table[degreeMatch];
		for (int i = 0; i < selectedRow.length; i++) {
			if (key.equals(selectedRow[i])) {
				return key;
			}
		}
		int longerDegreeMatch = degreeMatch;
		String longerDegreeMatchNode = this.nodeId;
		for (int i = degreeMatch + 1; i < this.rows; i++) {
			String[] consideredRow = this.table[i];
			for (int j = 0; j < consideredRow.length; j++) {
				if (consideredRow[j] == null) {
					continue;
				}
				int newDegreeMatch = getDegreeMatch(consideredRow[j], key);
				if (newDegreeMatch > longerDegreeMatch) {
					longerDegreeMatch = newDegreeMatch;
					longerDegreeMatchNode = consideredRow[j];
				}
			}
		}

		// If no key is found with a higher degree match, find a key which is
		// numerically closer to the given key.
		if (longerDegreeMatchNode.equals(this.nodeId)) {
			String[] consideredRow = this.table[degreeMatch];
			for (int j = 0; j < consideredRow.length; j++) {
				if (consideredRow[j] == null) {
					continue;
				}
				int newDegreeMatch = getDegreeMatch(consideredRow[j], key);
				if (newDegreeMatch > longerDegreeMatch) {
					longerDegreeMatch = newDegreeMatch;
					longerDegreeMatchNode = consideredRow[j];
				}
			}
		}
		return longerDegreeMatchNode;
	}

	/**
	 * Function that returns the degree of prefix match between the two strings.
	 * 
	 * @param nodeId1
	 *            Node ID of the first node.
	 * @param nodeId2
	 *            Node ID of the second node.
	 * 
	 * @return Degree of Prefix Matching between the two strings.
	 */
	public int getDegreeMatch(String nodeId1, String nodeId2) {
		// For the least length of the two strings, iterate from character to
		// character till the time a non-equal character is encountered.
		int len = Math.min(nodeId1.length(), nodeId2.length());
		int degreeMatch = 0;
		for (int i = 0; i < len; i++) {
			if (nodeId1.charAt(i) == nodeId2.charAt(i)) {
				degreeMatch++;
			} else {
				break;
			}
		}
		return degreeMatch;
	}

	/**
	 * Function that converts the base of a decimal number to the base 2 ^ b.
	 * 
	 * @param from
	 *            The decimal value.
	 * 
	 * @return The changed base value of the decimal number.
	 */
	private String changeBase(int from) {
		if (from == 0) {
			return "0";
		}
		long temp = from;
		String finalId = "";
		while (temp != 0) {
			long modulo = temp % power;
			String mod = "";
			if (modulo == 10) {
				mod = "A";
			} else if (modulo == 11) {
				mod = "B";
			} else if (modulo == 12) {
				mod = "C";
			} else if (modulo == 13) {
				mod = "D";
			} else if (modulo == 14) {
				mod = "E";
			} else if (modulo == 15) {
				mod = "F";
			} else {
				mod = "" + modulo;
			}
			finalId = mod.concat(finalId);
			temp = temp / power;
		}
		return finalId;
	}

	/**
	 * Function that prints the routing table.
	 */
	public void printTable() {
		for (int i = 0; i < this.columns; i++) {
			System.out.print(this.columnValues[i] + "\t");
		}
		System.out.println();
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				System.out.print(this.table[i][j] + " \t");
			}
			System.out.println();
		}
	}
}
