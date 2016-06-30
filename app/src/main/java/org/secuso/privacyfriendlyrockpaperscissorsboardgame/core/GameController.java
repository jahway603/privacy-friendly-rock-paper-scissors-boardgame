package org.secuso.privacyfriendlyrockpaperscissorsboardgame.core;

import android.content.Context;
import android.widget.Toast;

import org.secuso.privacyfriendlyrockpaperscissorsboardgame.ui.RPSBoardLayout;
import org.secuso.privacyfriendlyrockpaperscissorsboardgame.ui.RPSFieldView;

import java.util.List;

/**
 * Created by david on 06.05.2016.
 */
public class GameController {
    IPlayer p0;
    IPlayer p1;
    IPlayer playerOnTurn;
    int fieldX;
    int fieldY;
    RPSBoardLayout view;
    GameState model;
    boolean cellSelected;
    int selX;
    int selY;
    Context context;
    /**
     * Starts a new Game in Standard 8x8 Layout
     */
    public GameController(){
        this.fieldX=8;
        this.fieldY=8;
        cellSelected=false;
    }


    /**
     * Starts a GameController with a saved instance. This can either be a savegame or the advencing into the next level when a game is draw for now.
     * @param fieldX describes how many fields we have in x direction
     * @param fieldY describes how many fields we have in y direction
     */
    public GameController(int fieldX, int fieldY, Context context){
        this.fieldX=fieldX;
        this.fieldY=fieldY;
        this.context=context;
    }

    public int getX(){
        return fieldX;
    }

    public int getY(){
        return fieldY;
    }

    /**
     * Starts a new game. Creates the model, requests the starting assignments from both players and initializes the first turn
     * @param view the Board view to use as view
     */
    public void startGame(RPSBoardLayout view){
        this.p0=new NormalPlayer(0);
        this.p1=new NormalPlayer(1);
        this.playerOnTurn=this.p1;
        this.view=view;
        this.model=new GameState(fieldX,fieldY);
        this.model.setGamePane(this.placeFigures(p0.provideInitialAssignment(this.getX()*2),p1.provideInitialAssignment(this.getX()*2)));
        view.drawFigures(this.getRepresentationForPlayer());
    }

    /**
     * Places the figures of both players randomly in the correct spots for the starting assignment
     * @param figuresP0 the figures of p0
     * @param figuresP1 the figures of p1
     * @return the starting gamepane
     */
    public RPSGameFigure[][] placeFigures(List<RPSGameFigure> figuresP0,List<RPSGameFigure> figuresP1){
        RPSGameFigure[][] gamePane=new RPSGameFigure[fieldY][fieldX];
        for(int i =0; i<gamePane.length;i++){
            for(int j=0;j<gamePane[i].length;j++){
                gamePane[i][j]=null;
            }
        }
        for(int i=0;i<16;i++){
            gamePane[i/8][i%8]=figuresP0.get(i);
        }
        for(int i=0;i<16;i++){
            gamePane[6+i/8][i%8]=figuresP1.get(i);
        }
        return gamePane;

    }

    /**
     * Implements the Figure Hiding Game and the bottom up view for the player that is currently on turn.
     * @return
     */
    public RPSFigure[][] getRepresentationForPlayer(){
        RPSGameFigure[][] originalGamePane=model.getGamePane();
        RPSFigure[][] representationForPlayer= new RPSFigure[originalGamePane.length][originalGamePane[0].length];
        if(playerOnTurn==p1){
            for(int i=0;i<originalGamePane.length;i++){
                for(int j=0;j<originalGamePane[i].length;j++){
                    if(originalGamePane[i][j]!=null&&!(originalGamePane[i][j].getOwner()==p0.getId())){
                        representationForPlayer[getY()-1-i][getX()-1-j]=originalGamePane[i][j].isHidden()?RPSFigure.GHOST:originalGamePane[i][j].getType();
                    }
                    else representationForPlayer[getY()-1-i][getX()-1-j]=originalGamePane[i][j]==null?null:originalGamePane[i][j].getType();
                }
            }
        }
        else{
            for(int i=0;i<originalGamePane.length;i++){
                for(int j=0;j<originalGamePane[i].length;j++){
                    if(originalGamePane[i][j]!=null&&!(originalGamePane[i][j].getOwner()==p1.getId())){
                        representationForPlayer[i][j]=originalGamePane[i][j].isHidden()?RPSFigure.GHOST:originalGamePane[i][j].getType();
                    }
                    else representationForPlayer[i][j]=originalGamePane[i][j]==null?null:originalGamePane[i][j].getType();
                }
            }
        }
        return representationForPlayer;
    }

    public void forceRedraw(){
        this.view.drawFigures(this.getRepresentationForPlayer());
    }

    public void playerMove(int xTarget, int yTarget){
        try {
            validateMove(this.selX, this.selY,xTarget,yTarget);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.cellSelected=false;
    }

    public void selectCell(int x, int y){
            Toast.makeText(this.context, "Select Cell "+y+", "+x, Toast.LENGTH_SHORT).show();
            this.cellSelected=true;
            this.selX=x;
            this.selY=y;
    }

    public void deselect(){
        this.cellSelected=false;
        Toast.makeText(this.context, "Deselect Cell "+selY+", "+selX, Toast.LENGTH_SHORT).show();
    }
    public boolean isCellSelected(){
        return this.cellSelected;
    }

    public int getSelX(){
        return this.selX;
    }

    public int getSelY(){
        return this.selY;
    }

    private void validateMove(int xStart, int yStart, int xTarget, int yTarget) throws InterruptedException {
        //Check if movement direction and length is fine
        if(Math.abs(xStart-xTarget)==1) {
            if (Math.abs(yStart - yTarget) == 0) {
                ;
            }
            else{
                Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
                return ;
            }
        }
        else if(Math.abs(xStart-xTarget)==0) {
            if (Math.abs(yStart - yTarget) == 1) {
                ;
            } else {
                Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
                return ;
            }
        }
        else {
            Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
            return ;
        }
        //Check if source field is occupied
        RPSGameFigure[][] gamePane=this.model.getGamePane();
        //Check if figure on field is of current Player
        if(gamePane[yStart][xStart]==null) {
            Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
            return;
        }
        if(playerOnTurn.getId()==p1.getId()){
            if(gamePane[yStart][xStart].getOwner()!=p1.getId()) {
                Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
                return ;
            }
        }
        else if (gamePane[getY()-1-yStart][getX()-1-xStart].getOwner()!=p0.getId()){
            Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
            return ;
        }
        if(playerOnTurn.getId()==p1.getId())
            //Check if target field is empty
            if(gamePane[getY()-1-yTarget][getX()-1-xTarget]==null) {
                gamePane[getY()-1-yTarget][getX()-1-xTarget]=gamePane[getY()-1-yStart][getX()-1-xStart];
                gamePane[getY()-1-yStart][getX()-1-xStart]=null;
                updateModelAndView(gamePane);
                //wait(2000);
                this.playerOnTurn=p0;
                this.forceRedraw();
                return ;
            }
            else{
                    if(gamePane[yStart][xStart].getOwner()!=p1.getId()) {
                        //TODO: initiateFight
                        updateModelAndView(gamePane);
                        //wait(2000);
                        this.playerOnTurn=p0;
                        this.forceRedraw();
                        return ;
                    }
                    else{
                        Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
                        return ;
                    }
                }
        else{
            //Check if target field is empty
            if(gamePane[yTarget][xTarget]==null){
                gamePane[yTarget][xTarget]=gamePane[yStart][xStart];
                gamePane[yStart][xStart]=null;
                updateModelAndView(gamePane);
                //wait(2000);
                this.playerOnTurn=p0;
                this.forceRedraw();
                return ;
            }
            else{
                if(gamePane[getY()-1-yStart][getX()-1-xStart].getOwner()!=p0.getId()) {
                    //TODO: initiateFight
                    updateModelAndView(gamePane);
                    //wait(2000);
                    this.playerOnTurn=p0;
                    this.forceRedraw();
                    return ;
                }
                else{
                    Toast.makeText(this.context, "Invalid Move", Toast.LENGTH_SHORT).show();
                    return ;
                }
            }
        }
    }

    void updateModelAndView(RPSGameFigure[][] newPane){
        model.setGamePane(newPane);
        this.view.drawFigures(this.getRepresentationForPlayer());
    }
}
