package org.vitrivr.cineast.core.som;

// code base from https://github.com/dashaub/kohonen4j

/**
 * Fit a self-organizing map to a dataset.
 *
 * Copyright (C) 2016 David Shaub
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author David Shaub
 * @version 1.1.0
 *
 * */

import java.io.File;
import java.io.IOException;
import java.util.*;
public class SOM {

	// X dimension of the map
	private final int xDim;
	// Y dimension of the map
	private final int yDim;
	// Number of training rounds
	private final int epochs;
	// Ordered pairs for all the points on the map
	private int [][] pairArray;
	// Weights fitted during training
	private double [][] weights;
	// Final node assigned to each observation in training
	private int[] finalNodes;
	// The distance from each data point to the final node
	private double[] finalDistances;

	public int[] nearestEntryOfNode;

	private ArrayList<String> ids;
	private int numColumns;
	private ArrayList<float[]> inputData;
	private Grid grid;

	private static SOM som;

	public static SOM getInstance() {
		return som;
	}

	public static SOM setInstance(int xDim, int yDim, int epochs){
		return som = new SOM(xDim, yDim, epochs);
	}

	private SOM(int xDim, int yDim, int epochs)
	{
		// Only allow positive xDim and yDim
		if(xDim <= 0 || yDim <= 0)
		{
			throw new IllegalArgumentException();
		}
		this.xDim = xDim;
		this.yDim = yDim;
		this.epochs = epochs;
	}


	/**
	 * Getter method for finalDistances.
	 *
	 * This method returns the final distances
	 * to the assigned node for each observation
	 * after training.
	 *
	 * @return An array with the distance
	 * to the assigned node for each observation
	 *
	 * */
	public double [] getDistances()
	{
		return this.finalDistances;
	}


	/**
	 * Getter method for finalNodes.
	 *
	 * This method returns the final node
	 * labels assigned to each observation
	 * after training
	 *
	 * @return An array with the node labels
	 * for each observation
	 *
	 * */
	public int [] getNodes()
	{
		return finalNodes;
	}

	public ArrayList<String> getIds() {
		return ids;
	}


	/**
	 * Train the SOM to the data.
	 * This method runs the training
	 * algorithm to fit the self-organizing
	 * maps to the training data
	 *
	 * */
	public void train()
	{
		this.init();
		// Number of rows in the training data
		int dataRows = grid.gridData.length;
		// Number of columns in the training data
		int dataColumns = grid.gridData[0].length;
		// Number of rows (same number of columns) in the weights map
		int weightsRows = this.weights.length;
		// Number of columns in the weights map
		int weightsColumns = this.weights[0].length;
		// Number of rounds of training
		int iterations = this.epochs * dataRows;
		// Initial learning rate
		double learningRate = 0.5; // 0.5 seems to work fine
		// Initial neighborhood size
		double neighborhood;

		// Current row being processed
		int currentObs;
		// Nearest node to the current point
		int nearest = 0;
		// Smallest identified distance to a node
		double nearestDistance;

		// Temporary variables for the current distance to a node
		double dist;
		double tmp;

		// "Unpack" the gridData, weights, and pair distances into a 1D array
		double [] data = new double[dataRows * dataColumns];
		double [] nodes = new double[weightsRows * weightsColumns];
		double [] distPairs = new double[this.pairArray.length * this.pairArray.length];
		int count = 0;
		for(int i = 0; i < dataColumns; i++)
		{
			for(int j = 0; j < dataRows; j++)
			{
				data[count] = grid.gridData[j][i];
				count++;
			}
		}
		count = 0;
		for(int i = 0; i < weightsColumns; i++)
		{
			for(int j = 0; j < weightsRows; j++)
			{
				nodes[count] = this.weights[j][i];
				count++;
			}
		}
		count = 0;
		int currentX;
		int currentY;
		double xDist;
		double yDist;
		for(int i = 0; i < this.pairArray.length; i++)
		{
			// Set the reference point to the current row
			currentX = this.pairArray[i][0];
			currentY = this.pairArray[i][1];
			for(int j = 0; j < this.pairArray.length; j++)
			{
				// Calculate the rectilinear distances from this point
				// to the reference point
				xDist = Math.abs(this.pairArray[j][0] - currentX);
				yDist = Math.abs(this.pairArray[j][1] - currentY);
				distPairs[count] = xDist + yDist;
				count++;
			}
		}

		// Set the neighborhood to capture approximately 2/3 of the nodes.
		// This is approximately 1.75 * variance (See Chebychev's inequality)
		// https://en.wikipedia.org/wiki/Chebyshev's_inequality
		neighborhood = 1.75 * grid.variance(distPairs);
		//double stepNH = neighborhood/iterations;
		double stepLR = learningRate/iterations;

		// Adapted from the C code for VR_onlineSOM in the R "class" package
		long lastprogress = -1;
		double initLR = learningRate;
		double initNH = neighborhood;
		for(int i = 0; i < iterations; i++)
		{
			long progress = i*100L/iterations;
			if (lastprogress != progress && progress % 10 == 0) {
				System.out.println("Progress @ "+progress);
				System.out.println("learningRate: "+learningRate);
				System.out.println("neighborhood: "+neighborhood);
				//System.out.println("delta lr: "+stepLR);
				//System.out.println("delta nh: "+stepNH);
				lastprogress = progress;
			}
			// Choose a random observation for fitting
			currentObs = (int)(Math.random() * dataRows);
			// Find its nearest node
			// Start with the maximum distance possible
			nearestDistance = Double.MAX_VALUE;
			nearest = 0;
			for(int j = 0; j < weightsRows; j++)
			{
				// Reset the distance to zero for the next training point
				dist = 0;
				for(int k = 0; k < dataColumns; k++)
				{
					// For the current random observation and the current column,
					// find the difference
					tmp = data[currentObs + k * dataRows] - nodes[j + k * weightsRows];
					// dist^2 is the square of the sums of
					// all the individual components, and
					// minimizing distance^2 leads to the same
					// node as minimizing distance.
					dist += (tmp * tmp);
				}
				// New closest node found
				if(dist < nearestDistance)
				{
					// Update the nearest node and distance
					nearest = j;
					nearestDistance = dist;
				}
			}

			// Update learning rate and neighborhood distances
			// Initially "pull" the map by large amounts and
			// pull nodes that are farther away (but within the
			// neighborhood as well); as training
			// continues create smaller distortions and apply
			// them within a smaller neighborhood.

			learningRate -= stepLR; // 0.004 ~fine (0.04 orig)
			//neighborhood -= stepNH;

			double exp = Math.exp(-progress*3/100d);
			//learningRate = initLR*exp;
			neighborhood = initNH*exp;

			if (learningRate <= 0) {
				System.out.println("NEGATIVE LEARNING RATE!!!");
				break;
			}
			if (neighborhood <= 0) {
				System.out.println("NEGATIVE NEIGHBORHOOD!!!");
				break;
			}

			// Apply the distortion to the map for nodes within
			// the neighborhood
			for(int l = 0; l < weightsRows; l++)
			{
				// Apply if the distance to the other node is within the neighborhood
				if(distPairs[l + weightsRows * nearest] <= neighborhood)
				{
					// Apply to all columns in this row
					for(int m = 0; m < dataColumns; m++)
					{
						tmp = data[currentObs + m * dataRows] - nodes[l + m * weightsRows];
						nodes[l + m * weightsRows] += (tmp * learningRate);
					}
				}
			}
		}

		// Adapted from the C code for mapKohonen in the R "kohonen" package
		// Now calculate the weights/data to map distance
		count = 0;
		double [] mapDist = new double[dataRows * weightsRows];
		// Loop over all data points
		for(int i = 0; i < dataRows; i++)
		{
			// Loop over all the map nodes
			for(int j = 0; j < weightsRows; j++)
			{
				mapDist[count] = 0;
				// Loop over all the variable
				for(int k = 0; k < weightsColumns; k++)
				{
					tmp = data[i + k * dataRows] - nodes[j + k * weightsRows];
					mapDist[count] += (tmp * tmp);
				}
				count++;
			}
		}

		// Represent the map distances as the original matrix
		count = 0;
		double [][] distanceMatrix = new double [dataRows][weightsRows];
		for(int i = 0; i < dataRows; i++)
		{
			for(int j = 0; j < weightsRows; j++)
			{
				distanceMatrix[i][j] = mapDist[count];
				count++;
			}
		}

		// Finally label the observations with the nearest node
		// to complete the map training
		finalNodes = new int[dataRows];
		finalDistances = new double[dataRows];
		nearestEntryOfNode = new int[weightsRows];
		Arrays.fill(nearestEntryOfNode, -1);

		int nodeIndex;
		double minDistance;
		for(int i = 0; i < distanceMatrix.length; i++)
		{
			count = 0;
			nodeIndex = count;
			minDistance = Double.MAX_VALUE;
			for(int j = 0; j < weightsRows; j++)
			{
				if(distanceMatrix[i][j] < minDistance)
				{
					minDistance = distanceMatrix[i][j];
					nodeIndex = count;
				}
				count++;
			}
			finalNodes[i] = nodeIndex;
			finalDistances[i] = minDistance;
			if (nearestEntryOfNode[nodeIndex] == -1 || minDistance < finalDistances[nearestEntryOfNode[nodeIndex]]) nearestEntryOfNode[nodeIndex] = i;
		}
	}

	/**
	 * Initialize the SOM object for training
	 * This method prepares the SOM for
	 * training by initializing the random
	 * weights with a bootstrap sample from
	 * the training data.
	 *
	 * */
	private void init()
	{
		// Scale the Grid
		grid.scaleGrid();

		// Prepare the array for
		// calculating pair distances
		pairArray = new int[this.xDim * this.yDim][2];
		int count = 0;
		for(int i = 0; i < this.xDim; i++)
		{
			for(int j = 0; j < this.yDim; j++)
			{
				this.pairArray[count][0] = i;
				this.pairArray[count][1] = j;
				count++;
			}
		}

		// Useful variables
		int pairRows = this.pairArray.length;
		int dataRows = grid.gridData.length;


		// Sample from the data to determine
		// which observations to use
		// for initial weights
		Set <Integer> samplePoints = new HashSet <>();
		while(samplePoints.size() < pairRows)
		{
			samplePoints.add((int)(Math.random() * dataRows));
		}
		Integer [] sampleIndex = samplePoints.toArray(new Integer[0]);

		// Use the selected rows to build the starting weights
		weights = new double[pairRows][grid.gridData[0].length];
		int weightCount = 0;
		// Select the rows from sampleIndex
		for(Integer i : sampleIndex)
		{
			// Select all the columns in the row
			for(int j = 0; j < grid.gridData[0].length; j++)
			{
				weights[weightCount][j] = grid.gridData[i][j];
			}
			weightCount++;
		}
	}

	/**
	 * Read in the input csv data
	 * This function reads the input data
	 * from a csv for constructing the
	 * self-organizing map. The function also
	 * checks the data to make sure it is valid
	 * and satisfies the necessary conditions
	 * to construct a self-organizing map.
	 *
	 * @param input The file object to read in
	 *
	 * */
	private void setupFromFile(File input) throws IOException
	{
		// Open a Scanner
		Scanner inFile;
		try
		{
			inFile = new Scanner(input);
			inFile.useDelimiter("\n");
		}
		catch(IOException ioe)
		{
			throw new IOException("Cannot read file. Select a readable file.");
		}

		// Treat the first line as a header and use it
		// to determine the number of columns
		numColumns = 0;
		String currentLine = inFile.next();
		Scanner parser = new Scanner(currentLine);
		parser.useDelimiter(",");
		while(parser.hasNext())
		{
			parser.next();
			numColumns++;
		}
		numColumns--;
		// There should be at least two columns
		if(numColumns < 2)
		{
			throw new IOException("The file should have at least two columns.");
		}
		// Now read the data
		float[] currentRow;
		ids = new ArrayList<>();
		inputData = new ArrayList<>();
		while(inFile.hasNext())
		{
			currentLine = inFile.next();
			parser = new Scanner(currentLine);
			parser.useDelimiter(",");
			try
			{
				ids.add(parser.next());
				currentRow = new float[numColumns];
				// Ignore data in rows with more entries than in the header
				for(int i = 0; i < numColumns; i++)
				{
					currentRow[i] = Float.parseFloat(parser.next());
				}
				inputData.add(currentRow);
			}
			// Ensure the data are parsed to numeric
			catch(NumberFormatException nfe)
			{
				throw new IOException("The file should contain only numeric data.");
			}
			// Ensure a non-jagged array
			catch(NoSuchElementException nsee)
			{
				throw new IOException("Every row should contain one number for every columns in the file header.");
			}
		}

		// Ensure # rows >= # cols
		if (inputData.size() < numColumns) {
			throw new IOException("There must be at least as many data rows as columns in the file.");
		}
	}

	public SOM trainSOM(String filename) throws IOException {
		setupFromFile(new File(filename));
		return getTrainedSom();
	}

	//TODO use relevance feedback interface
	public SOM trainSOM(String filename, ArrayList<String> ids, ArrayList<float[]> vectors) throws IOException {
		setupFromFile(new File(filename));
		System.out.println("size: "+this.ids.size()+" "+inputData.size());
		if (ids.size() != vectors.size() || numColumns!=0 && !vectors.isEmpty() && vectors.get(0).length != numColumns) throw new IOException("Invalid array");
		this.ids.addAll(ids);
		this.inputData.addAll(vectors);
		System.out.println("size: "+this.ids.size()+" "+inputData.size());
		return getTrainedSom();
	}

	public SOM trainFromArrayOnly(ArrayList<String> ids, ArrayList<float[]> vectors) throws IOException {
		if (ids.size() != vectors.size() || numColumns!=0 && !vectors.isEmpty() && vectors.get(0).length != numColumns)
			throw new IOException("Invalid array");
		this.numColumns = vectors.get(0).length;
		this.ids = ids;
		inputData = vectors;
		return getTrainedSom();
	}

	private SOM getTrainedSom() {
		double[][] validData = new double[inputData.size()][numColumns];
		float[] tmpArray;
		for(int i = 0; i < inputData.size(); i++)
		{
			tmpArray = inputData.get(i);
			for(int j = 0; j < numColumns; j++)
			{
				validData[i][j] = tmpArray[j];
			}
		}

		// Convert to a grid object
		if (xDim <= 0 || yDim <= 0 || epochs <= 0) throw new NumberFormatException();

		// Create the SOM object
		long startTime = System.nanoTime();
		grid = new Grid(validData);
		// Train the Kohonen network
		train();
		long endTime = System.nanoTime();

		System.out.println("training time: " + (endTime - startTime) / 1000000000 + "s");
		//metric();

		return this;
	}

	public void metric() {
		int [] counts = new int[xDim * yDim];
		int average =  getDistances().length / counts.length;
		int lowerBound = (int)(average * 0.7);
		int upperBound = (int)(average * 1.3);
		int diff = 0;
		int tooLow = 0;
		int tooHigh = 0;
		int emptyCells = 0;
		for (int node : getNodes()) {
			counts[node] += 1;
		}
		for (int c : counts) {
			diff += Math.abs(c-average);
			if (c < lowerBound) tooLow++;
			if (c > upperBound) tooHigh++;
			if (c == 0) emptyCells++;

		}
		float fHigh = tooHigh*1f / counts.length;
		float fLow = tooLow*1f / counts.length;
		System.out.println("diff: "+diff+" empty: "+emptyCells+" fLow: "+fLow+" fHigh: "+fHigh);
	}
}
