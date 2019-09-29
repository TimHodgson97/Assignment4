import java.util.concurrent.atomic.AtomicInteger;

public class Score {
	private final AtomicInteger missedWords; //= new AtomicInteger(0);
	private final AtomicInteger caughtWords; //= new AtomicInteger(0);
	private final AtomicInteger gameScore; //= new AtomicInteger(0);

	Score() {
		missedWords = new AtomicInteger(0);
		caughtWords = new AtomicInteger(0);
		gameScore = new AtomicInteger(0);
	}

	// all getters and setters must be synchronized

	public synchronized int getMissed() {
		return missedWords.get();
	}

	public synchronized int getCaught() {
		return caughtWords.get();
	}

	public synchronized int getTotal() {
		return (missedWords.get() + caughtWords.get());
	}

	public synchronized int getScore() {
		return gameScore.get();
	}

	public synchronized void missedWord() {
		while (true) {
			int existingValue = getMissed();
			int newValue = existingValue + 1;
			if (missedWords.compareAndSet(existingValue, newValue)) {
				return;
			}
			// missedWords++;
		}
	}

	public synchronized void caughtWord(int length) {
		// caughtWords++;
		// gameScore+=length;
		while (true) {
			int existingValueW = getCaught();
			int existingValueG = getScore();
			int newValueW = existingValueW + 1;
			int newValueG = existingValueG + length;
			if (caughtWords.compareAndSet(existingValueW, newValueW)
					&& gameScore.compareAndSet(existingValueG, newValueG)) {
				return;
			}
		}
	}

	public synchronized void resetScore() {
		/*
		 * caughtWords=0; missedWords=0; gameScore=0;
		 */
		while (true) {
			int existingValueW = getCaught();
			int existingValueG = getScore();
			int existingValueM = getMissed();
			int newValueW = 0;
			int newValueG = 0;
			int newValueM = 0;
			if (caughtWords.compareAndSet(existingValueW, newValueW)
					&& gameScore.compareAndSet(existingValueG, newValueG)
					&& missedWords.compareAndSet(existingValueM, newValueM)) {
				return;
			}
		}
	}
}
