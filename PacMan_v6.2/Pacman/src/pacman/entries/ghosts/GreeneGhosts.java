package pacman.entries.ghosts;

//import java.awt.Color;
import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
//import pacman.game.GameView;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getActions() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.ghosts.mypackage).
 */
public class GreeneGhosts extends Controller<EnumMap<GHOST,MOVE>>
{
	public int tiles = 4;
	public long time = 0;
	public long prevTime = getTime();
	
	public boolean scared = false;
	private EnumMap<GHOST, MOVE> myMoves=new EnumMap<GHOST, MOVE>(GHOST.class);
	
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue)
	{
		//System.out.println(game.getPacmanCurrentNodeIndex());
		//System.out.println(game.getCurrentLevel());
		//GameView.addPoints(game, Color.RED , 0);
		//GameView.addPoints(game, Color.RED , 1217);
		//GameView.addLines(game, Color.RED, 0, 1217);
		
		if(!scared) { //if a ghost is scared then don't count the time
			time += getTime()-prevTime;
		}
		prevTime = getTime();
		
		scared = false; //default not scared
		myMoves.clear();
		//System.out.println(time);
		for(GHOST ghostType:GHOST.values()) {
			//ghost scared if edible
			scared = scared || game.isGhostEdible(ghostType);
			if(game.doesGhostRequireAction(ghostType)) {
				int mvIndex;
				//check if ghost is scared and edible
				if(game.isGhostEdible(ghostType)) {
					mvIndex = ghostScare(game, ghostType);
				} else if (time <= 7 //Scatter for 7 chase for 20
						            || (time > 28 && time < 36) //Scatter for 7 chase for 20
						            || (time > 56 && time < 61) //Scatter for 5 chase for 20
						           || (time > 81 && time < 86)) { //Scatter for 5 and then chase permanently
							mvIndex = Scatter(game, ghostType);
				} else {
						if(ghostType == GHOST.INKY) {
							mvIndex = INKYChase(game, ghostType);
						}
						else if(ghostType == GHOST.SUE) {
							mvIndex = CLYDEChase(game, ghostType);
						}
						else if(ghostType == GHOST.PINKY) {
							mvIndex = PINKYChase(game, ghostType);
						}
						else mvIndex = BLINKYChase(game, ghostType);
				} myMoves.put(ghostType, game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghostType), mvIndex, game.getGhostLastMoveMade(ghostType), Constants.DM.EUCLID));
			}
			
		}
		return myMoves;
	}
	public int INKYChase(Game game, GHOST ghostType) {
        int mvIndex = game.getPacmanCurrentNodeIndex();
        System.out.println("Inky chase activated");
        for(int i = 0; i < (tiles * 2); i++) {
            int tempIndex = game.getNeighbour(mvIndex, game.getPacmanLastMoveMade());
            if (tempIndex <= 0) break;
            mvIndex = tempIndex;
        }
        int xVec = game.getNodeXCood(mvIndex);
        int yVec = game.getNodeYCood(mvIndex);
        int realMVIndex = mvIndex;
        // distance to Blinky
        int xDist = (xVec - game.getNodeXCood(game.getGhostCurrentNodeIndex(GHOST.BLINKY)));
        int yDist = (yVec - game.getNodeYCood(game.getGhostCurrentNodeIndex(GHOST.BLINKY)));
        //loop through x
        while(xDist != 0) {
            if(xDist > 0) {
                xDist--;
                realMVIndex = GetDirectionalIndex(game, mvIndex, MOVE.RIGHT);
            } else {
                xDist++;
                realMVIndex = GetDirectionalIndex(game, mvIndex, MOVE.LEFT);
            }
        }
        //loop through y
        while(yDist != 0) {
            if(yDist > 0) {
                yDist--;
                realMVIndex = GetDirectionalIndex(game, mvIndex, MOVE.UP);
            } else {
                yDist++;
                realMVIndex = GetDirectionalIndex(game, mvIndex, MOVE.DOWN);
            }
        }
        return realMVIndex;
    }
	
	// Clyde chase is the same as blinky but just scatter around
    public int CLYDEChase(Game game, GHOST ghostType) {
        // get the dist to pacman
    	//System.out.println("Clyde chase activated");
        double pacDist = game.getDistance(
                game.getGhostCurrentNodeIndex(ghostType),
                game.getPacmanCurrentNodeIndex(),
                Constants.DM.EUCLID);
        if (pacDist <= (tiles * 8f)) {
        	//System.out.println("Clyde scatter");
        	return game.getPowerPillIndices()[2];
        }
        else return BLINKYChase(game, ghostType);
    }
    
    public int PINKYChase(Game game, GHOST ghostType) {
    	int mvIndex = game.getPacmanCurrentNodeIndex();
    	//System.out.println("Pinky chase activated");
    	for (int i = 0; i < (tiles * 4); i++) {
    		int tempIndex = game.getNeighbour(mvIndex, game.getPacmanLastMoveMade());
            if (tempIndex <= 0) break;
            mvIndex = tempIndex;
    	}
    	return mvIndex;
    }
    
    public int BLINKYChase(Game game, GHOST ghostType) {
    	//System.out.println("blinky chase activated");
    	return game.getPacmanCurrentNodeIndex();
    }
    
    public int GetDirectionalIndex(Game game, int mvIndex, MOVE move) {
        int tempIndex = game.getNeighbour(mvIndex, move);

        //if you hit a wall break away 
        if (tempIndex <= 0) {
            return mvIndex;
        }
        return tempIndex;
    }
    
	public long getTime() {
		return System.currentTimeMillis()/1000;
	}
	
	public int ghostScare(Game game, GHOST ghostType) {
		//grab the array of neighboring nodes
		int[] neighbor = game.getNeighbouringNodes(game.getGhostCurrentNodeIndex(ghostType));
		//return random node
		return neighbor[(int) Math.floor(Math.random() * neighbor.length)];
	}
	
	public int Scatter(Game game, GHOST ghost) {
		if(game.getMazeIndex()==0) {
			//inky bottom right corner
			if(ghost == GHOST.INKY) return 1291;
			//clyde bottom left corner
			else if(ghost == GHOST.SUE) return 1191;
			//pinky gets top left corner
			else if(ghost == GHOST.PINKY) return 0;
			//and blinky gets top right corner
			else return 78;
		}
		else if(game.getMazeIndex()==1) {
			//inky bottom right corner
			if(ghost == GHOST.INKY) return 1317;
			//clyde bottom left corner
			else if(ghost == GHOST.SUE) return 1217;
			//pinky gets top left corner
			else if(ghost == GHOST.PINKY) return 0;
			//and blinky gets top right corner
			else return 86;
		}
		else if(game.getMazeIndex()==2) {
			//inky bottom right corner
			if(ghost == GHOST.INKY) return 1378;
			//clyde bottom left corner
			else if(ghost == GHOST.SUE) return 1300;
			//pinky gets top left corner
			else if(ghost == GHOST.PINKY) return 0;
			//and blinky gets top right corner
			else return 78;
		}
		else { //inky bottom right corner
			if(ghost == GHOST.INKY) return 1307;
			//clyde bottom left corner
			else if(ghost == GHOST.SUE) return 1218;
			//pinky gets top left corner
			else if(ghost == GHOST.PINKY) return 0;
			//and blinky gets top right corner
			else return 100;
		}
	}
}