package com.game;

import java.awt.*;

public class Player {
	
	//Fields
	private int x;
	private int y;
	private int r;
	
	private int dx;
	private int dy;
	private int speed;
	
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;
	
	private boolean firing;
	private long firingTimer;
	private long firingDelay;
	
	// попадание врага по игроку
	private boolean recovering;
	private long recoveryTimer;
	
	private int lives;
	private Color color1;
	private Color color2;
	
	private int score;
	
	private int powerLevel;
	private int power;
	private int[] requiredPower = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	
	// CONSTRUCTOR
	public Player() {
		
		x = GamePanel.WIDTH / 2;
		y = GamePanel.HEIGHT / 2;
		r = 5;
		
		dx = 0;
		dy = 0;
		speed = 5;
		
		lives = 3;
		color1 = Color.WHITE;
		color2 = Color.RED;
		
		firing = false;
		firingTimer = System.nanoTime();
		firingDelay = 100;
		
		recovering = false;
		recoveryTimer = 0;
		
		score = 0;
		
	}
	
	// FUNCTIONS
	
	public int getx() { return x; }
	public int gety() { return y; }
	public int getr() { return r; }
	
	public int getScore() { return score; }
	
	public int getLives() { return lives; }
	
	public boolean isDead() { return lives <= 0; }
	public boolean isRecovering() { return recovering; }
	
	public void setLeft(boolean b) { left = b; }
	public void setRight(boolean b) { right = b; }
	public void setUp(boolean b) { up = b; }
	public void setDown(boolean b) { down = b; }
	
	public void setFiring(boolean b) { firing = b; }
	
	public void addScore(int i) { 
		score += i; 
		
		if(score >= 500 && score < 504) { lives++; }
		else if(score >= 1000 && score < 1004) { lives++; }
		else if(score >= 1500 && score < 1504) { lives++; }
		else if(score >= 2000 && score < 2004) { lives++; }
		else if(score >= 2500 && score < 2504) { lives++; }
		else if(score >= 3000 && score < 3004) { lives++; }
		else if(score >= 3500 && score < 3504) { lives++; }
		else if(score >= 4000 && score < 4004) { lives++; }
		else if(score >= 4500 && score < 4504) { lives++; }
		else if(score >= 5000 && score < 5004) { lives++; }
		else if(score >= 5500 && score < 5504) { lives++; }
		else if(score >= 6000 && score < 6004) { lives++; }
		else if(score >= 6500 && score < 6504) { lives++; }
		else if(score >= 7000 && score < 7004) { lives++; }
		else if(score >= 7500 && score < 7504) { lives++; }
		else if(score >= 8000 && score < 8004) { lives++; }
		else if(score >= 8500 && score < 8504) { lives++; }
		else if(score >= 9000 && score < 9004) { lives++; }
		else if(score >= 9500 && score < 9504) { lives++; }
		else if(score >= 10000 && score < 10004) { lives++; }
		
		}
	
	public void gainLife() { lives++; }
	
	// попадание врага по игроку
	public void loseLife() {
		lives--;
		recovering = true;
		recoveryTimer = System.nanoTime(); // время восстановления. Увеличить.
		if(powerLevel > 4){ powerLevel = 4; }
	}
	
	public void increasePower(int i) {
		power += i;
		if(powerLevel == 9) { // чтобы не вылетал Exception на переполнение массива
			if(power > requiredPower[powerLevel]){
				power = requiredPower[powerLevel];
			}
			return;
		}
			if(power >= requiredPower[powerLevel]) {
				power -= requiredPower[powerLevel];
				powerLevel++;
			}
		
	}
	
	public int getPowerLevel() { return powerLevel; }
	public int getPower() { return power; }
	public int getRequiredPower() { return requiredPower[powerLevel]; }
	
	public void update() {
		
		if(left) { dx = -speed; }
		if(right) { dx = speed; }
		if(up) { dy = -speed; }
		if(down) { dy = speed; }
		
		x += dx;
		y += dy;
		
		if(x < r) x = r;
		if(y < r) y = r;
		if(x > GamePanel.WIDTH - r) x = GamePanel.WIDTH - r;
		if(y > GamePanel.HEIGHT - r) y = GamePanel.HEIGHT - r;
		
		dx = 0;
		dy = 0;
		
		if(firing) {
			long elapsed = (System.nanoTime() - firingTimer) / 1000000;
			if(elapsed > firingDelay) {
				firingTimer = System.nanoTime();
				// улучшение вооружения
				if(powerLevel < 2) {
					GamePanel.bullets.add(new Bullet(270, x, y));
				}
				else if(powerLevel < 3) {
					GamePanel.bullets.add(new Bullet(270, x + 5, y));
					GamePanel.bullets.add(new Bullet(270, x - 5, y));
				}
				else if(powerLevel < 4) {
					GamePanel.bullets.add(new Bullet(270, x, y));
					GamePanel.bullets.add(new Bullet(275, x + 5, y));
					GamePanel.bullets.add(new Bullet(265, x - 5, y));
				}
				else if(powerLevel < 5) {
					GamePanel.bullets.add(new Bullet(270, x, y));
					GamePanel.bullets.add(new Bullet(280, x + 10, y));
					GamePanel.bullets.add(new Bullet(275, x + 5, y));
					GamePanel.bullets.add(new Bullet(265, x - 5, y));
					GamePanel.bullets.add(new Bullet(260, x - 10, y));
				}
				else if(powerLevel < 7){
					GamePanel.bullets.add(new Bullet(270, x, y));
					GamePanel.bullets.add(new Bullet(285, x + 15, y));
					GamePanel.bullets.add(new Bullet(280, x + 10, y));
					GamePanel.bullets.add(new Bullet(275, x + 5, y));
					GamePanel.bullets.add(new Bullet(265, x - 5, y));
					GamePanel.bullets.add(new Bullet(260, x - 10, y));
					GamePanel.bullets.add(new Bullet(255, x - 15, y));
				}
				else {
					GamePanel.bullets.add(new Bullet(270, x, y));
					GamePanel.bullets.add(new Bullet(300, x + 30, y));
					GamePanel.bullets.add(new Bullet(295, x + 25, y));
					GamePanel.bullets.add(new Bullet(290, x + 20, y));
					GamePanel.bullets.add(new Bullet(285, x + 15, y));
					GamePanel.bullets.add(new Bullet(280, x + 10, y));
					GamePanel.bullets.add(new Bullet(275, x + 5, y));
					GamePanel.bullets.add(new Bullet(265, x - 5, y));
					GamePanel.bullets.add(new Bullet(260, x - 10, y));
					GamePanel.bullets.add(new Bullet(255, x - 15, y));
					GamePanel.bullets.add(new Bullet(250, x - 20, y));
					GamePanel.bullets.add(new Bullet(245, x - 25, y));
					GamePanel.bullets.add(new Bullet(240, x - 30, y));
				}
				
				
			}
		}
		// восстановление 
		if(recovering) {}
			long elapsed = (System.nanoTime() - recoveryTimer) / 1000000;
			if(elapsed > 2000) {
				recovering = false;
				recoveryTimer = 0;
			}
		}
		
	
	public void draw(Graphics2D g) {
		
		if(recovering) { // менять цвет когда попадает враг
			g.setColor(color2);
			g.fillOval(x - r, y - r, 2 * r, 2 * r);
		
			g.setStroke(new BasicStroke(1));
			g.setColor(color2.darker());
			g.drawOval(x - r,  y - r, 2 * r, 2 * r);
			g.setStroke(new BasicStroke(1));
		}
		else {
		
			g.setColor(color1);
			g.fillOval(x - r, y - r, 2 * r, 2 * r);
			
			g.setStroke(new BasicStroke(1));
			g.setColor(color1.darker());
			g.drawOval(x - r,  y - r, 2 * r, 2 * r);
			g.setStroke(new BasicStroke(1));
		}
	}
}
	
