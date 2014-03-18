import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.Series;
import edu.rit.numeric.Statistics;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import edu.rit.util.Random;
import edu.rit.util.RandomSubset;

/**
 * Program computes and plots H versus N for a range of number of nodes
 * 
 * @author Peeral Malani
 * @author Hitesh Chidambar Kotian
 * @author Pratik Rasam
 */

public class PastryMain {
	public static void main(String[] args) {
		if (args.length != 5) {
			System.exit(1);
		}

		// Get all the values for the parameters from the command line.
		int NLower = Integer.parseInt(args[0]);
		int NUpper = Integer.parseInt(args[1]);
		int NInc = Integer.parseInt(args[2]);
		int messages = Integer.parseInt(args[3]);
		long seed = Long.parseLong(args[4]);

		// Create an instance of Random.
		Random r = Random.getInstance(seed);

		// Create an instance of RandomSubset to choose a source and destination
		// for routing from the list of node IDs.
		RandomSubset rand;

		System.out.println("Number of Nodes - Lower Bound: " + NLower);
		System.out.println("Number of Nodes - Upper Bound: " + NUpper);
		System.out.println("Increment: " + NInc);
		System.out.println("Number of trials: " + messages);
		System.out.println();

		// Create an array list to store all the node Id's for the system.
		ArrayList<Long> keyList = new ArrayList<Long>();
		for (int i = 0; i < NUpper; i++) {
			long num = r.nextLong();
			if (num < 0) {
				num = num * -1;
			}
			keyList.add(num);
		}

		// Sort all the keys produced in ascending order.
		Collections.sort(keyList);

		// Create a hashmap that stores all the node id's that were previously
		// visited.
		HashMap<Long, Integer> visited;

		// Store the expectedHopSeries for each value of b
		ListXYSeries expectedHopSeries1 = new ListXYSeries();
		ListXYSeries expectedHopSeries2 = new ListXYSeries();
		ListXYSeries expectedHopSeries3 = new ListXYSeries();
		ListXYSeries expectedHopSeries4 = new ListXYSeries();

		ListXYSeries ex = new ListXYSeries();

		// Vary the value of b from 1 to 4
		for (int b = 1; b < 5; b++) {
			System.out.println("For b = " + b);
			System.out.println("\n N \t H");
			ListXYSeries NHSeries = new ListXYSeries();
			ListXYSeries NH1Series = new ListXYSeries();
			double chi2 = 0.0;
			for (int N = NLower; N <= NUpper; N += NInc) {
				System.out.print(N);
				ListSeries HMeanSeries = new ListSeries();
				ListSeries series = new ListSeries();
				visited = new HashMap<Long, Integer>();
				for (int mess = 0; mess < messages; mess++) {
					ListSeries Hseries = new ListSeries();
					for (int m = 0; m < messages; m++) {
						rand = new RandomSubset(r, N);
						int destNo = rand.next();
						int initNo = rand.next();
						visited.put(keyList.get(initNo), 0);
						int minBound;
						int maxBound;

						int iteration = 0;

						while (true) {

							// Create an instance of the routing table.
							RoutingTable routing = new RoutingTable(N,
									changeBase(keyList.get(initNo), b), b);

							// Set the minimum and maximum bounds for the
							// indices of the keys to be selected to be stored
							// in the routing table.
							if (destNo < initNo) {
								minBound = destNo;
								maxBound = initNo - 1;
							} else {
								minBound = initNo + 1;
								maxBound = N;
							}

							// Add only those node Id's in the routing table
							// that are have not been visited before.
							for (int i = minBound; i < maxBound; i++) {
								if (i == initNo) {
									continue;
								}
								if (visited.containsKey(keyList.get(i))) {
									continue;
								}
								routing.addNode(changeBase(keyList.get(i), b));
							}
							long dest = keyList.get(destNo);
							long next;

							String nextStr = routing.getNextNode(changeBase(
									dest, b));

							Scanner scan = new Scanner(nextStr);
							next = scan.nextLong((int) Math.pow(2, b));
							scan.close();

							int newSource = keyList.indexOf(next);
							iteration++;

							if (next == dest) {
								break;
							}
							if (visited.containsKey(next)) {
								break;
							}
							initNo = newSource;
							visited.put(next, 0);

						}
						Hseries.add(iteration);
					}
					Series.Stats stats = Hseries.stats();
					double H = stats.mean;

					NH1Series.add(N, H);
					System.out.print("\t" + H);

					HMeanSeries.add(H);
					series.add(Hseries.stats().mean);

				}
				Series.Stats stats = HMeanSeries.stats();
				double HMean = stats.mean;
				double HStdDev = stats.stddev;

				double expected = (Math.log10(N) / Math.log10(Math.pow(2, b)));

				double chi = (HMean - expected) / HStdDev;

				// Calculate the chi^2 value for each value of b
				chi2 += chi * chi;

				System.out
						.printf("\n\t Mean = %.2f, Expected = %.2f, Standard Deviation = %.2f",
								HMean, expected, HStdDev);

				if (b == 1) {
					expectedHopSeries1.add(N, expected);
					NHSeries.add(N, HMean);
					ex = expectedHopSeries1;
				} else if (b == 2) {
					expectedHopSeries2.add(N, expected);
					NHSeries.add(N, HMean);
					ex = expectedHopSeries2;
				} else if (b == 3) {
					expectedHopSeries3.add(N, expected);
					NHSeries.add(N, HMean);
					ex = expectedHopSeries3;
				} else {
					expectedHopSeries4.add(N, expected);
					NHSeries.add(N, HMean);
					ex = expectedHopSeries4;
				}

				System.out.println();
			}

			System.out.printf("\nchi^2   = %.5f%n", chi2);
			System.out.printf("p-value = %.5f%n%n",
					Statistics.chiSquarePvalue(NUpper - NLower + 1, chi2));

			// Plot the graph of H versus N for different values for the average
			// number of hops
			new Plot()
					.plotTitle(
							"Pastry Plot : Trials = " + messages + " b = " + b)
					.xAxisTitle("Number of nodes, N")
					.yAxisTitle("Mean number of hops").seriesStroke(null)
					.xySeries(NH1Series).yAxisStart(1)
					.yAxisEnd(ex.maxY() > 10 ? ex.maxY() : 10)
					.yAxisMajorDivisions(9).seriesColor(Color.red)
					.seriesDots(null).seriesStroke(Strokes.solid(1))
					.xySeries(ex).getFrame().setVisible(true);

			// Plot the graph of H versus N to determine whether the hypothesis
			// is true or false
			new Plot()
					.plotTitle(
							"Pastry Plot : Trials = " + messages + " b = " + b)
					.xAxisTitle("Number of nodes, N")
					.yAxisTitle("Mean number of hops").seriesStroke(null)
					.xySeries(NHSeries).yAxisStart(1)
					.yAxisEnd(ex.maxY() > 10 ? ex.maxY() : 10)
					.yAxisMajorDivisions(9).seriesColor(Color.red)
					.seriesDots(null).seriesStroke(Strokes.solid(1))
					.xySeries(ex).getFrame().setVisible(true);

		}

	}

	/**
	 * Converts the id to the base 2^b
	 * 
	 * @param id
	 *            The node ID
	 * @param b
	 *            The configuration parameter
	 * @return The nodeID with the base 2^b
	 */
	private static String changeBase(long id, int b) {
		long temp = id;
		long base = (long) Math.pow(2, b);
		String finalId = "";
		while (temp != 0) {
			long modulo = temp % base;
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
			temp = temp / base;
		}
		return finalId;
	}
}