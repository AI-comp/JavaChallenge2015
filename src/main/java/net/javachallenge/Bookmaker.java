package net.javachallenge;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Bookmaker {
    private static boolean DEBUG = true;
    public static boolean HUMAN = false;

    private static final int PLAYERS_NUM = 4;
    private static final int INITIAL_LIFE = 5;
    private static final int MAP_WIDTH = 40;
    private static final int BLOCK_WIDTH = 5;
    private static final int FORCED_END_TURN = 10000;
    private static final int PANEL_REBIRTH_TURN = 5;
    public static final int PLAYER_REBIRTH_TURN = 5;
    public static final int ATTACKED_PAUSE_TURN = 5;// 攻撃後の硬直している時間
    public static final int MUTEKI_TURN = 10;
    private static final int REPULSION = 7;// プレイヤーの反発範囲

    public static final String READY = "Ready";
    public static final String UP = "U";
    public static final String DOWN = "D";
    public static final String RIGHT = "R";
    public static final String LEFT = "L";
    public static final String ATTACK = "A";
    public static final String NONE = "N";

    private static Player[] players;
    private static Random rnd;
    private static int turn;
    private static int[][] board = new int[MAP_WIDTH][MAP_WIDTH];

    public static void main(String[] args) throws InterruptedException {
	Scanner scanner = new Scanner(System.in);
	if (DEBUG) {
	    args = new String[] { "-ai", "/home/nkenkou/Desktop/tekitoAI",
		    "-ai", "/home/nkenkou/Desktop/tekitoAI", "-ai",
		    "/home/nkenkou/Desktop/tekitoAI", "-ai",
		    "/home/nkenkou/Desktop/tekitoAI", };
	}

	String[] ais = new String[PLAYERS_NUM];
	for (int i = 0; i < PLAYERS_NUM; i++) {
	    ais[i] = "";
	}
	int cur = 0;
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-ai")) {
		if (cur == 4) {
		    continue;
		}
		ais[cur++] = args[++i];
	    } else if (args[i].equals("-debug")) {
		DEBUG = true;
	    } else if (args[i].equals("-human")) {
		HUMAN = true;
	    } else {
		System.out
			.println("WARNING: unknown argument " + args[i] + ".");
	    }
	}
	rnd = new Random(System.currentTimeMillis());
	turn = 0;
	players = new Player[PLAYERS_NUM];
	for (int i = 0; i < players.length; i++) {
	    players[i] = new Player(INITIAL_LIFE, ais[i]);
	}
	rebirthPhase();// プレイヤーを初期配置する

	while (!isFinished()) {
	    turn++;
	    System.out.println(turn);
	    for (Player player : players) {
		System.out.println(player.life);
	    }
	    rebirthPhase();

	    ArrayList<String> commands = infromationPhase();

	    // 各AIのコマンドを表示
	    for (String string : commands) {
		System.out.print(string + ", ");
	    }
	    System.out.println();
	    if (DEBUG) {
		commands.remove(3);
		commands.add(scanner.next());
	    }
	    actionPhase(commands);

	    // ボードを表示
	    for (int i = 0; i < MAP_WIDTH; i++) {
		outer: for (int j = 0; j < MAP_WIDTH; j++) {
		    for (int k = 0; k < PLAYERS_NUM; k++) {
			Player player = players[k];
			if (player.isOnBoard() && player.x == i
				&& player.y == j) {
			    char c = (char) (k + 'A');
			    System.out.print(c + "" + c);
			    continue outer;
			}
		    }
		    if (String.valueOf(board[i][j]).length() == 1) {
			System.out.print(" ");
		    }
		    System.out.print(board[i][j]);
		}
		System.out.println();
	    }
	    System.out.println();

	    // いる座標を表示
	    for (Player player : players) {
		if (player.isOnBoard()) {
		    System.out.println(player.x + " " + player.y);
		} else {
		    System.out.println((-1) + " " + (-1));
		}
	    }
	}
    }

    // パネルやプレーヤーを落としたり復活させたりする
    private static void rebirthPhase() {
	// パネルを落としたり復活させたりする
	for (int i = 0; i < MAP_WIDTH; i++) {
	    for (int j = 0; j < MAP_WIDTH; j++) {
		if (board[i][j] < 0) {
		    board[i][j]++;
		} else if (board[i][j] == 1) {
		    board[i][j] = -PANEL_REBIRTH_TURN;
		} else if (board[i][j] > 1) {
		    board[i][j]--;
		}
	    }
	}

	// プレイヤーを落としたり復活させたりする
	for (int i = 0; i < PLAYERS_NUM; i++) {
	    Player p = players[i];
	    // 落とす
	    if (p.isOnBoard() && !p.isMuteki(turn)) {
		if (board[p.x][p.y] < 0) {
		    p.drop(turn);
		}
	    } else if (p.isAlive() && !p.isOnBoard() && p.rebirthTurn == turn) {
		// 復活させる

		// 復活場所を探す
		search: while (true) {
		    int x = nextInt();
		    int y = nextInt();
		    for (int j = 0; j < PLAYERS_NUM; j++) {
			if (i == j) {
			    continue;
			}

			Player other = players[j];
			if (other.isOnBoard()
				&& dist(x, y, other.x, other.y) <= REPULSION) {
			    // 敵に近過ぎたらだめ
			    continue search;
			}
		    }

		    // x,yに復活させる
		    p.reBirthOn(x, y, turn);
		    p.dir = nextDir();
		    break;
		}
	    }
	}
    }

    // AIに情報を渡してコマンドを受け取る
    private static ArrayList<String> infromationPhase() {
	ArrayList<Integer> lifes = new ArrayList<Integer>();
	ArrayList<String> wheres = new ArrayList<String>();

	for (int i = 0; i < PLAYERS_NUM; i++) {
	    lifes.add(players[i].life);
	    if (players[i].isOnBoard()) {
		wheres.add(players[i].x + " " + players[i].y);
	    } else {
		wheres.add((-1) + " " + (-1));
	    }
	}

	// 情報をAIに渡す
	for (int id = 0; id < PLAYERS_NUM; id++) {
	    if (!players[id].isAlive()) {
		continue;
	    }
	    players[id].putInformation(id, turn, board, lifes, wheres);
	}

	// コマンドを受け取る
	ArrayList<String> commandList = new ArrayList<String>();
	for (Player p : players) {
	    if (!p.isAlive()) {
		commandList.add(NONE);
	    }
	    commandList.add(p.getAction());
	}
	return commandList;
    }

    // AIから受け取ったアクションを実行する
    private static void actionPhase(ArrayList<String> commands) {
	// 攻撃を処理
	for (int i = 0; i < PLAYERS_NUM; i++) {
	    Player p = players[i];
	    if (!p.isOnBoard() || p.isPausing(turn)
		    || !commands.get(i).equals(ATTACK)) {
		continue;
	    }
	    // 今いるブロックを出す
	    int xBlock = p.x / BLOCK_WIDTH;
	    int yBlock = p.y / BLOCK_WIDTH;
	    for (int x = 0; x < MAP_WIDTH; x++) {
		for (int y = 0; y < MAP_WIDTH; y++) {
		    int xB = x / BLOCK_WIDTH;
		    int yB = y / BLOCK_WIDTH;
		    if (p.dir == 0) {
			// 上向きの時
			// xが減っていく
			// yは同じ
			if (yB == yBlock && xB < xBlock && board[x][y] == 0) {
			    board[x][y] = dist(xB, yB, xBlock, yBlock);
			}
		    } else if (p.dir == 1) {
			// 右向きの時
			// yは増えていき、xは同じ
			if (xB == xBlock && yB > yBlock && board[x][y] == 0) {
			    board[x][y] = dist(xB, yB, xBlock, yBlock);
			}
		    } else if (p.dir == 2) {
			// 下向きの時
			// xは増え、yは同じ
			if (yB == yBlock && xB > xBlock && board[x][y] == 0) {
			    board[x][y] = dist(xB, yB, xBlock, yBlock);
			}
		    } else if (p.dir == 3) {
			// 左向きの時
			if (xB == xBlock && yB < yBlock && board[x][y] == 0) {
			    board[x][y] = dist(xB, yB, xBlock, yBlock);
			}
		    }
		}
	    }

	    // 攻撃すると硬直する
	    p.attackedPause(turn);
	}
	// 攻撃処理終了

	// 移動処理
	boolean[] moved = new boolean[PLAYERS_NUM];
	int[] toxCache = new int[PLAYERS_NUM];
	int[] toyCache = new int[PLAYERS_NUM];
	for (int i = 0; i < PLAYERS_NUM; i++) {
	    Player p = players[i];
	    moved[i] = true;
	    if (!p.isOnBoard() || p.isPausing(turn)) {
		continue;
	    }

	    String command = commands.get(i);
	    p.direction(command);
	    int tox = -1, toy = -1;
	    if (command.equals(UP)) {
		tox = p.x - 1;
		toy = p.y;
	    } else if (command.equals(RIGHT)) {
		tox = p.x;
		toy = p.y + 1;
	    } else if (command.equals(DOWN)) {
		tox = p.x + 1;
		toy = p.y;
	    } else if (command.equals(LEFT)) {
		tox = p.x;
		toy = p.y - 1;
	    } else {
		continue;
	    }

	    // ボード外への移動を指定している場合はスルー
	    if (!isInside(tox, toy)) {
		continue;
	    }

	    // 移動先でぶつからないかどうかチェック
	    for (int j = 0; j < PLAYERS_NUM; j++) {
		if (!players[j].isOnBoard() || j == i) {
		    continue;
		}

		if (dist(tox, toy, players[j].x, players[j].y) < REPULSION - 1) {
		    // 移動先でぶつかりそうな時
		    moved[i] = false;
		    toxCache[i] = tox;
		    toyCache[i] = toy;
		}
	    }

	    // 移動
	    if (moved[i]) {
		p.moveTo(tox, toy);
	    }
	}

	// ぶつかりそうな人たちを処理する
	move: for (int i = 0; i < PLAYERS_NUM; i++) {
	    if (moved[i]) {
		continue;
	    }
	    for (int j = 0; j < PLAYERS_NUM; j++) {
		if (i == j) {
		    continue;
		}
		if (!moved[j]
			&& dist(toxCache[i], toyCache[i], toxCache[j],
				toyCache[j]) < REPULSION) {
		    // ぶつかる場合は動けない
		    continue move;
		} else if (players[j].isOnBoard()
			&& dist(toxCache[i], toyCache[i], players[j].x,
				players[j].y) < REPULSION) {
		    // ぶつかる場合は動けない
		    continue move;
		}
	    }

	    // なんだかんだぶつからなかった時
	    players[i].moveTo(toxCache[i], toyCache[i]);
	}

    }

    // マンハッタン距離計算
    private static int dist(int x1, int y1, int x2, int y2) {
	return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static int nextInt() {
	// ランダムな座標を返す
	int ret = (int) (rnd.nextDouble() * MAP_WIDTH);
	return ret;
    }

    private static int nextDir() {
	// ランダムな向きを返す
	int rng = rnd.nextInt(4);
	return rng;
    }

    private static boolean isInside(int x, int y) {
	return 0 <= x && x < MAP_WIDTH && 0 <= y && y < MAP_WIDTH;
    }

    private static boolean isFinished() {
	int livingCnt = 0;
	for (int i = 0; i < players.length; i++) {
	    if (players[i].life > 0) {
		livingCnt++;
	    }
	}
	return livingCnt == 0 || turn > FORCED_END_TURN;
    }
}
