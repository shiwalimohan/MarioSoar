package edu.rutgers.rl3.comp;

public class Block {
	public double reward = 0.0;
	public int x ;
	public int y;
	public String type;
	public String group;
	public Block(int xtemp, int ytemp, float rewardTemp) {
		x = xtemp;
		y = ytemp;
		reward = rewardTemp;
	}
	public Block getBlock(){
		return this;
	}
}
