package javaLab.graphics;

import java.awt.*; //awt: abstract windows toolkit
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class G {
    public static Random RND = new Random(); //RND is a constant
    public static int rnd (int max) {return RND.nextInt(max);}
    public static G.V UP = new G.V(0,-1);
    public static G.V DOWN = new G.V(0,1);
    public static G.V LEFT = new G.V(-1,0);
    public static G.V RIGHT = new G.V(1,0);
    //? nextInt() scans the next token of the input data as an "int"

    public static Color rndColor(){
        return new Color(rnd(256),rnd(256),rnd(256));
    }
    public static void clear(Graphics g){
        g.setColor(Color.black); g.fillRect(0,0,5000,5000);
    } // clear the window
    public static void drawCircle(Graphics g, int x, int y, int r) {
        g.drawOval(x-r,y-r, 2*r,2*r); //a circle
    }

    // 0915
    //---------------------V--------------------// Vector
    public static class V implements Serializable{
        public static Transform T = new Transform(); //0929
        public int x,y;
        //constructor
        public V(int x, int y){this.set(x,y);}

        public V() {}

        public void set(int x, int y){this.x = x; this.y = y;}
        public void set(V v){this.x = v.x; this.y = v.y;} // 0922, 'set' but for V
        public void add(V v){x += v.x; y += v.y;}

        //0929
        public void setT(V v){set(v.tx(),v.ty());}  // for v, replace x with tx, replace y with ty
        public int tx() {return x*T.n/T.d + T.dx;}  // x transformed by T
        public int ty() {return y*T.n/T.d + T.dy;}  // y transformed by T

        public void blend(V v, int k) {
            set((k*x+v.x)/(k+1),(k*y+v.y)/(k+1));
        }
        // 1004, set new x,y values into existing one
        // !! without /(k+1) the shape on left-top will be twice as large at next blend,
        // so we can know that there is some problem with the original blend() and go find it
        // you can't blend more than two shapes because the norm grows so big that the new shape you draw cannot match it

        //-----------Transform----------//0929
        // x'(or tx) = x*(n/d) + dx, y'(or ty) = y*(n/d) + dy
        public static class Transform {
            public int dx, dy, n, d; // dx&dy: translations in the x&y direction
            public void setScale(int oW, int oH, int nW, int nH) { // old&new Width&Height
                // pick maximum value between (nW&nH), (oW&oH) and divide by it to guarantee that the new one is inside
                n = (nW > nH) ? nW : nH;
                d = (oW > oH) ? oW : oH;
            }
            // X' = (X - oX - oW/2)n/d + nX + nW/2
            //    = X*n/d + ((-oX - oW/2)n/d + nX + nW/2)
            //    = X*n/d + dx
            public int offSet(int oX, int oW, int nX, int nW) {
                return (-oX - oW/2)*n/d + nX + nW/2; //! -oX, not oX
            }
            public void set(VS oVS, VS nVS) {
                setScale(oVS.size.x, oVS.size.y, nVS.size.x, nVS.size.y);
                dx = offSet(oVS.loc.x, oVS.size.x, nVS.loc.x, nVS.size.x);
                dy = offSet(oVS.loc.y, oVS.size.y, nVS.loc.y, nVS.size.y); // ? check usage
            }
            public void set(BBox oB, VS nVS) {
                setScale(oB.h.size(), oB.v.size(), nVS.size.x, nVS.size.y);
                dx = offSet(oB.h.lo, oB.h.size(), nVS.loc.x, nVS.size.x);
                dy = offSet(oB.v.lo, oB.v.size(), nVS.loc.y, nVS.size.y); // ? check usage
            }
        }
    }

    //---------------------VS--------------------// about the rectangle
    public static class VS{
        public V loc, size;
        public VS (int x, int y, int w, int h){loc = new V(x,y); size = new V(w, h);}
        public void fill(Graphics g, Color c) {
            g.setColor(c);
            // !! 1013 this was originally g.setColor(Color.white), and all boxes I drew with SW-SW were white.
            // must set it to c to make it public Color c = G.rndColor(); *in ReactionTest
            g.fillRect(loc.x, loc.y, size.x, size.y);
        }

        public void draw(Graphics g, Color c) {
            g.setColor(c);
            g.drawRect(loc.x, loc.y, size.x, size.y);
        }

        public boolean hit(int x, int y) {
            //test whether x,y,... was inside the rectangle. If all inside then you hit on the rectangle
            return loc.x < x && loc.y < y && x < (loc.x + size.x) && y < (loc.y + size.y);
        }

        // 1013
        public int xL() {return loc.x;}
        public int xM() {return loc.x + size.x / 2;}
        public int xH() {return loc.x + size.x;}
        public int yL() {return loc.y;}
        public int yM() {return loc.y + size.y / 2;}
        public int yH() {return loc.y + size.y;}

        public void resize(int x, int y) {
            //if hit on the rectangle: change the size of the rectangle you click on
            if (x > loc.x && y > loc.y) {size.set(x-loc.x, y-loc.y);}
        }
    }

    //---------------------LoHi--------------------// 0927
    public static class LoHi{
        public int lo, hi;
        //constructor
        public LoHi(int min, int max) {lo = min; hi = max;} //this.lo & this.hi
        public void add(int x) {if (x < lo) {lo = x;} if (x > hi) {hi = x;}} //renew
        public void set(int x) {lo = x; hi = x;} // initial value?
        public int size() {return (hi - lo) == 0 ? 1 : hi-lo;}
        //️conditional expression: if ...==0: return 1, or return (hi-lo)
    }

    //---------------------BBox--------------------// 0927, bounding box of the line you draw
    public static class BBox{
        public LoHi h, v;
        public BBox () {h = new LoHi(0,0); v = new LoHi(0,0);}
        public void set(int x, int y) {h.set(x); v.set(y);}
        public void add(V v) {h.add(v.x); this.v.add(v.y); }// there are 2 v. this.v !!
        public void add(int x, int y) {h.add(x); this.v.add(y);} // same as above?
        public VS getNewVS() {return new VS(h.lo, v.lo, h.size(),v.size());}
        public void draw(Graphics g) {g.drawRect(h.lo, v.lo, h.size(),v.size());}

    }

    //---------------------Pl--------------------// 0922
    public static class Pl implements Serializable {
        //'points' is an array, a bunch of 'point', number in V[] is the number of 'point'
        public V[] points;
        public Pl (int count) {
            points = new V[count]; //共有count个point
            for(int i = 0; i < count; i++) {points[i] = new V(0,0);}
        }
        public int size() {return points.length;}

        //0929
        public void transform(){
            for (int i = 0; i < points.length; i++) {
                points[i].setT(points[i]); // for every point, transform its x and y
            }
        }

        public void drawN(Graphics g, int n) {
            for (int i = 1; i < n; i++) {
                g.drawLine(points[i-1].x,points[i-1].y,points[i].x, points[i].y);
            }
        }
        public void drawNDots(Graphics g, int n) {
            for (int i = 0; i < n; i++) {drawCircle(g, points[i].x, points[i].y, 2);}
        } // 0927 draw dots on the line
        public void draw(Graphics g) {drawN(g, points.length);}
    }


    // -------- button 230313 --------
    public static abstract class Button{
        // going to be used everywhere
        // doesn't have any idea what is going to happen when button clicked
        // so abstract
        public abstract void act();
        public boolean enabled = true, bordered = true;
        public String text = "";
        public VS vs = new VS(0,0,0,0);
        public LookAndFeel lnf = new LookAndFeel();

        public Button(Button.List list, String str){
            if (list != null){list.add(this);}
            text = str;
        }

        public void show(Graphics g){
            if (vs.size.x == 0){setSize(g);} // text in button demands resizing
            vs.fill(g, lnf.back);
            if (bordered){vs.draw(g, lnf.border);} // draw a border
            g.setColor(enabled? lnf.enabled : lnf.disabled);
            g.drawString(text, vs.loc.x+lnf.m.x, vs.loc.y+lnf.dyText);
        }

        public void setSize(Graphics g){
            FontMetrics fm = g.getFontMetrics();
            vs.size.set(2*lnf.m.x+fm.stringWidth(text), 2*lnf.m.y+fm.getHeight());
            lnf.dyText = fm.getAscent()+lnf.m.y;  // how far from the baseline
        }

        public void set(int x, int y){vs.loc.set(x, y);}

        public boolean hit(int x, int y){return vs.hit(x, y);}

        public void click(){if (enabled){act();}}

        public static class LookAndFeel {
            public Color back = Color.BLACK;
            public Color border = Color.cyan;
            public Color enabled = Color.pink;
            public Color disabled = Color.green;
            public V m = new V(5, 3);// margins
            public int dyText = 0;
        }
        // ---------- List ----------
        // inside or outside button? inside
        public static class List extends ArrayList<Button> {
            public Button hit(int x, int y){
                for (Button b: this){
                    if (b.hit(x, y)){return b;}
                }return null;
            }
            public boolean clicked(int x, int y){
                Button b = hit(x, y);
                if (b == null){return false;}
                b.click();
                return true;
            }

            public void show(Graphics g) {for(Button b: this){b.show(g);}}
        }
    }
}
