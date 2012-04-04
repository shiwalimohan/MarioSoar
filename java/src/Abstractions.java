import sml.FloatElement;
import sml.Identifier;
import sml.IntElement;
import sml.StringElement;


public class Abstractions{
	static class Monster {
		double x;
		double y;
		double sx;
		double sy;
		int type;			
		String typeName;
		boolean winged;
		public double reward;
	}
	static class MonsterWME{
		double x;
		double y;
		double sx;
		double sy;
		int type;		
		double reward;
		String typeName;
		boolean winged;
		Identifier monsterWME;
		StringElement monster_type;
		StringElement monster_winged;
		FloatElement monster_xd;
		FloatElement monster_yd;
		FloatElement monster_reward;

		//For discretization of space around monsters. uncomment 
				FloatElement monster_x;
			FloatElement monster_y;
		
	    //IntElement monster_x;
		//IntElement monster_y;
		
		FloatElement monster_sx;
		FloatElement monster_sy;
		boolean flag;
	}
	static class TileBlock {
		public int x;
		public int y;
		public String type;
		public String orig_type;
		public String group = "block";
		Identifier tileBlockId;
		IntElement tile_x_id;
		IntElement tile_y_id;
		StringElement tile_type_id;
		FloatElement reward_id;
		StringElement tile_group_id;
	}
	
}
