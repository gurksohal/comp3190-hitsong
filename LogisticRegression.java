

public class LogisticRegression {

	private double learnRate;
	private double[] weights;
	private double bias;
	private double[][] trainX;
	private int[] trainY;
	private double[][] testX;
	private int[] testY;

	public LogisticRegression(double[][] trainX, int[] trainY, double[][] testX, int[] testY) {
		this.trainX = trainX;
		this.trainY = trainY;
		this.testX = testX;
		this.testY = testY;
		learnRate = 0.01;
		bias = 0;

		weights = new double[trainX[0].length]; // same amount of weights as features
		for (int i = 0; i < weights.length; i++) { // set starting weights to zero
			weights[i] = 0.0;
		}
	}

	public void train() {
		for (int x = 0; x < 1000; x++) {
			for (int row = 0; row < trainX.length; row++) {
				double y = predict(trainX[row]);

				// y is current predicted value for this song
				// adjust all the weights so next loop y is closer to the real value
				// to update weights its w = w - lr(dw)
				// dw for the iTH feature = feature*(y - label)
				// dw the slope of the function, which we want to minimize

				double db = y - trainY[row];
				// update the weights
				for (int i = 0; i < weights.length; i++) {
					weights[i] = weights[i] - (learnRate * trainX[row][i] * db);
				}

				// update bias
				// to update bias its b = b - lr(db)
				// db = (y-label)
				bias = bias - (learnRate * db);
			}
		}
	}

	public double test() {
		int[] returnVal = new int[testX.length];
		
		for (int row = 0; row < testX.length; row++) {
			returnVal[row] = getBinaryAnswer(testX[row]);
			System.out.println("Guess: " + returnVal[row] + " - Real: " + testY[row]);
		}
		System.out.println("accuracy: " + acc(returnVal));
		return acc(returnVal);
	}

	private double predict(double[] song) {
		// guess = sig(Bias + W1(X1) + W2(X2) + ... + Wn(Xn))
		double guess = 0.0;
		for (int i = 0; i < weights.length; i++) {
			guess += weights[i] * song[i];
		}

		return sigmoid(guess + bias);
	}

	private int getBinaryAnswer(double[] x) {
		if (predict(x) >= 0.5) {
			return 1;
		} else {
			return 0;
		}
	}

	private double acc(int[] predict) {
		int[] diff = new int[predict.length];
		for(int x = 0; x < diff.length; x++) {
			diff[x] = predict[x] - testY[x];
		}
		
		double count = 0.0;
		for(int x = 0; x < diff.length; x++) {
			if(diff[x] != 0) {
				count++;
			}
		}
		
		return 1.0 - (count / diff.length);
	}

	private double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}
}
