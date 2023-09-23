package scrabble;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.GridLayout;
import java.awt.Color;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.EventQueue;

public class Main
{

	private JFrame frame;
	JButton[][] cellButtons; 
	JButton[] rackButtons; 
	
	JPanel boardPanel; 
	JPanel playerPanel; 
	JPanel rackPanel; 
	
	Board board;
	Bag bag;
	Player player;
	Engine engine;
	
	private JPanel logoPanel;
	private JLabel logoLabel;
	private JLabel scoreLabel;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main main = new Main();
					main.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public Main() 
	{
		initialize();
	}

	private void initialize() 
	{
		createWindow();
		createBoardPanel();
		createPlayerPanel();
		createRackPanel();
		
		board = new Board();
		player = new Player();
		engine = new Engine(player,board);
		bag = new Bag();
		
		createCellButtons(board.cellMatrix);
		
		givePlayerStartingTiles();
		
		createButtonsForPlayerRack();
		
		updatePlayerRackGUI();
		
	}
	
	public void newGame()
	{
		board = new Board();
		player = new Player();
		engine = new Engine(player, board);
		bag = new Bag();
	}
	
	
	private void createWindow()
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 866, 802);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
	}
	
	private void createBoardPanel()
	{
		boardPanel = new JPanel();
		boardPanel.setBounds(70, 0, 831, 541);
		frame.getContentPane().add(boardPanel);
		boardPanel.setLayout(new GridLayout(15, 15, 0, 0));
	}
	
	private void createPlayerPanel()
	{
		playerPanel = new JPanel();
		playerPanel.setBounds(10, 515, 831, 143);
		frame.getContentPane().add(playerPanel);
		playerPanel.setLayout(null);
		{
			logoPanel = new JPanel();
			logoPanel.setBounds(10, 11, 831, 47);
			frame.getContentPane().add(logoPanel);
			logoPanel.setLayout(new BorderLayout(0, 0));
			{
				logoLabel = new JLabel("S  C  R  A  B  B  L  E");
				logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
				logoLabel.setFont(new Font("Calibri Light", Font.BOLD, 22));
				logoPanel.add(logoLabel);
			}
		}
	}
	
	private void createRackPanel()
	{
		rackPanel = new JPanel();
		rackPanel.setBounds(165, 60, 500, 61);
		playerPanel.add(rackPanel);
		rackPanel.setLayout(new GridLayout(0, 7, 0, 0));
		
		JPanel actionPanel = new JPanel();
		actionPanel.setBounds(165, 12, 500, 37);
		playerPanel.add(actionPanel);
		actionPanel.setLayout(new GridLayout(1, 4, 0, 0));
		
		JButton shuffleButton = new JButton("Shuffle");
		actionPanel.add(shuffleButton);
		shuffleButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				player.shuffleRack();
				player.organizeRack();
				updatePlayerRackGUI();
			}
		});
		
		JButton undoButton = new JButton("Undo");
		actionPanel.add(undoButton);
		undoButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.recentlyPlayedTileStack.size() > 0  && engine.recentPlayStack.size() > 0 && engine.recentButtonStack.size() > 0 && engine.rackTileSelected == null)
				{
					Tile recentTilePlayed = engine.recentlyPlayedTileStack.pop();
					
					player.addTileToRack(recentTilePlayed);
					
					Cell recentCell = engine.recentPlayStack.pop();
					recentCell.setTile(null);
					
					JButton recentCellButton = engine.recentButtonStack.pop();
					recentCellButton.setText(recentCell.getBonus());
					recentCellButton.setBackground(Color.LIGHT_GRAY);
					
					player.organizeRack();
					updatePlayerRackGUI();
				}
			}
		});
		
		
		JButton submitButton = new JButton("Submit");
		actionPanel.add(submitButton);
		submitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.checkBoard() == true)
				{
					System.out.println("Move passed");
					scoreLabel.setText(player.getScore()); 
					
					while(true)
					{
						if(givePlayerANewTile() == false)
						{
							break;
						}
					}
					
					updatePlayerRackGUI();
				}
				else
				{
					System.out.println("Move denied");
				}
			}
		});
		
		scoreLabel = new JLabel("0");
		scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		scoreLabel.setFont(new Font("Tahoma", Font.PLAIN, 25));
		scoreLabel.setBounds(10, 39, 65, 53);
		playerPanel.add(scoreLabel);
		
		JLabel lblNewLabel = new JLabel("pts.");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel.setBounds(85, 48, 43, 42);
		playerPanel.add(lblNewLabel);
		
	}
	
	private void createCellButtons(Cell[][] cellMatrix)
	{
		cellButtons = new JButton[15][15];
		
		for(int i = 0; i < 15; i++)
		{
			for(int j = 0; j < 15; j++)
			{
				JButton cellButton = new JButton();
				cellButton.setBackground(Color.LIGHT_GRAY);
				Cell currentCell = cellMatrix[i][j];
				
				cellButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						if(engine.rackTileSelected != null && currentCell.getTile() == null)
						{
							if(engine.rackTileSelected.getLetter() == "-")
							{
								chooseTileDialog ct = new chooseTileDialog();
								ct.setModal(true);
								ct.setVisible(true);
								currentCell.setTile(Bag.swappedBlankTile); 
								cellButton.setText(Bag.swappedBlankTile.getLetter()); 

							}
							else
							{
								currentCell.setTile(engine.rackTileSelected); 
								cellButton.setText(engine.rackTileSelected.getLetter()); 
							}
							
							cellButton.setBackground(Color.ORANGE); 
							engine.rackTileSelected = null;
							engine.recentPlayStack.push(currentCell); 
							engine.recentButtonStack.push(cellButton);
						}
					}
				});
				
				cellButtons[i][j] = cellButton;
				boardPanel.add(cellButton);
					
				//labeling cell buttons with bonus label (Triple/double word/letter on specific cell buttons)
				if(cellMatrix[i][j].getBonus() != null)
				{
					cellButtons[i][j].setText(cellMatrix[i][j].getBonus());
				}
			}
		}
		
	}
	
	//create button for each of 7 tiles for player's rack
	private void createButtonsForPlayerRack()
	{
		rackButtons = new JButton[7];
			
		//rack button 1
		JButton rackButton1 = new JButton();
		rackButton1.setBackground(Color.ORANGE);
		rackButton1.setText(player.getRack()[0].getLetter()); //set tile's letter and points to show on button
		
		rackButton1.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				//check if player has no tiles from rack selected before selecting another tile from rack. Also update rack tile button GUI
				if(engine.rackTileSelected == null && rackButton1.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(0); //to transfer tile from player's rack onto board
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected); //for undo purposes

					rackButton1.setBackground(Color.LIGHT_GRAY); //update rack tile button GUI
					rackButton1.setText(""); //update rack tile button GUI
				
				}
				
			}
		});
		
		rackButtons[0] = rackButton1;
		rackPanel.add(rackButton1);
		
		//rack button 2
		JButton rackButton2 = new JButton();
		rackButton2.setBackground(Color.ORANGE);
		rackButton2.setText(player.getRack()[1].getLetter());
		
		rackButton2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.rackTileSelected == null && rackButton2.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(1);
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected);
				
					rackButton2.setBackground(Color.LIGHT_GRAY);
					rackButton2.setText("");
				
				}
				
			}
		});
		
		rackButtons[1] = rackButton2;
		rackPanel.add(rackButton2);
		
		
		//rack button 3
		JButton rackButton3 = new JButton();
		rackButton3.setBackground(Color.ORANGE);
		rackButton3.setText(player.getRack()[2].getLetter());
		
		rackButton3.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.rackTileSelected == null && rackButton3.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(2);
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected);
		
					rackButton3.setBackground(Color.LIGHT_GRAY);
					rackButton3.setText("");
					
				}
				
			}
		});
		
		rackButtons[2] = rackButton3;
		rackPanel.add(rackButton3);
		
		//rack button 4
		JButton rackButton4 = new JButton();
		rackButton4.setBackground(Color.ORANGE);
		rackButton4.setText(player.getRack()[3].getLetter());
		
		rackButton4.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.rackTileSelected == null && rackButton4.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(3);
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected);
				
					rackButton4.setBackground(Color.LIGHT_GRAY);
					rackButton4.setText("");
				
				}
				
			}
		});
		
		rackButtons[3] = rackButton4;
		rackPanel.add(rackButton4);
		
		//rack button 5
		JButton rackButton5 = new JButton();
		rackButton5.setBackground(Color.ORANGE);
		rackButton5.setText(player.getRack()[4].getLetter());
		
		rackButton5.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.rackTileSelected == null && rackButton5.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(4);
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected);
				
					rackButton5.setBackground(Color.LIGHT_GRAY);
					rackButton5.setText("");
					
				}
				
			}
		});
		
		rackButtons[4] = rackButton5;
		rackPanel.add(rackButton5);
		
		//rack button 6
		JButton rackButton6 = new JButton();
		rackButton6.setBackground(Color.ORANGE);
		rackButton6.setText(player.getRack()[5].getLetter());
		
		rackButton6.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.rackTileSelected == null && rackButton6.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(5);
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected);
			
					rackButton6.setBackground(Color.LIGHT_GRAY);
					rackButton6.setText("");
					
				}
				
			}
		});
		
		rackButtons[5] = rackButton6;
		rackPanel.add(rackButton6);
		
		//rack button 7
		JButton rackButton7 = new JButton();
		rackButton7.setBackground(Color.ORANGE);
		rackButton7.setText(player.getRack()[6].getLetter());
		
		rackButton7.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(engine.rackTileSelected == null && rackButton7.getText() != "")
				{
					engine.rackTileSelected = player.getAndRemoveFromRackAt(6);
					engine.recentlyPlayedTileStack.push(engine.rackTileSelected);
				
					rackButton7.setBackground(Color.LIGHT_GRAY);
					rackButton7.setText("");
				
				}
				
			}
		});
		
		rackButtons[6] = rackButton7;
		rackPanel.add(rackButton7);
	}
	
	
	//CREATING BUTTONS - END
	
	//UPDATING BUTTONS/DATA STRUCTURES - START
	
	//Add a tile from bag to player's rack if possible
	private boolean givePlayerANewTile()
	{
		//cannot add tiles to player's rack if bag is empty or rack is full (rack has more than 7 elements)
		if(bag.bagIsEmpty() || (player.getRackSize() == 7))
		{
			return false;
		}
		
		Tile newTile = bag.getNextTile();
		player.addTileToRack(newTile);
		return true;
	}
	
	//Add 7 tiles from bag to player's rack at start of game
	private void givePlayerStartingTiles()
	{
		for(int i = 0; i < 7; i++)
		{
			givePlayerANewTile();
		}
	}
	
	private void updatePlayerRackGUI() //places tiles pieces found directly in player's rack onto rack button GUI
	{
		for(int i = 0; i < 7; i++)
		{
			if(player.getRack()[i] != null)
			{
				rackButtons[i].setText(player.getRack()[i].getLetter());
				rackButtons[i].setBackground(Color.ORANGE);
			}
			else if(player.getRack()[i] == null)
			{
				rackButtons[i].setText("");
				rackButtons[i].setBackground(Color.LIGHT_GRAY);
			}
			
		}
	}
}
