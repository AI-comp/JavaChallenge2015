import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class TestAI {
	private static final int PLAYERS_NUM = 4;
	private static final int FIELD_SIZE = 6;
	private static Scanner scanner;
	private static PrintWriter writer;

	private static int playerId;
	private static int T;
	private static int B[][];
	private static int R[];
	private static int C[];
	private static String D[];
	private static int S[];
	private static String EOD;

	private static int myLife;
	private static int enemyLives;
	private static String commands = "URDLAN";
	private static Queue<String> command;

	public static void main(String[] args) {
		B = new int[FIELD_SIZE][FIELD_SIZE];
		R = new int[PLAYERS_NUM];
		C = new int[PLAYERS_NUM];
		D = new String[PLAYERS_NUM];
		S = new int[PLAYERS_NUM];
		command = new LinkedList<String>();

		if (args.length >= 2) {
			if (args[0].equals("-c")) {
				for (int i = 1; i < args.length; i++) {
					command.add(args[i]);
				}
			} else if (args[0].equals("-f")) {
				getCommandFromFile(args[1]);
			}
		}
		scanner = new Scanner(System.in);
		writer = new PrintWriter(System.out, true);
		writer.println("READY");
		writer.flush();

		while (true) {
			readData();

			enemyLives = 0;
			for (int i = 0; i < PLAYERS_NUM; i++) {
				if (i != playerId && !isDead(i)) {
					enemyLives++;
				}
			}
			if (isDead(playerId) || enemyLives == 0) {
				break;
			}

			writeCommand();
		}

		scanner.close();
		writer.close();
	}

	private static boolean isDead(int id) {
		return R[id] == -1 && C[id] == -1;
	}

	private static void readData() {
		playerId = scanner.nextInt();
		T = scanner.nextInt();
		for (int i = 0; i < FIELD_SIZE; i++) {
			for (int j = 0; j < FIELD_SIZE; j++) {
				B[i][j] = scanner.nextInt();
			}
		}
		for (int i = 0; i < PLAYERS_NUM; i++) {
			R[i] = scanner.nextInt();
			C[i] = scanner.nextInt();
			D[i] = scanner.next();
			S[i] = scanner.nextInt();
		}
		EOD = scanner.next();
	}

	private static void writeCommand() {
		String cmd = !command.isEmpty() ? command.poll() : "N";
		writer.println(cmd);
		writer.flush();
	}

	private static void getCommandFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				command.add(scanner.next());
			}
		} catch (Exception e) {
			return;
		}
	}
}