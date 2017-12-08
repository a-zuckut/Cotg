package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cotg.wrappers.Player;
import gui.helper.GhostText;
import gui.helper.MyListKeyListener;
import gui.helper.MyListSelectionListener;

public class PlayersPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public static final String ghostText = "Search...";
	
	protected static MyListSelectionListener<Player> listListener = new MyListSelectionListener<>();
	
	private Set<Player> players;
	
	private JTextField search;
	private JScrollPane scrollPane;
	private JList<Player> list;
	private JButton submit;
	
	public static JFrame childFrame = null;

	public PlayersPanel(Set<Player> players, int x, int y) {
		this.setPreferredSize(new Dimension(x, y));
		this.setLayout(new BorderLayout());
		
		this.players = players;
		initComponents();
		
		this.add(search, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(submit, BorderLayout.SOUTH);
	}

	public void filterModel(DefaultListModel<Player> model, String filter) {
		for (Player s : players) {
			if (!s.name.startsWith(filter) && !filter.equals(ghostText)) {
				if (model.contains(s)) {
					model.removeElement(s);
				}
			} else {
				if (!model.contains(s)) {
					model.addElement(s);
				}
			}
		}
	}

	protected JList<Player> createJList() {
		JList<Player> list = new JList<>(createDefaultListModel());
		list.setVisibleRowCount(6);
		return list;
	}

	protected ListModel<Player> createDefaultListModel() {
		DefaultListModel<Player> model = new DefaultListModel<>();
		for (Player s : players) {
			model.addElement(s);
		}
		return model;
	}
	
	private void initComponents() {
		search = new JTextField();
		new GhostText(search, ghostText);

		list = createJList();
		list.addListSelectionListener(listListener);
		list.setVisibleRowCount(6);

		search.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				filter();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				filter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filter();
			}

			private void filter() {
				String filter = search.getText();
				filterModel((DefaultListModel<Player>) list.getModel(), filter);
			}
		});

		scrollPane = new JScrollPane(list);

		submit = new JButton("Submit");
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				childFrame = new PlayerFrame(listListener.selected);
			}
		});
		
		list.addKeyListener(new MyListKeyListener(submit));
	}

	public void addKeyListeners(KeyListener keyListener) {
		search.addKeyListener(keyListener);
		list.addKeyListener(keyListener);
		scrollPane.addKeyListener(keyListener);
		submit.addKeyListener(keyListener);
		this.addKeyListener(keyListener);
	}

}
