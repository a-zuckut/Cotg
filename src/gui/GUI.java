package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

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

import cotg.data.Constants;
import cotg.wrappers.Alliance;
import gui.helper.GhostText;
import gui.helper.MyCloseKeyListener;
import gui.helper.MyListKeyListener;

public class GUI extends JPanel {
	
	public static final String ghostText = "Search...";

	protected static final long serialVersionUID = 1L;
	public static final int pref_width = 600, pref_height = 400;

	protected JTextField search;
	protected JScrollPane scroll;
	protected JList<Alliance> list;
	protected JButton open;

	public static JFrame childFrame = null;
	public static JFrame frame = GuiRunner.main;
	
	public GUI() {
		this.setPreferredSize(new Dimension(pref_width, pref_height));
		this.setLayout(new BorderLayout());

		initComponents();

		this.add(search, BorderLayout.NORTH);
		this.add(scroll, BorderLayout.CENTER);
		this.add(open, BorderLayout.SOUTH);
	}

	public void filterModel(DefaultListModel<Alliance> model, String filter) {
		for (Alliance s : Constants.curr_alliances) {
			if (!s.name.toLowerCase().trim().startsWith(filter.toLowerCase().trim()) && !filter.equals(ghostText)) {
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

	protected JList<Alliance> createJList() {
		JList<Alliance> list = new JList<>(createDefaultListModel());
		list.setVisibleRowCount(6);
		return list;
	}

	protected ListModel<Alliance> createDefaultListModel() {
		DefaultListModel<Alliance> model = new DefaultListModel<>();
		for (Alliance s : Constants.curr_alliances) {
			model.addElement(s);
		}
		return model;
	}

	protected void initComponents() {
		search = new JTextField();
		new GhostText(search, ghostText);

		list = createJList();
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
				filterModel((DefaultListModel<Alliance>) list.getModel(), filter);
			}
		});

		scroll = new JScrollPane(list);

		open = new JButton("Submit");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				childFrame = new AllianceFrame(list.getSelectedValue());
			}
		});
		
		list.addKeyListener(new MyListKeyListener(open));
		addKeyListeners();
	}
	
	public void addKeyListeners() {
		KeyListener keyListener = new MyCloseKeyListener(frame, search, list);
		list.addKeyListener(keyListener);
		search.addKeyListener(keyListener);
		open.addKeyListener(keyListener);
		scroll.addKeyListener(keyListener);
		this.addKeyListener(keyListener);
	}

}
