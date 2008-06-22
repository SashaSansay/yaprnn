package yaprnn.gui;

import yaprnn.gui.view.MainView;

/**
 * GUI-Controller von yaprnn.
 */
public class GUI {

	private MainView mainView = null;

	/**
	 * Main-Method zum testen.
	 */
	public static void main(String[] args) {
		new GUI(new MainView());
	}

	/**
	 * Konstruktor
	 * 
	 * @param mv
	 *            Die zu verwaltende MainView.
	 */
	public GUI(MainView mv) {
		this.setView(mv);
		mv.setVisible(true);
	}

	/**
	 * Setzt die zu verwaltene MainView.
	 * 
	 * @param mainView
	 *            Die zu verwaltende MainView.
	 */
	public void setView(MainView mainView) {
		if (this.mainView != null)
			disconnectMainView();
		this.mainView = mainView;
		connectMainView();
	}

	/**
	 * Liefert die aktuell verwaltete MainView.
	 */
	public MainView getView() {
		return this.mainView;
	}

	/**
	 * H�ngt die Eventhandler an eine MainView.
	 */
	private void connectMainView() {
		// TODO
	}

	/**
	 * Entfernt die Eventhandler von einer MainView.
	 */
	private void disconnectMainView() {
		// TODO
	}

}
