/*
 * 適当なターン数が経過すると3秒停止するようになるAI
 */
#include <iostream>
#include <random>
#include "unistd.h"

using namespace std;

const string commands = "URDLA";
int board[40][40];
int life[4];
int turn;
int x[4];
int y[4];
string dir[4];

void output() {
}

int main() {
	cout << "Ready" << endl;
	random_device rnd;
	while (true) {
		int my_num;
		cin >> my_num;

		cin >> turn;

		for (int i = 0; i < 4; ++i) {
			cin >> life[i];
		}
		for (int i = 0; i < 40; ++i)
			for (int j = 0; j < 40; ++j)
				cin >> board[i][j];

		for (int i = 0; i < 4; ++i)
			cin >> y[i] >> x[i];

		string eod;
		cin >> eod;

		if (turn > my_num * 100) {
			usleep(3000 * 1000);
		}

		int c = rnd() % commands.size();
		cout << commands[c] << endl;
	}

	return 0;
}
