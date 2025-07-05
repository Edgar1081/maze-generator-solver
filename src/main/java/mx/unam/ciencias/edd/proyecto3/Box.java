package mx.unam.ciencias.edd.proyecto3;

public class Box {
    private int north;
    private int east;
    private int south;
    private int west;
    private int score;

    private boolean visited = false;

    private int x;
    private int y;

    private int type;
    private int exit;


    private boolean start = false;
    private boolean end = false;

    public Box(int x, int y, int w, int h, int score) {
        this.x = x;
        this.y = y;
        this.score = score;
        north = 1;
        south = 1;
        east = 1;
        west = 1;
        setType(w, h);
    }

    public boolean isExit(){
        return exit > 0;
    }

    private void setExit() {
        switch (type) {
            case 1:
                if (doorWest())
                    this.exit = 2;
                if (doorNorth())
                    this.exit = 3;
                break;
            case 2:
                if (doorNorth())
                    this.exit = 3;
                if (doorEast())
                    this.exit = 4;
                break;
            case 3:
                if (doorSouth())
                    this.exit = 1;
                if (doorEast())
                    this.exit = 4;
                break;
            case 4:
                if (doorSouth())
                    this.exit = 1;
                if (doorWest())
                    this.exit = 2;
                break;
            case 5:
                if (doorNorth())
                    this.exit = 3;
                break;
            case 6:
                if (doorEast())
                    this.exit = 4;
                break;
            case 7:
                if (doorSouth())
                    this.exit = 1;
                break;
            case 8:
                if (doorWest())
                    this.exit = 2;
                break;
        }
    }

    public void setStartEnd(int startEnd) {
        if (startEnd == 1)
            this.start = true;
        else
            this.end = true;
    }

    private void setType(int w, int h) {
        if (x == 0 && y == 0)
            this.type = 1;
        else if (x == 0 && y == w - 1)
            this.type = 2;
        else if (x == h - 1 && y == w - 1)
            this.type = 3;
        else if (x == h - 1 && y == 0)
            this.type = 4;
        else if (x == 0 && (y > 0 && y < w - 1))
            this.type = 5;
        else if ((x > 0 && x < h - 1) && y == w - 1)
            this.type = 6;
        else if (x == h - 1 && (y > 0 && y < w - 1))
            this.type = 7;
        else if ((x > 0 && x < h - 1) && y == 0)
            this.type = 8;
        else
            this.type = 9;
    }

    @Override
    public String toString() {
        int cod = (south << 3) + (west << 2) + (north << 1) + east;
        return Integer.toBinaryString(cod) + "(" + x + "," + y + ")" + exit;
    }

    public int getExit(){
        return exit;
    }

    public String getConfig(){
        String s = "" + (south) + "," + (west)
            + "," + north + ","  + east;
        return s + "(" + x + "," + y + ")";
        //System.out.println("Config " + (c&0xFF) + " EXIT " + exit + doorNorth());
    }

    public void setConfig(int c){
        this.east = ((c & 0xFF) & 1) & 0xFF;
        this.north = (((c & 0xFF) >> 1) & 1) & 0xFF;
        this.west = (((c & 0xFF) >> 2) & 1) & 0xFF;
        this.south = (((c & 0xFF) >> 3) & 1) & 0xFF;
        setExit();
        //System.out.println("Config " + (c&0xFF) + " EXIT " + exit + doorNorth());
    }


    public void setDoors(int e) {
        exit = e;
        if(e == 1)
            south = 0;
        else if (e == 2)
            west = 0;
        else if (e == 3)
            north = 0;
        else
            east = 0;
    }

    public int getType() {
        return type;
    }

    public void setVisited(boolean b) {
        this.visited = b;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getVisited() {
        return visited;
    }

    public void setSouth(int s) {
        this.south = s;
    }

    public void setWest(int w) {
        this.west = w;
    }

    public void setNorth(int n) {
        this.north = n;
    }

    public void setEast(int e) {
        this.east = e;
    }

    public String toSvg() {
        int width = 50;
        int height = 50;
        int wallThickness = 3;
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = 15;

        StringBuilder svgBuilder = new StringBuilder();

        if (south == 1) {
            svgBuilder.append("<line x1=\"").append(0).append("\" y1=\"").append(height);
            svgBuilder.append("\" x2=\"").append(width).append("\" y2=\"").append(height);
            svgBuilder.append("\" stroke=\"black\" stroke-width=\"").append(wallThickness);
            svgBuilder.append("\" />");
        }
        if (west == 1) {
            svgBuilder.append("<line x1=\"").append(0).append("\" y1=\"").append(0);
            svgBuilder.append("\" x2=\"").append(0).append("\" y2=\"").append(height);
            svgBuilder.append("\" stroke=\"black\" stroke-width=\"").append(wallThickness);
            svgBuilder.append("\" />");
        }
        if (north == 1) {
            svgBuilder.append("<line x1=\"").append(0).append("\" y1=\"").append(0);
            svgBuilder.append("\" x2=\"").append(width).append("\" y2=\"").append(0);
            svgBuilder.append("\" stroke=\"black\" stroke-width=\"").append(wallThickness);
            svgBuilder.append("\" />");
        }
        if (east == 1) {
            svgBuilder.append("<line x1=\"").append(width).append("\" y1=\"").append(0);
            svgBuilder.append("\" x2=\"").append(width).append("\" y2=\"").append(height);
            svgBuilder.append("\" stroke=\"black\" stroke-width=\"").append(wallThickness);
            svgBuilder.append("\" />");
        }
        if (start) {
            svgBuilder.append("<circle cx=\"").append(centerX);
            svgBuilder.append("\" cy=\"").append(centerY);
            svgBuilder.append("\" r=\"").append(radius);
            svgBuilder.append("\" fill=\"green\" />");
        }
        if (end) {
            svgBuilder.append("<circle cx=\"").append(centerX);
            svgBuilder.append("\" cy=\"").append(centerY);
            svgBuilder.append("\" r=\"").append(radius);
            svgBuilder.append("\" fill=\"red\" />");
        }
        svgBuilder.append("<text x=\"").append(centerX).append("\" y=\"").append(centerY);
        svgBuilder.append("\" text-anchor=\"middle\" dominant-baseline=\"middle\">");
        svgBuilder.append(score).append("</text>");

        return svgBuilder.toString();
    }
    public boolean doorNorth(){
        return north == 0;
    }
    public boolean doorSouth(){
        return south == 0;
    }
    public boolean doorEast(){
        return east == 0;
    }
    public boolean doorWest(){
        return west == 0;
    }
    private String connectDoorsLines(){
        int width = 50;
        int height = 50;
        int wallThickness = 3;
        int centerX = width / 2;
        int centerY = height / 2;

        StringBuilder svgBuilder = new StringBuilder();

        if (doorSouth() && exit != 1){
            svgBuilder.append("<line x1=\"" + centerX + "\" y1=\"" + centerY
                    + "\" x2=\"" + centerX + "\" y2=\"" + height
                    + "\" stroke=\"blue\" stroke-width=\"" + wallThickness
                    + "\" />");
        }
        if (doorWest() && exit != 2){
            svgBuilder.append("<line x1=\"" + centerX + "\" y1=\"" + centerY
                    + "\" x2=\"" + 0 + "\" y2=\"" + centerY
                    + "\" stroke=\"blue\" stroke-width=\"" + wallThickness
                    + "\" />");
        }
        if (doorNorth() && exit != 3){
            svgBuilder.append("<line x1=\"" + centerX + "\" y1=\"" + centerY
                    + "\" x2=\"" + centerX + "\" y2=\"" + 0
                    + "\" stroke=\"blue\" stroke-width=\"" + wallThickness
                    + "\" />");
        }
        if (doorEast() && exit != 4){
            svgBuilder.append("<line x1=\"" + centerX + "\" y1=\"" + centerY
                    + "\" x2=\"" + width + "\" y2=\"" + centerY
                    + "\" stroke=\"blue\" stroke-width=\"" + wallThickness
                    + "\" />");
        }
        return svgBuilder.toString();
    }
    public byte toByte(){
        int doorCode = ((south << 3)+(west << 2)+(north << 1)+(east)) & 0xFF;
        int scoreCode = (score << 4) & 0xFF;

        return (byte)((scoreCode + doorCode) & 0xFF);
    }
    public byte toByteDoors(){
        int doorCode = (south << 3)+(west << 2)+(north << 1)+(east);
        return (byte) (doorCode & 0xFF);
    }
    public int getScore(){
        return score;
    }
}
