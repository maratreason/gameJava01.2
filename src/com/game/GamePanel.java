package com.game;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {

	// FIELDS
	public static int WIDTH = 500;
	public static int HEIGHT = 500;

	private Thread thread;
	private boolean running;

	private BufferedImage image;
	private Graphics2D g;

	private int FPS = 30;
	private double averageFPS;
	
	public static Player player;
	//public static Enemy enemy;
	public static ArrayList<Bullet> bullets;
	public static ArrayList<Enemy> enemies;
	public static ArrayList<PowerUp> powerups;
	public static ArrayList<Explosion> explosions;
	public static ArrayList<Text> texts;
	
	private long waveStartTimer;
	private long waveStartTimerDiff;
	private int waveNumber;
	private boolean waveStart;
	private int waveDelay = 2000;
	
	// замедление
	private long slowDownTimer;
	private long slowDownTimerDiff;
	private int slowDownLength = 6000;
	

	// Constructor
	public GamePanel() {
		super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
	}

	// FUNCTIONS
	public void addNotify() {
		super.addNotify();
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		addKeyListener(this);
	}

	public void run() {

		running = true;

		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		
		// сглаживание контуров рисованных объектов
		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
	// new lists in thread
		player = new Player();
		bullets = new ArrayList<>();
		enemies = new ArrayList<>();
		powerups = new ArrayList<>();
		explosions = new ArrayList<>();
		texts = new ArrayList<>();
		
		// running wave
		waveStartTimer = 0;
		waveStartTimerDiff = 0;
		waveStart = true;
		waveNumber = 0;

		long startTime;
		long URDTimeMillis;
		long waitTime;
		long totalTime = 0;

		int frameCount = 0;
		int maxFrameCount = 30;

		long targetTime = 1000 / FPS;

		// GAME LOOP
		while(running)  {

			startTime = System.nanoTime();

			gameUpdate();
			gameRender();
			gameDraw();

			URDTimeMillis = (System.nanoTime() - startTime) / 1000000;

			waitTime = targetTime - URDTimeMillis;

			try {
				Thread.sleep(waitTime);
			}
			catch(Exception e) {

			}

			totalTime += System.nanoTime() - startTime;
			frameCount++;
			if(frameCount == maxFrameCount) {
				averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
				frameCount = 0;
				totalTime = 0;
			}

		}
		
		g.setColor(new Color(0, 100, 252));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Century Gothic", Font.PLAIN, 16));
		String s = "G A M E   O V E R";
		int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
		s = "Final Score: " + player.getScore();
		length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 30);
		gameDraw();

	}

	private void gameUpdate() {

		// new wave
		if(waveStartTimer == 0 && enemies.size() == 0) {
			waveNumber++;
			waveStart = false;
			waveStartTimer = System.nanoTime();
		}
		else {
			waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
			if(waveStartTimerDiff > waveDelay) {
				waveStart = true;
				waveStartTimer = 0;
				waveStartTimerDiff = 0;
			}
		}
		
		// create enemies
		if(waveStart && enemies.size() == 0) {
			createNewEnemies();
		}
	
		// player update
		player.update();
		
		// bullet update
		for(int i = 0; i < bullets.size(); i++){
			boolean remove = bullets.get(i).update();
			if(remove){
				bullets.remove(i);
				i--;
			}
		}
		
		// enemy update
		for(int i = 0; i < enemies.size(); i++) {
			enemies.get(i).update();
		}
		
		// powerup update
		for(int i = 0; i < powerups.size(); i++) {
			boolean remove = powerups.get(i).update();
			if(remove) {
				powerups.remove(i);
				i--;
			}
		}
		
		// explosion update
		for(int i = 0; i < explosions.size(); i++) {
			boolean remove = explosions.get(i).update();
			if(remove) {
				explosions.remove(i);
				i--;
			}
		}
		
		// text update - появление и исчезновение текста
		for(int i = 0; i < texts.size(); i++) {
			boolean remove = texts.get(i).update();
			if(remove) {
				texts.remove(i);
				i--;
			} // далее прописать в gameRender
		}
		
		// bullet-enemy collision
		for(int i = 0; i < bullets.size(); i++) {
			
			Bullet b = bullets.get(i);
			double bx = b.getx();
			double by = b.gety();
			double br = b.getr();
			
			for(int j = 0; j < enemies.size(); j++) {
				
				Enemy e = enemies.get(j);
				double ex = e.getx();
				double ey = e.gety();
				double er = e.getr();
				
				double dx = bx - ex;
				double dy = by - ey;
				double dist = Math.sqrt(dx * dx + dy * dy);
				
				// при попадании пуля исчезает
				if(dist < br + er) {
					e.hit();
					bullets.remove(i);
					i--;
					break;
				}
				
			}
		}
		
		// check dead enemies
		for(int i = 0; i < enemies.size(); i++) {
			if(enemies.get(i).isDead()) {
				// если враг уничтожен
				Enemy e = enemies.get(i);
				
				// chance for powerup
				// коробочки жизней и апгрейдов оружия
				double rand = Math.random();
				if(rand < 0.040) powerups.add(new PowerUp(1, e.getx(), e.gety()));
				else if(rand < 0.300) powerups.add(new PowerUp(2, e.getx(), e.gety()));
				else if(rand < 0.320) powerups.add(new PowerUp(3, e.getx(), e.gety()));
				else if(rand < 0.345) powerups.add(new PowerUp(4, e.getx(), e.gety()));
				
				// раскомментировать, если для тестов.
				//else powerups.add(new PowerUp(2, e.getx(), e.gety()));
				
				// то добавить очки врагов
				// getType прописан в Enemy.java
				// попробовать поставить if в addScore( if(){...} ) 
				// чтобы очков давали больше за сложных врагов
				player.addScore(e.getType() + e.getRank() + 1); 
				enemies.remove(i);
				i--;
				
				// после удаления объектов врага ...e.getr() + 20 - чем больше тем медленне 
				// анимация взрыва
				e.explode(); 
				explosions.add(new Explosion(e.getx(), e.gety(), e.getr(), e.getr() + 30));
				
			}
		}
		
		// check dead player
		if(player.isDead()) {
			running = false;
		}
			
		// player-enemy collision
		if(!player.isRecovering()) {
			int px = player.getx();
			int py = player.gety();
			int pr = player.getr();
			for(int i = 0; i < enemies.size(); i++) {
				
				Enemy e = enemies.get(i);
				double ex = e.getx();
				double ey = e.gety();
				double er = e.getr();
				
				double dx = px - ex;
				double dy = py - ey;
				double dist = Math.sqrt(dx * dx + dy * dy);
				
				if(dist < pr + er) {
					player.loseLife();
				}
				
			}
		}
		
		// player-powerup collision
		int px = player.getx();
		int py = player.gety();
		int pr = player.getr();
		for(int i = 0; i < powerups.size(); i++) {
			PowerUp p = powerups.get(i);
			double x = p.getx();
			double y = p.gety();
			double r = p.getr();
			double dx = px - x;
			double dy = py - y;
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			// collected powerup
			// gainLife() body in Player.java 
			if(dist < pr + r) {
				
				int type = p.getType();
				
				if(type == 1) {
					player.gainLife(); // прибавка жизней
					texts.add(new Text(player.getx(), player.gety(), 1000, "Extra Life"));
/*Это уже я добавил*/ player.addScore(3);
					
				}
				if(type == 2) {
					player.increasePower(1);
					texts.add(new Text(player.getx(), player.gety(), 1000, "Power"));
					player.addScore(3);
				}
				if(type == 3) {
					player.increasePower(3);
					texts.add(new Text(player.getx(), player.gety(), 1000, "Double Power"));
					player.addScore(5);
				}
				if(type == 4) {
					slowDownTimer = System.nanoTime(); // j потому что уже есть i в
					for(int j = 0; j < enemies.size(); j++) { // цикле выше. и скопировать
						enemies.get(j).setSlow(true); // этот цикл в slowdown update
					}                          // только прописать false
					// texts заполнять как в конструкторе
					texts.add(new Text(player.getx(), player.gety(), 1000, "Slow Down"));
					player.addScore(3);
					// и прописать в text update цикл
					}
			
				
				powerups.remove(i);
				i--;
			}
	
		}
		
		// slowdown update
		if(slowDownTimer != 0) {
			slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
			if(slowDownTimerDiff > slowDownLength) {
				slowDownTimer = 0;
				for(int j = 0; j < enemies.size(); j++) { // цикле выше
						enemies.get(j).setSlow(false);
					}
			}
		
		}
		
	}

	private void gameRender() {
		
		// draw background
		g.setColor(new Color(0, 150, 200));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		/*g.setColor(Color.BLACK);
		g.drawString("FPS: " + averageFPS, 10, 10);
		g.drawString("num bullets: " + bullets.size(), 10, 20);*/
		
	// draw slowdown screen - изменение цвета экрана при замедлении
		if(slowDownTimer != 0) {
			g.setColor(new Color(255, 200, 200, 64));
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}
		
	// draw player
		player.draw(g);
		
	// draw bullet
		for(int i = 0; i < bullets.size(); i++) { bullets.get(i).draw(g); }
		
	// draw enemy
		for(int i = 0; i < enemies.size(); i++) { enemies.get(i).draw(g); }
		
	// draw powerups
		for(int i = 0; i < powerups.size(); i++) { powerups.get(i).draw(g); }
		
	// draw explosions
		for(int i = 0; i < explosions.size(); i++) { explosions.get(i).draw(g); }
		
	// draw text
		for(int i = 0; i < texts.size(); i++) { texts.get(i).draw(g); }
		
	// draw wave number
		if(waveStartTimer != 0) {
			g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
			String s = " - W A V E    " + waveNumber + "   -";
			int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
			int alpha = (int)(255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
			if(alpha > 255) alpha = 255;
			g.setColor(new Color(255, 255, 255, alpha));
			g.drawString(s, WIDTH / 2 - length / 2, HEIGHT / 2);
		}
		
	// draw player lives
		for(int i = 0; i < player.getLives(); i++) {
			g.setColor(Color.WHITE);
			g.fillOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
			g.setStroke(new BasicStroke(1));
			g.setColor(Color.WHITE.darker());
			g.drawOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
			g.setStroke(new BasicStroke(1));
		}
		
	// draw player power
		g.setColor(Color.WHITE);
		g.fillRect(20, 40, player.getPower() * 8, 8);
		g.setColor(Color.WHITE.darker());
		g.setStroke(new BasicStroke(1));
		for(int i = 0; i < player.getRequiredPower(); i++) {
			g.drawRect(20 + 8 * i, 40, 8, 8);
		}
		g.setStroke(new BasicStroke(1));
		
	// draw player score
		g.setColor(Color.WHITE);
		g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
		// высота и ширина вывода надписи Score. 30 - высота
		g.drawString("Score: " + player.getScore(), WIDTH - 100, 30);
		
	// draw slowdown meter
		if(slowDownTimer != 0) {
			g.setColor(Color.WHITE);
			g.drawRect(20, 60, 100, 8);
			g.fillRect(20, 60, 
				(int) (100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
		}
		
	}

	private void gameDraw() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}
	
	private void createNewEnemies() {
		
		enemies.clear();
		Enemy e;
		
		if(waveNumber == 1) {
			for(int i = 0; i < 5; i++) {
				enemies.add(new Enemy(1, 1));
			}
		}
		if(waveNumber == 2) {
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(1, 1));
			}
			enemies.add(new Enemy(1, 2));
			enemies.add(new Enemy(1, 2));
		}
		if(waveNumber == 3) {
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(1, 2));
				enemies.add(new Enemy(1, 3));
				enemies.add(new Enemy(1, 3));
			}
		}
		if(waveNumber == 4) {
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(1, 2));
				enemies.add(new Enemy(1, 3));
				enemies.add(new Enemy(1, 4));
			}
		}
		if(waveNumber == 5) {
			for(int i = 0; i < 6; i++) {
				enemies.add(new Enemy(1, 3));
				enemies.add(new Enemy(2, 2));
				enemies.add(new Enemy(2, 1));
				enemies.add(new Enemy(1, 4));
			}
		}
		if(waveNumber == 6) {
			for(int i = 0; i < 6; i++) {
				enemies.add(new Enemy(1, 3));
				enemies.add(new Enemy(2, 2));
				enemies.add(new Enemy(2, 1));
				enemies.add(new Enemy(1, 4));
			}
		}
		if(waveNumber == 7) {
			for(int i = 0; i < 6; i++) {
				enemies.add(new Enemy(1, 3));
				enemies.add(new Enemy(3, 1));
				enemies.add(new Enemy(2, 1));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(3, 3));
			}
		}
		if(waveNumber == 8) {
			for(int i = 0; i < 6; i++) {
				enemies.add(new Enemy(2, 3));
				enemies.add(new Enemy(3, 2));
				enemies.add(new Enemy(2, 1));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(3, 3));
			}
		}
		if(waveNumber == 9) {
			for(int i = 0; i < 6; i++) {
				enemies.add(new Enemy(1, 3));
				enemies.add(new Enemy(3, 2));
				enemies.add(new Enemy(3, 1));
				enemies.add(new Enemy(3, 4));
				enemies.add(new Enemy(3, 3));
			}
		}
		if(waveNumber == 10) {
			for(int i = 0; i < 7; i++) {
				enemies.add(new Enemy(3, 3));
				enemies.add(new Enemy(3, 2));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(3, 1));
				enemies.add(new Enemy(2, 4));
			}
		}
		if(waveNumber == 11) {
			for(int i = 0; i < 7; i++) {
				enemies.add(new Enemy(3, 3));
				enemies.add(new Enemy(3, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
			}
		}
		if(waveNumber == 12) {
			for(int i = 0; i < 8; i++) {
				enemies.add(new Enemy(1, 4));
				enemies.add(new Enemy(2, 3));
				enemies.add(new Enemy(3, 3));
				enemies.add(new Enemy(3, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
			}
		}
		if(waveNumber == 13) {
			for(int i = 0; i < 8; i++) {
				enemies.add(new Enemy(1, 4));
				enemies.add(new Enemy(2, 3));
				enemies.add(new Enemy(3, 3));
				enemies.add(new Enemy(3, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
			}
		}
		if(waveNumber == 14) {
			for(int i = 0; i < 9; i++) {
				enemies.add(new Enemy(1, 4));
				enemies.add(new Enemy(2, 3));
				enemies.add(new Enemy(3, 3));
				enemies.add(new Enemy(3, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(3, 4));
				enemies.add(new Enemy(2, 4));
				enemies.add(new Enemy(2, 4));
			}
		}
		
		if(waveNumber == 15) {
			running = false;
		}
		
	}
	
	public void keyTyped(KeyEvent key) {}
	
	public void keyPressed(KeyEvent key) {
		int keyCode = key.getKeyCode();
		if(keyCode == KeyEvent.VK_LEFT) {
			player.setLeft(true);
		}
		if(keyCode == KeyEvent.VK_RIGHT) {
			player.setRight(true);
		}
		if(keyCode == KeyEvent.VK_UP) {
			player.setUp(true);
		}
		if(keyCode == KeyEvent.VK_DOWN) {
			player.setDown(true);
		}
		if(keyCode == KeyEvent.VK_Z) {
			player.setFiring(true);
		}
	}
	public void keyReleased(KeyEvent key) {
		int keyCode = key.getKeyCode();
		if(keyCode == KeyEvent.VK_LEFT) {
			player.setLeft(false);
		}
		if(keyCode == KeyEvent.VK_RIGHT) {
			player.setRight(false);
		}
		if(keyCode == KeyEvent.VK_UP) {
			player.setUp(false);
		}
		if(keyCode == KeyEvent.VK_DOWN) {
			player.setDown(false);
		}
		if(keyCode == KeyEvent.VK_Z) {
			player.setFiring(false);
		}
	}

}




