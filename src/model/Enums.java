package model;
public class Enums {
	public enum Difficulty {
		normal,hard,expert
	}
	public enum GameMode {
		multiplayer,solo
	}
	public enum GameStatus {
		setup,inProgress,waitingForFinalGuess,soloContinue,finished
	}
	public enum LetterFeedback {
		correctPosition,wrongPosition,notInWord,usedPresent,usedNotPresent,unused
	}
	public enum TimerDuration {
			none(0),oneMinute(60),threeMinutes(180),fourMinutes(240),fiveMinutes(300);
			TimerDuration(int seconds) {
				this.seconds=seconds;
			}
			public int seconds() {
				return seconds;
			}
			public boolean isTimed() {
				return seconds>0;
			}
			final int seconds;
		}
		public enum WordLength {
			three(3),four(4),five(5),six(6);
			WordLength(int length) {
				this.length=length;
			}
			public int length() {
				return length;
			}
			final int length;
		}
	public enum WordSource {
		manual,rollTheDice
	}
}
