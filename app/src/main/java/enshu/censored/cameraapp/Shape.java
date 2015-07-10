package enshu.censored.cameraapp;

class Shape {
	public enum Genre {
		Rock, Scizers, Paper, Noise, NoGesture, BackGround
	};

	public static int toInt(Genre d) {
		int k = 0;
		switch (d) {
		case Rock:
			return 1;
		case Scizers:
			return 2;
		case Paper:
			return 3;
		case Noise:
			return 4;
		case NoGesture:
			return 5;
		default:
			return 0;
		}
	}

	public static Shape.Genre valueOf(int i) {
		switch (i) {
		case 1:
			return Genre.Rock;
		case 2:
			return Genre.Scizers;
		case 3:
			return Genre.Paper;
		case 4:
			return Genre.Noise;
		case 5:
			return Genre.NoGesture;
		default:
			return Genre.BackGround;
		}
	}

	public static Shape.Genre getDistance(int pixel) {
		final int Rock = 0x00FF0000;// red
		final int Scizers = 0x0000FF00;// green
		final int Paper = 0x000000FF;// blue
		final int NoGesture = 0x00FFFF00;// yellow
		final int Noise = 0x00FFFFFF;// noise
		switch (pixel & Noise) {
		case Rock:
			return Genre.Rock;
		case Scizers:
			return Genre.Scizers;
		case Paper:
			return Genre.Paper;
		case NoGesture:
			return Genre.NoGesture;
		case Noise:
			return Genre.Noise;
		}
		return Genre.BackGround;
	}

	public static boolean getBinary(int pixel) {
		return (pixel & 0x00FFFFFF) != 0;
	}
}
