package javaLab.game;

import javaLab.graphics.G;
import javaLab.graphics.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static javaLab.game.Tetris.Shape.*;

public class Tetris extends Window implements ActionListener {
    public static Timer timer;
    public static final int H = 20, W = 10, C = 25;  // how big the squares and the field will be
    public static final int xM = 50, yM = 50;
    public static Color[] colors = {Color.red, Color.green, Color.blue, Color.orange,
                                    Color.cyan, Color.yellow, Color.magenta, Color.white, Color.pink};
    public static Shape[] shapes = {Z, S, J, L,
                                    I, O, T};



    public static int[][] well = new int[W][H];
    public static int time = 1, iShape = 0;
    public static Shape shape;
    public static int iBkCol = 7;
    public static int zap = 8;
    public Tetris() {
        super("Tetris", 1000, 700);
        shape = shapes[G.rnd(7)];  // gets a random single shape
        clearWell();
        timer = new Timer(30, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public void paintComponent(Graphics g) {
        G.clear(g);
        time++;
        if (time==30) {
            time = 0;
            shape.drop();
        }
//        if (time == 60){
//            time = 0;
//            iShape=(iShape+1)%7;}  /* remainder between 0 and 6 */
//        if (time == 30){shapes[iShape].rot();}
//        shapes[iShape].show(g);
        unZapWell();
        showWell(g);
        shape.show(g);
    }

    public void keyPressed(KeyEvent ke){
        int vk = ke.getKeyCode();
        if (vk == KeyEvent.VK_LEFT){shape.slide(G.LEFT);}
        if (vk == KeyEvent.VK_RIGHT){shape.slide(G.RIGHT);}
        if (vk == KeyEvent.VK_UP){shape.safeRot();}
        if (vk == KeyEvent.VK_DOWN){shape.drop();}
        repaint();
    }

    public static void clearWell(){
        for (int x = 0; x<W; x++){
            for (int y = 0; y<H; y++){
                well[x][y] = iBkCol;
            }
        }
    }

    public static void showWell(Graphics g){
        for (int x = 0; x<W; x++){
            for (int y = 0; y<H; y++){
                g.setColor(colors[well[x][y]]);
                int xx = xM + C*x, yy = yM + C*y;
                g.fillRect(xx, yy, C, C);
                g.setColor(Color.WHITE);
                g.drawRect(xx, yy, C, C);
            }
        }
    }

    public static void zapWell(){
        for (int y = 0; y < H; y++){zapRow(y);}
    }

    public static void zapRow(int y){
        for (int x = 0; x < W; x++){
            if (well[x][y] == iBkCol){return;}
        }  // check if whole line is occupied by squares
        for (int x = 0; x < W; x++){
            well[x][y] = zap;
        }
    }

    public static void unZapWell(){
        boolean done = false;
        for (int y=1; y<H; y++){
            for (int x=0; x < W; x++){
                if (well[x][y-1] != zap && well[x][y] == zap){  // unoccupied line above occupied line
                    done = true;
                    well[x][y] = well[x][y-1];  // swap
                    well[x][y-1] = (y==1)? iBkCol : zap;
                }
            }
            if (done){return;}
        }
    }

    public static void dropNewShape(){
        shape = shapes[G.rnd(7)];
        shape.loc.set(4, 0);  // new shape centered in top
    }

    public static void main(String[] args){
        (PANEL=new Tetris()).launch();
    }

    // ---------- Shape ----------
    public static class Shape{
        public static Shape Z, S, J, L, I, O, T;
        public G.V[] a = new G.V[4];  // the array that holds the 4 squares for each shape
        public int iColor;  // index of color
        public static G.V temp = new G.V(0, 0);
        public static Shape cds = new Shape(new int[]{0,0, 0,0, 0,0, 0,0}, 0); // collision detecting shape
        public G.V loc = new G.V();
        public Shape(int[] xy, int iC){
            for (int i = 0; i < 4; i++){
                a[i] = new G.V();
                a[i].set(xy[i*2], xy[i*2+1]);  // i*2 even, i*2+1 odd
            }
            iColor = iC;
        }
        public void show(Graphics g){
            g.setColor(colors[iColor]);
            for (int i = 0; i < 4; i++){
                g.fillRect(x(i), y(i), C, C);
            }
            g.setColor(Color.white);  // draw the black border of squares
            for (int i = 0; i < 4; i++){
                g.drawRect(x(i), y(i), C, C);
            }
        }
        public int x(int i){return xM + C*(a[i].x+loc.x);}
        public int y(int i){return yM + C*(a[i].y+loc.y);}

        public void rot(){  // rotate the shape
            temp.set(0, 0);
            for (int i = 0; i < 4; i++){
                a[i].set(-a[i].y, a[i].x); // (x, y) -> (-y, x)
                if (temp.x > a[i].x) {temp.x = a[i].x;}
                if (temp.y > a[i].y) {temp.y = a[i].y;}  //  track minimum x, y and keep temp min
            }
            temp.set(-temp.x, -temp.y);  // negate of zeroing out potentially (-) value
            for (int i = 0; i < 4; i++) {
                a[i].add(temp);
            }
        }

        public static boolean collisionDetected(){
            for (int i = 0; i < 4; i++){
                /* don't go outside the boundary */
                G.V v = cds.a[i];
                if (v.x < 0 || v.x >= W || v.y < 0 || v.y >= H ){return true;}
                if (well[v.x][v.y] < iBkCol) {return true;}  // hit some existing square
            }
            return false;
        }
        public void cdsSet(){  // call on an existing shape, object -> cds.a[i]
            for (int i = 0; i < 4; i++) {cds.a[i].set(a[i]); cds.a[i].add(loc);}}
        public void cdsGet(){  // call on an existing shape, cds.a[i] -> object
            for (int i = 0; i < 4; i++) {a[i].set(cds.a[i]);}}
        public void cdsAdd(G.V v){  // adds vector to each element in cds
            for (int i = 0; i < 4; i++) {cds.a[i].add(v);}}
        public void slide(G.V dX){
            cdsSet();
            cdsAdd(dX);
            if (collisionDetected()){return;}
//            cdsGet();
            loc.add(dX);  // slide is updating loc
        }

        public void drop(){
            cdsSet();
            cdsAdd(G.DOWN);
            if (collisionDetected()){
                copyToWell();
                zapWell();
                dropNewShape();
                return;
            }
            loc.add(G.DOWN);
        }

        public void safeRot(){
            rot();
            cdsSet();
            if (collisionDetected()){rot();rot();rot();}
        }

        public void copyToWell(){
            for (int i = 0; i < 4; i++){
                well[a[i].x+loc.x][a[i].y+loc.y] = iColor;  // copy shape into the well
            }
        }



        static{
            Z = new Shape(new int[]{0,0, 1,0, 1,1, 2,1},0);
            S = new Shape(new int[]{0,1, 1,0, 1,1, 2,0},1);
            J = new Shape(new int[]{0,0, 0,1, 1,1, 2,1},2);
            L = new Shape(new int[]{0,1, 1,1, 2,1, 2,0},3);
            I = new Shape(new int[]{0,0, 1,0, 2,0, 3,0},4);
            O = new Shape(new int[]{0,0, 1,0, 0,1, 1,1},5);
            T = new Shape(new int[]{0,1, 1,0, 1,1, 2,1},6);
        }
    }
}
