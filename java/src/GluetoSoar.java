import java.util.Iterator;
import java.util.Vector;
import sml.Agent;
import sml.Identifier;
import sml.FloatElement;
import sml.IntElement;
import sml.StringElement;


import org.rlcommunity.rlglue.codec.types.Observation;


public class GluetoSoar{
	
	private static Identifier marioloc;
	private static Identifier inputLink;
	private static Agent agent;
	
	private static int prev_intArray0;
	
	private double Reward;
	private static FloatElement reward;
	private static Observation obs; 
	private static boolean flag[];
	
	static Monster Mario;
	private Identifier marioWME;
	private static IntElement marioNumMonsters;
	private static StringElement marioType;
	private static StringElement isInit;
	
	

	private static FloatElement mario_prevx;
	private static FloatElement mario_xd;
	private static FloatElement mario_yd;
	private static FloatElement mario_x;
	private static FloatElement mario_y;
	private static IntElement mario_xi;
	private static IntElement mario_yi;
	private static FloatElement mario_sx;
	private static FloatElement mario_sy;
	private static double prev_xloc;
	
	private static Identifier visualScene;
	private static Identifier[] tileRowWME;
	private static IntElement ypos;
	private static Identifier[][] tileWME;
	private static Identifier[][] tileWMEcp;
	private static IntElement xpos;
	private static StringElement type;
	
	private static Vector <Block> blockRewards;
	
	private static Vector <TileBlock> blocks;
	
	static Vector<Monster> monsters;
	static Vector<MonsterWME> monstersPresent;
	private static Identifier monstersWME;
	private static FloatElement right_reward;
	private static Identifier tileBlockId;
		
	public GluetoSoar(Agent a){
		agent = a;
		inputLink = agent.GetInputLink();
		
		prev_intArray0 = -1;
		
		Reward = 0.0;
		
		prev_xloc = 0.0;
		
		reward = agent.CreateFloatWME(inputLink, "reward", 0.0);
		right_reward = agent.CreateFloatWME(inputLink, "right-reward", 0.0);
		
		isInit = agent.CreateStringWME(inputLink, "init","no");
		marioWME = agent.CreateIdWME(inputLink, "mario");
		tileBlockId = agent.CreateIdWME(inputLink, "block-objects");
		marioType = agent.CreateStringWME(marioWME, "type","small");
		
		//added to support rewarding the agent on moving towards right
		mario_prevx = agent.CreateFloatWME(marioWME, "prevx", 0.0);
		mario_x = agent.CreateFloatWME(marioWME, "x", 0);
		mario_y = agent.CreateFloatWME(marioWME, "y", 0);
		mario_xi = agent.CreateIntWME(marioWME, "xi", 0);
		mario_yi = agent.CreateIntWME(marioWME, "yi", 0);
		mario_xd = agent.CreateFloatWME(marioWME, "xd", 0);
		mario_yd = agent.CreateFloatWME(marioWME, "yd", 0);
		mario_sx = agent.CreateFloatWME(marioWME, "sx", 0.0);
		mario_sy = agent.CreateFloatWME(marioWME, "sy", 0.0);
		
		
		marioNumMonsters = agent.CreateIntWME(inputLink, "num_monsters", 0);		
		visualScene = agent.CreateIdWME(inputLink, "visual-scene");
		tileRowWME = new Identifier[16];
		tileWME = new Identifier[16][21];
		tileWMEcp = new Identifier[16][21];
		
		for (int i = 0; i < 16; i++){
			tileRowWME[i] = agent.CreateIdWME(visualScene, "tile-row");
			ypos = agent.CreateIntWME(tileRowWME[i], "y", 15-i);
		 	for (int j = 0; j < 21; j++){
				tileWME[i][j] = agent.CreateIdWME(tileRowWME[i], "tile");
				tileWMEcp[i][j] = tileWME[i][j];
		 	}

		}
		monstersWME = agent.CreateIdWME(inputLink, "monsters");
		
		monsters = new Vector <Monster>();
		monstersPresent = new Vector <MonsterWME>();
		blockRewards = new Vector <Block> ();
		
		blocks = new Vector <TileBlock>(); 
	
	}
	public static char getTileAt(double xf, double yf) {
		int x = (int)xf;
		if (x<0)
			return '7';
		int y = 16-(int)yf;
		x -= obs.intArray[0];
		if (x<0 || x>21 || y<0 || y>15)
			return '\0';
		int index = y*22+x;
		return obs.charArray[index];
	}
	
	public static Vector<Monster> getMonsters() {
		blockRewards.clear();
		Vector<Monster> monster_vec = new Vector<Monster>();
		int i = 0;
		for (; 1+2*i<obs.intArray.length &&  obs.intArray[1+2*i] != 100; i++) {
			Monster m = new Monster();
			m.type = obs.intArray[1+2*i];
			m.winged = obs.intArray[2+2*i]!=0;
			switch (m.type) {
			case 0:
				m.typeName = "Mario";
				break;
			case 1:
				m.typeName = "Red Koopa";
				break;
			case 2:
				m.typeName = "Green Koopa";
				break;
			case 3:
				m.typeName = "Goomba";
				break;
			case 4:
				m.typeName = "Spikey";
				break;
			case 5:
				m.typeName = "Piranha Plant";
				break;
			case 6:
				m.typeName = "Mushroom";
				break;
			case 7:
				m.typeName = "Fire Flower";
				break;
			case 8:
				m.typeName = "Fireball";
				break;
			case 9:
				m.typeName = "Shell";
				break;
			case 10:
				m.typeName = "Big Mario";
				break;
			case 11:
				m.typeName = "Fiery Mario";
				break;
			}
			m.x = obs.doubleArray[5*i];
			m.y = obs.doubleArray[5*i+1];
			m.sx = obs.doubleArray[5*i+2];
			m.sy = obs.doubleArray[5*i+3];
			m.reward = obs.doubleArray[5*i+4];
			SoarMarioAgent.RewardMonsters += m.reward;
	//		System.out.println("Reward for monster " + m.typeName + " is " + m.reward + " x is " + m.x);
			monster_vec.add(m);
		}
		
		int j = i*2+2;
		//System.out.println("J is " + j + "length of array is " + obs.intArray.length);
		for (; j < obs.intArray.length; j+=3){
		//	System.out.println("Tile x: " + obs.intArray[j] + " y: " + obs.intArray[j+1] + " reward: " + obs.intArray[j+2]);
			blockRewards.add(new Block (obs.intArray[j], obs.intArray[j+1], obs.intArray[j+2]));
			SoarMarioAgent.RewardBlocks += obs.intArray[j+2];
			
		}
		
		return monster_vec;
	}
	
	public static Monster getMario() {
		monsters = getMonsters();
		for (Monster m : monsters) {
			if ((m.type == 0)||(m.type == 10)||(m.type == 11))
				return m;
		}
		return null;
	}
	
	public static void writeToSoar(Observation o, double r){
		obs = o;
		agent.Update(isInit,"no");
		Mario = getMario();
		
		/*---------------------------Modified reward function ------------------------------------*/
	
		if(Mario.x > prev_xloc){
			//reward the agent on moving towards right
			double right = 0.00;
			agent.Update(right_reward, right);
			//System.out.println("reward = "+ r);
			}
		else 
			{ 	double right = -0.01;
				agent.Update(right_reward, right);
			//System.out.println("reward = "+ r);
			}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		switch (Mario.type) {
		case 0:
			agent.Update(marioType, "Small");
			break;
		case 10:
			agent.Update(marioType, "Big");
			break;
		case 11:
			agent.Update(marioType, "Fiery");
			break;
		}
		int tempx = (int)(2*Mario.x);
		int tempy = (int)(2*Mario.y);
		agent.Update(mario_xd, Mario.x);
		agent.Update(mario_yd, Mario.y);
		agent.Update(mario_xi, (int)Mario.x);
		agent.Update(mario_yi, (int)Mario.y);
		agent.Update(mario_x, tempx/2.0);
		agent.Update(mario_y, tempy/2.0);
		agent.Update(mario_sx, Mario.sx);
		agent.Update(mario_sy, Mario.sy);
		agent.Update(mario_prevx, prev_xloc);
		prev_xloc = Mario.x;
		
		agent.Update(marioNumMonsters, monsters.size());
		
		getVisualScene(o);
		getVisualSceneBlocks(o);
		getMonsterList(o);	
		agent.Commit();
		prev_intArray0 = o.intArray[0];
	}
	
	private static void getMonsterList(Observation o){
		boolean flag = false;
		boolean[] prev_flag = new boolean[20];
		int start = 0;
	//	monsters = getMonsters();

	   int k = 0;
		while (k < monsters.size()){
			if ((monsters.elementAt(k).type == 0) || (monsters.elementAt(k).type == 10) || (monsters.elementAt(k).type == 11)){
				monsters.remove(k);
			}
			else k++;
	    }
		//System.out.println(" present size0 " + monstersPresent.size());
		k = 0;
		while(k < monstersPresent.size()){
				if(k < monsters.size()){
					int tempx = (int)(2*monsters.elementAt(k).x);
					int tempy = (int)(2*monsters.elementAt(k).y);
				//	System.out.println("Double distance, x: "+ monsters.elementAt(k).x+"y: " + monsters.elementAt(k).y + " Int distance, x: " + tempx/4.0 + "y: " +tempy/4.0);
					if(monstersPresent.elementAt(k).typeName.equals(monsters.elementAt(k).typeName)){ 
						agent.Update(monstersPresent.elementAt(k).monster_x, tempx/2.0);
						agent.Update(monstersPresent.elementAt(k).monster_y, tempy/2.0);
						
						
						//changed the discreteization to single time///
						//change back for some agent runs//
						//or make this a parameter of the program//
					//	agent.Update(monstersPresent.elementAt(k).monster_x, (int)(monsters.elementAt(k).x));
					//	agent.Update(monstersPresent.elementAt(k).monster_y, (int)(monsters.elementAt(k).y));
						agent.Update(monstersPresent.elementAt(k).monster_xd, monsters.elementAt(k).x);
						agent.Update(monstersPresent.elementAt(k).monster_yd, monsters.elementAt(k).y);
						agent.Update(monstersPresent.elementAt(k).monster_sx, monsters.elementAt(k).sx);
						agent.Update(monstersPresent.elementAt(k).monster_sy, monsters.elementAt(k).sy);
						agent.Update(monstersPresent.elementAt(k).monster_reward, monsters.elementAt(k).reward);
						agent.Commit();
						k++;
					}
					else{
							agent.DestroyWME(monstersPresent.elementAt(k).monsterWME);
							monstersPresent.remove(k);
						}
				}
				else{
					agent.DestroyWME(monstersPresent.elementAt(k).monsterWME);
					monstersPresent.remove(k);
				}
		}
		
		//System.out.println(" present size1 " + monstersPresent.size() + " " + monsters.size());
		
		for(int i = monstersPresent.size(); i < monsters.size(); i++){
				MonsterWME addNew = new MonsterWME();
				addNew.x = monsters.elementAt(i).x;
				addNew.y = monsters.elementAt(i).y;
				addNew.sx = monsters.elementAt(i).sx;
				addNew.sy = monsters.elementAt(i).sy;
				addNew.type = monsters.elementAt(i).type;
				addNew.typeName = monsters.elementAt(i).typeName;
				addNew.winged = monsters.elementAt(i).winged;
				addNew.reward = monsters.elementAt(i).reward;
				addNew.monsterWME = agent.CreateIdWME(monstersWME, "monster");
				addNew.monster_type = agent.CreateStringWME(addNew.monsterWME, "type", monsters.elementAt(i).typeName);
				if (addNew.winged)
					addNew.monster_winged = agent.CreateStringWME(addNew.monsterWME, "winged", "yes");
				else 
					addNew.monster_winged = agent.CreateStringWME(addNew.monsterWME, "winged", "no");
				int tempx = (int)(2*monsters.elementAt(i).x);
				int tempy = (int)(2*monsters.elementAt(i).y);
			//	addNew.monster_x = agent.CreateIntWME(addNew.monsterWME, "x", (int)(monsters.elementAt(k).x));
			//	addNew.monster_y  = agent.CreateIntWME(addNew.monsterWME, "y", (int)(monsters.elementAt(k).y));
				addNew.monster_x = agent.CreateFloatWME(addNew.monsterWME, "x", tempx/2.0);
				addNew.monster_y  = agent.CreateFloatWME(addNew.monsterWME, "y", tempy/2.0);
				addNew.monster_xd = agent.CreateFloatWME(addNew.monsterWME,"xd",monsters.elementAt(i).x);
				addNew.monster_yd = agent.CreateFloatWME(addNew.monsterWME,"yd",monsters.elementAt(i).y);
				addNew.monster_sx = agent.CreateFloatWME(addNew.monsterWME, "sx", monsters.elementAt(i).sx);
				addNew.monster_sy  = agent.CreateFloatWME(addNew.monsterWME, "sy", monsters.elementAt(i).sy);
				addNew.monster_reward = agent.CreateFloatWME(addNew.monsterWME, "reward", monsters.elementAt(i).reward);
				agent.CreateStringWME(addNew.monsterWME, "group", "monster");
				monstersPresent.add(addNew);
		}
		//System.out.println(" present size2 " + monstersPresent.size() + " " + monsters.size());
	
		/*for (int i = 0; i < monstersPresent.size(); i ++){
				System.out.println("" + monsters.elementAt(i).typeName + " " + monstersPresent.elementAt(i).typeName);
		}*/
	}
	
	
	private static float getBlockRewardAt(int xe, int ye){
		Iterator<Block> itr = blockRewards.iterator();
		while (itr.hasNext()){
			Block b = itr.next();
			if (b.x == xe && b.y == ye)
			{
		//		System.out.println("Block x: " + b.x + " y:" + b.y +  "reward is: " + b.reward);
				return (float) b.reward;
			}
		}
		return (float) 0.0;
	}

	private static void getVisualScene(Observation o){
		
		if (prev_intArray0==-1){
			for(int i = 0; i < 16; i++){
				for(int k = 0; k < 21; k++ ){
					if(o.charArray[i*22+k] == 'M'){
						o.charArray[i*22+k] = ' ';
					}
					xpos = agent.CreateIntWME(tileWME[i][k], "x",o.intArray[0]+k );
					type = agent.CreateStringWME(tileWME[i][k], "type", Character.toString(o.charArray[i*22+k]));
					agent.CreateFloatWME(tileWME[i][k],"reward", 0.0);
				}
			}
		}
		
		else {
			int offset = o.intArray[0] - prev_intArray0;
			if (offset >= 0){
				for(int i = 0; i < 16; i++){
					for(int k = 0; k < offset; k++){
						agent.DestroyWME(tileWME[i][k]);
					}
					for(int k = offset; k < 21; k++){
						if(o.charArray[i*22+k-offset] == 'M'){
							o.charArray[i*22+k-offset] = ' ';
						}
						String temp_type = tileWME[i][k].GetChild(1).GetValueAsString();
						if (temp_type.equals(Character.toString(o.charArray[i*22+k-offset]))){
							agent.Update(tileWME[i][k].GetChild(2).ConvertToFloatElement(), 0.0);
							tileWMEcp[i][k-offset] = tileWME[i][k];
						}
						else {
							agent.DestroyWME(tileWME[i][k]);
							agent.Commit();
							tileWME[i][k] = agent.CreateIdWME(tileRowWME[i], "tile");
						    xpos = agent.CreateIntWME(tileWME[i][k], "x",o.intArray[0]+k-offset);
						    type = agent.CreateStringWME(tileWME[i][k], "type", Character.toString(o.charArray[i*22+k-offset]));
						//    System.out.println ("Changed at: " + (o.intArray[0]+k-offset) + " " + i);
						    agent.CreateFloatWME(tileWME[i][k], "reward", getBlockRewardAt(o.intArray[0]+k-offset, i));
						    tileWMEcp[i][k-offset] =  tileWME[i][k];
						}
					}
					for(int k = 21-offset; k < 21; k++){
						if(o.charArray[i*22+k] == 'M'){
							o.charArray[i*22+k] = ' ';
						}
						tileWMEcp[i][k] = agent.CreateIdWME(tileRowWME[i], "tile");
					    xpos = agent.CreateIntWME(tileWMEcp[i][k], "x",o.intArray[0]+k);
					    type = agent.CreateStringWME(tileWMEcp[i][k], "type", Character.toString(o.charArray[i*22+k]));
					    agent.CreateFloatWME(tileWMEcp[i][k], "reward", 0.0);
					}
				}
			}
			else{
				for(int i = 0; i < 16; i++){
					for(int k = 20; k > 20+offset; k--){
						agent.DestroyWME(tileWME[i][k]);
					}
					for(int k = -offset; k < 21; k++){
						if(o.charArray[i*22+k] == 'M'){
							o.charArray[i*22+k] = ' ';
						}
						String temp_type = tileWME[i][k+offset].GetChild(1).GetValueAsString();
						if (temp_type.equals(Character.toString(o.charArray[i*22+k]))){
							agent.Update(tileWME[i][k+offset].GetChild(2).ConvertToFloatElement(), 0.0);
							tileWMEcp[i][k] = tileWME[i][k+offset];
						}
						else {
							agent.DestroyWME(tileWME[i][k+offset]);
							agent.Commit();
							tileWME[i][k+offset] = agent.CreateIdWME(tileRowWME[i], "tile");
						    xpos = agent.CreateIntWME(tileWME[i][k+offset], "x",o.intArray[0]+k);
						    type = agent.CreateStringWME(tileWME[i][k+offset], "type", Character.toString(o.charArray[i*22+k]));
						    agent.CreateFloatWME(tileWME[i][k+offset], "reward",getBlockRewardAt(i,k));
						    tileWMEcp[i][k] =  tileWME[i][k+offset];
						}
					}
					for(int k = 0; k < -offset; k++){
						if(o.charArray[i*22+k] == 'M'){
							o.charArray[i*22+k] = ' ';
						}
						tileWMEcp[i][k] = agent.CreateIdWME(tileRowWME[i], "tile");
					    xpos = agent.CreateIntWME(tileWMEcp[i][k], "x",o.intArray[0]+k);
					    type = agent.CreateStringWME(tileWMEcp[i][k], "type", Character.toString(o.charArray[i*22+k]));
					    agent.CreateFloatWME(tileWMEcp[i][k], "reward", 0.0);
					}
				}
				
			}
		}
		
	//	System.out.println("======");
		
		for (int i = 0;i <16; i++){
			for(int k = 0; k < 21; k++){
				tileWME[i][k] = tileWMEcp[i][k];
		//System.out.print(tileWMEcp[i][k].GetChild(1).GetValueAsString());
			}
		//	System.out.println();
		}

	}

	
	
	
	
	public static void getVisualSceneBlocks(Observation o){
		boolean found = false;
		// add new block objects
		for (int i = 0; i < 16; i++){
			for (int k = 0; k < 22; k++){
				if (o.charArray[i*22+k] == '$' || o.charArray[i*22+k] == '?' || o.charArray[i*22+k] == 'b'){
					Iterator itr = blocks.iterator();
					found = false;
					while(itr.hasNext()){
						TileBlock temp = (TileBlock) itr.next();
						if(temp.x == o.intArray[0]+k && temp.y == 15-i){
							found = true;
						}
					}
					if(!found){
						TileBlock temp = new TileBlock();
						temp.x = o.intArray[0]+k;
						temp.y = 15-i;
						temp.orig_type = Character.toString(o.charArray[i*22+k]);
						temp.type = Character.toString(o.charArray[i*22+k]);
						temp.tileBlockId = agent.CreateIdWME(tileBlockId, "block");
						temp.tile_x_id = agent.CreateIntWME(temp.tileBlockId, "x", temp.x);
						temp.tile_y_id = agent.CreateIntWME(temp.tileBlockId, "y", temp.y);
						temp.tile_type_id = agent.CreateStringWME(temp.tileBlockId, "type", temp.type);
						temp.tile_group_id = agent.CreateStringWME(temp.tileBlockId, "group", temp.group);
						temp.reward_id = agent.CreateFloatWME(temp.tileBlockId, "reward", getBlockRewardAt(k,i));
						blocks.add(temp);
					}
				}
			}
		}
		
		for (int i = 0; i < blocks.size(); i++){
			TileBlock temp = blocks.get(i);
			if (temp.x < o.intArray[0] || temp.x > o.intArray[0]+21){
		//		System.out.println("Remove block (out of bounds) ");
				agent.DestroyWME(temp.tileBlockId);
				blocks.remove(i);
			}
			else {
				agent.Update(temp.reward_id,getBlockRewardAt(temp.x,temp.y));
				temp.type = Character.toString(o.charArray[(15-temp.y)*22+(temp.x-o.intArray[0])]);
				agent.Update(temp.tile_type_id, temp.type);
				if ((temp.type.equals("7") || temp.type.equals(" ")) && temp.orig_type.equals("remove")){
					agent.DestroyWME(temp.tileBlockId);
					blocks.remove(i);
			//		System.out.println("Remove block (empty) ");
				}
				else if ((temp.type.equals("7") || temp.type.equals(" ")) && (temp.orig_type.equals("$") || temp.orig_type.equals("?") || temp.orig_type.equals("b"))){
					temp.orig_type = "remove";
					agent.Update(temp.reward_id,getBlockRewardAt(temp.x,(15-temp.y)));
			//		System.out.println("Original type " + temp.orig_type);
				}
			}
		}
	}
	
	
	public static void writeToSoar(double r){
		agent.Update(isInit,"yes");
		agent.Update(reward, r);
		agent.Commit();
	}
	
	public static void reset(){
		prev_intArray0 = -1;
		
		agent.DestroyWME(visualScene);
		agent.Commit();
		visualScene = agent.CreateIdWME(inputLink, "visual-scene");
		for (int i = 0; i < 16; i++){
			tileRowWME[i] = agent.CreateIdWME(visualScene, "tile-row");
			ypos = agent.CreateIntWME(tileRowWME[i], "y", 15-i);
		 	for (int j = 0; j < 21; j++){
				tileWME[i][j] = agent.CreateIdWME(tileRowWME[i], "tile");
				tileWMEcp[i][j] = tileWME[i][j];
		 	}
		}
		
		agent.DestroyWME(monstersWME);
		agent.DestroyWME(tileBlockId);
		blocks.removeAllElements();
		monstersPresent.removeAllElements();
		monstersWME = agent.CreateIdWME(inputLink, "monsters");
		tileBlockId = agent.CreateIdWME(inputLink, "block-objects");
		agent.Commit();

	}
	
	private static Vector getReward(){
		return monsters;
		
	}
	
	
}