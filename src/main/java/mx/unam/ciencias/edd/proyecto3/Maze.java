package mx.unam.ciencias.edd.proyecto3;

import java.util.Random;

import mx.unam.ciencias.edd.Grafica;
import mx.unam.ciencias.edd.Lista;
import mx.unam.ciencias.edd.Pila;
import mx.unam.ciencias.edd.VerticeGrafica;

public class Maze {
    private int width;
    private int height;
    private Box[][] matrix;
    private Box start;
    private Box end;
    private Random rng;
    private Grafica<Box> graph;

    public Maze(int width, int height) {
        rng = new Random();
        this.width = width;
        this.height = height;
        this.matrix = new Box[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                matrix[i][j] = new Box(i, j, width, height, rng.nextInt(16));
        setStartEnd();
        makeMaze();
    }

    public Maze(int width, int height, long seed) {
        rng = new Random(seed);
        this.width = width;
        this.height = height;
        this.matrix = new Box[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                matrix[i][j] = new Box(i, j, width, height, rng.nextInt(16));
        setStartEnd();
        makeMaze();
    }


    public Maze(byte[] code) {
        int c = 0;
        width = code[1]&0xFF;
        height = code[0]&0xFF;
        matrix = new Box[height][width];
        int k = 2;
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                matrix[i][j] = new Box(i, j, width, height, firstBits(code[k]));
                matrix[i][j].setConfig(lastBits(code[k]));
                if(matrix[i][j].isExit())
                    if(c++ == 0)
                        this.start = matrix[i][j];
                    else
                        this.end = matrix[i][j];
                k++;
            }
        start.setStartEnd(1);
        end.setStartEnd(2);
        makeGraph();
    }

    private int firstBits(byte b){
        return (b & 0xF0) >>> 4;
    }

    private int lastBits(byte b){
        return (b & 0x0F);
    }

    public Box[][] getMatrix(){
        return matrix;
    }


    private void makeMaze() {
        Pila<Box> pila = new Pila<>();
        pila.mete(start);
        while (!pila.esVacia()) {
            Box box = pila.mira();
            box.setVisited(true);
            Lista<Box> candidates = getCandidates(box);
            if (!candidates.esVacia()) {
                if (candidates.getLongitud() == 1) {
                    connect(box, candidates.getPrimero());
                    pila.mete(candidates.getPrimero());
                } else {
                    int rdm = rng.nextInt(candidates.getLongitud());
                    Box next = candidates.get(rdm);
                    connect(box, next);
                    pila.mete(next);
                }
            } else{
                Lista<Box> newCandidates = newNeigh(box);
                for(Box newBox : newCandidates)
                    connect(newBox, box);
                pila.saca();
            }
        }
        this.makeGraph();
    }

    private String path(){
        StringBuilder pathBuilder = new StringBuilder();
        Lista<VerticeGrafica<Box>> trayectoria = graph.dijkstra(start, end);
        for(int i = 0; i < trayectoria.getLongitud()-1; i++){
            Box a = trayectoria.get(i).get();
            Box b = trayectoria.get(i+1).get();
            int displaceX = a.getX() * 50;
            int displaceY = a.getY() * 50;

            StringBuilder svgLineBuilder = new StringBuilder();

            svgLineBuilder.append("<g transform=\"translate(").append(displaceY);
            svgLineBuilder.append(",").append(displaceX).append(")\">");
            svgLineBuilder.append(connectLines(a, b));
            pathBuilder.append(svgLineBuilder).append("</g>");
        }
        return pathBuilder.toString();
    }

    private Lista<Box> availableWalls(Box box){
        Lista<Box> candidates = new Lista<>();
        int x = box.getX();
        int y = box.getY();
        Box rightBox;
        Box leftBox;
        Box downBox;
        Box upBox;
        switch (box.getType()) {
            case 1:
                rightBox = matrix[x][y + 1];
                downBox = matrix[x + 1][y];
                if (!box.doorEast())
                    candidates.agrega(rightBox);
                if (!box.doorSouth())
                    candidates.agrega(downBox);
                break;
            case 2:
                leftBox = matrix[x][y-1];
                downBox = matrix[x+1][y];
                if (!box.doorWest())
                    candidates.agrega(leftBox);
                if (!box.doorSouth())
                    candidates.agrega(downBox);
                break;
            case 3:
                leftBox = matrix[x][y-1];
                upBox = matrix[x-1][y];
                if (!box.doorWest())
                    candidates.agrega(leftBox);
                if (!box.doorNorth())
                    candidates.agrega(upBox);
                break;
            case 4:
                rightBox = matrix[x][y+1];
                upBox = matrix[x-1][y];
                if (!box.doorEast())
                    candidates.agrega(rightBox);
                if (!box.doorNorth())
                    candidates.agrega(upBox);
                break;
            case 5:
                leftBox = matrix[x][y-1];
                rightBox = matrix[x][y+1];
                downBox = matrix[x+1][y];
                if (!box.doorWest())
                    candidates.agrega(leftBox);
                if (!box.doorEast())
                    candidates.agrega(rightBox);
                if (!box.doorSouth())
                    candidates.agrega(downBox);
                break;
            case 6:
                upBox = matrix[x-1][y];
                leftBox = matrix[x][y-1];
                downBox = matrix[x+1][y];
                if (!box.doorNorth())
                    candidates.agrega(upBox);
                if (!box.doorWest())
                    candidates.agrega(leftBox);
                if (!box.doorSouth())
                    candidates.agrega(downBox);
                break;
            case 7:
                upBox = matrix[x-1][y];
                leftBox = matrix[x][y-1];
                rightBox = matrix[x][y+1];
                if (!box.doorNorth())
                    candidates.agrega(upBox);
                if (!box.doorWest())
                    candidates.agrega(leftBox);
                if (!box.doorEast())
                    candidates.agrega(rightBox);
                break;
            case 8:
                upBox = matrix[x-1][y];
                rightBox = matrix[x][y+1];
                downBox = matrix[x+1][y];
                if (!box.doorNorth())
                    candidates.agrega(upBox);
                if (!box.doorEast())
                    candidates.agrega(rightBox);
                if (!box.doorSouth())
                    candidates.agrega(downBox);
                break;
            case 9:
                rightBox = matrix[x][y+1];
                upBox = matrix[x-1][y];
                leftBox = matrix[x][y-1];
                downBox = matrix[x+1][y];
                if (!box.doorNorth())
                    candidates.agrega(upBox);
                if (!box.doorWest())
                    candidates.agrega(leftBox);
                if (!box.doorSouth())
                    candidates.agrega(downBox);
                if (!box.doorEast())
                    candidates.agrega(rightBox);
                break;
        }
        return candidates;
    }

    private Lista<Box> newNeigh(Box box){
        Lista<Box> availableWalls = availableWalls(box);
        Lista<Box> newNeigh = new Lista<>();
        int len = availableWalls.getLongitud();
        if(len == 0)
            return newNeigh;
        int wallToBreak = rng.nextInt(len);
        wallToBreak--;
        for(int i = 0; i < wallToBreak;i++){
            int index = rng.nextInt(wallToBreak);
            Box newBox = availableWalls.get(index);
            newNeigh.agrega(newBox);
            connect(box, newBox);
        }

        return newNeigh;
    }

    public Box getStart(){
        return start;
    }
    public Box getEnd(){
        return end;
    }

    public Grafica<Box> getGrafica(){
        return this.graph;
    }

    private void connect(Box a, Box b){
        if (a.getX() == b.getX())
            if (a.getY() < b.getY()) {
                a.setEast(0);
                b.setWest(0);
            } else {
                a.setWest(0);
                b.setEast(0);
            }
        else if (a.getY() == b.getY()) {
            if (a.getX() < b.getX()) {
                a.setSouth(0);
                b.setNorth(0);
            } else {
                a.setNorth(0);
                b.setSouth(0);
            }
        }
    }

    public String toSvg() {
        int numRows = matrix.length;
        int numCols = matrix[0].length;
        int boxWidth = 50;
        int boxHeight = 50;

        int svgWidth = numCols * boxWidth;
        int svgHeight = numRows * boxHeight;

        StringBuilder svgBuilder = new StringBuilder();

        svgBuilder.append("<svg width=\"" + svgWidth + "\" height=\"" + svgHeight + "\">");

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Box box = matrix[i][j];
                int displaceX = j * boxWidth;
                int displaceY = i * boxHeight;

                StringBuilder svgBoxBuilder = new StringBuilder();

                svgBoxBuilder.append("<g transform=\"translate(").append(displaceX);
                svgBoxBuilder.append(",").append(displaceY).append(")\">");
                svgBoxBuilder.append(box.toSvg()).append("</g>");
                svgBuilder.append(svgBoxBuilder);
            }
        }

        svgBuilder.append(path());
        svgBuilder.append("</svg>");

        return svgBuilder.toString();
    }



    private Lista<Box> getCandidates(Box box) {
        Lista<Box> candidates = new Lista<>();
        int x = box.getX();
        int y = box.getY();
        Box rightBox;
        Box leftBox;
        Box downBox;
        Box upBox;
        switch (box.getType()) {
            case 1:
                rightBox = matrix[x][y + 1];
                downBox = matrix[x + 1][y];
                if (!rightBox.getVisited())
                    candidates.agrega(rightBox);
                if (!downBox.getVisited())
                    candidates.agrega(downBox);
                break;
            case 2:
                leftBox = matrix[x][y-1];
                downBox = matrix[x+1][y];
                if(!leftBox.getVisited())
                    candidates.agrega(leftBox);
                if(!downBox.getVisited())
                    candidates.agrega(downBox);
                break;
            case 3:
                leftBox = matrix[x][y-1];
                upBox = matrix[x-1][y];
                if(!leftBox.getVisited())
                    candidates.agrega(leftBox);
                if(!upBox.getVisited())
                    candidates.agrega(upBox);
                break;
            case 4:
                rightBox = matrix[x][y+1];
                upBox = matrix[x-1][y];
                if(!rightBox.getVisited())
                    candidates.agrega(rightBox);
                if(!upBox.getVisited())
                    candidates.agrega(upBox);
                break;
            case 5:
                leftBox = matrix[x][y-1];
                rightBox = matrix[x][y+1];
                downBox = matrix[x+1][y];
                if(!leftBox.getVisited())
                    candidates.agrega(leftBox);
                if(!rightBox.getVisited())
                    candidates.agrega(rightBox);
                if(!downBox.getVisited())
                    candidates.agrega(downBox);
                break;
            case 6:
                upBox = matrix[x-1][y];
                leftBox = matrix[x][y-1];
                downBox = matrix[x+1][y];
                if(!upBox.getVisited())
                    candidates.agrega(upBox);
                if(!leftBox.getVisited())
                    candidates.agrega(leftBox);
                if(!downBox.getVisited())
                    candidates.agrega(downBox);
                break;
            case 7:
                upBox = matrix[x-1][y];
                leftBox = matrix[x][y-1];
                rightBox = matrix[x][y+1];
                if(!upBox.getVisited())
                    candidates.agrega(upBox);
                if(!leftBox.getVisited())
                    candidates.agrega(leftBox);
                if(!rightBox.getVisited())
                    candidates.agrega(rightBox);
                break;
            case 8:
                upBox = matrix[x-1][y];
                rightBox = matrix[x][y+1];
                downBox = matrix[x+1][y];
                if(!upBox.getVisited())
                    candidates.agrega(upBox);
                if(!rightBox.getVisited())
                    candidates.agrega(rightBox);
                if(!downBox.getVisited())
                    candidates.agrega(downBox);
                break;
            case 9:
                rightBox = matrix[x][y+1];
                upBox = matrix[x-1][y];
                leftBox = matrix[x][y-1];
                downBox = matrix[x+1][y];
                if(!upBox.getVisited())
                    candidates.agrega(upBox);
                if(!leftBox.getVisited())
                    candidates.agrega(leftBox);
                if(!downBox.getVisited())
                    candidates.agrega(downBox);
                if(!rightBox.getVisited())
                    candidates.agrega(rightBox);
                break;
        }
        return candidates;
    }

    private void setStartEnd(){
        int side1 = rng.nextInt(4);
        int side2 = rng.nextInt(4);
        int sideR = rng.nextInt(height);
        int sideC = rng.nextInt(width);
        switch(side1){
            case 0: start = matrix[sideR][width-1];
                matrix[sideR][width-1].setDoors(4);
                matrix[sideR][width-1].setStartEnd(1);;
                break;
            case 1: start = matrix[0][sideC];
                matrix[0][sideC].setDoors(3);
                matrix[0][sideC].setStartEnd(1);
                break;
            case 2: start = matrix[height-1][sideC];
                matrix[height-1][sideC].setDoors(1);
                matrix[height-1][sideC].setStartEnd(1);
                break;
            case 3: start = matrix[sideR][0];
                matrix[sideR][0].setDoors(2);
                matrix[sideR][0].setStartEnd(1);;
        }
        int sideR2 = rng.nextInt(height);
        int sideC2 = rng.nextInt(width);
        if(side1 == side2){
            while(sideR == sideR2)
                sideR2 = rng.nextInt(height);
            while(sideC == sideC2)
                sideC2 = rng.nextInt(width);
        }
        if(side1 != side2){
            while(sideR2 == sideC)
                sideR2 = rng.nextInt(height);
            while(sideC2 == sideR)
                sideC2 = rng.nextInt(width);
        }

        switch(side2){
            case 0: end = matrix[sideR2][width-1];
                matrix[sideR2][width-1].setDoors(4);
                matrix[sideR2][width-1].setStartEnd(2);
                break;
            case 1: end = matrix[0][sideC2];
                matrix[0][sideC2].setDoors(3);
                matrix[0][sideC2].setStartEnd(2);
                break;
            case 2: end = matrix[height-1][sideC2];
                matrix[height-1][sideC2].setDoors(1);
                matrix[height-1][sideC2].setStartEnd(2);
                break;
            case 3: end = matrix[sideR2][0];
                matrix[sideR2][0].setDoors(2);
                matrix[sideR2][0].setStartEnd(2);;
        }


    }

    @Override
    public String toString(){
        String s = "";
        for(Box[] row: matrix){
            for(Box cell: row)
                s+= cell.toString() + "\t";
            s+= "\n";
        }
        return s + "\n START " + start.toString() + "\n END " + end.toString();
    }

    private void connectDoors(Box box, Grafica<Box> g){
        int i = box.getX();
        int j = box.getY();

        box.setVisited(true);
        int exit = box.getExit();
        Box neigh;
        int w = 1+box.getScore();
        if (box.doorSouth() && exit != 1) {
            neigh = matrix[i + 1][j];
            if (!g.sonVecinos(neigh, box))
                g.conecta(box, neigh, w+neigh.getScore());
        }
        if (box.doorWest() && exit != 2){
            neigh = matrix[i][j-1];
            if (!g.sonVecinos(neigh, box))
                g.conecta(box, neigh, w+neigh.getScore());
        }
        if (box.doorNorth() && exit != 3){
            neigh = matrix[i-1][j];
            if (!g.sonVecinos(neigh, box))
                g.conecta(box, neigh, w+neigh.getScore());
        }
        if (box.doorEast() && exit != 4){
            neigh = matrix[i][j+1];
            if (!g.sonVecinos(neigh, box))
                g.conecta(box, neigh, w+neigh.getScore());
        }
    }

    private void makeGraph(){
        Grafica<Box> g = new Grafica<>();
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                Box ij = matrix[i][j];
                ij.setVisited(false);
                g.agrega(ij);
            }

        for(Box box: g)
            if(!box.getVisited())
                connectDoors(box, g);

        this.graph = g;
    }

    private String connectLines(Box a, Box b){
        int width = 50;
        int height = 50;
        int wallThickness = 3;
        int centerX = width / 2;
        int centerY = height / 2;
        StringBuilder svgBuilder = new StringBuilder();
        if (a.getX() == b.getX()) {
            if (a.getY() < b.getY()) {
                svgBuilder.append("<line x1=\"").append(centerX).append("\" y1=\"").append(centerY);
                svgBuilder.append("\" x2=\"").append(centerX + width).append("\" y2=\"").append(centerY);
                svgBuilder.append("\" stroke=\"blue\" stroke-width=\"").append(wallThickness);
                svgBuilder.append("\" />");
            } else {
                svgBuilder.append("<line x1=\"").append(centerX).append("\" y1=\"").append(centerY);
                svgBuilder.append("\" x2=\"").append(centerX - width).append("\" y2=\"").append(centerY);
                svgBuilder.append("\" stroke=\"blue\" stroke-width=\"").append(wallThickness);
                svgBuilder.append("\" />");
            }
        } else if (a.getY() == b.getY()) {
            if (a.getX() < b.getX()) {
                svgBuilder.append("<line x1=\"").append(centerX).append("\" y1=\"").append(centerY);
                svgBuilder.append("\" x2=\"").append(centerX).append("\" y2=\"").append(centerY + height);
                svgBuilder.append("\" stroke=\"blue\" stroke-width=\"").append(wallThickness);
                svgBuilder.append("\" />");
            } else {
                svgBuilder.append("<line x1=\"").append(centerX).append("\" y1=\"").append(centerY);
                svgBuilder.append("\" x2=\"").append(centerX).append("\" y2=\"").append(centerY - height);
                svgBuilder.append("\" stroke=\"blue\" stroke-width=\"").append(wallThickness);
                svgBuilder.append("\" />");
            }
        }
        return svgBuilder.toString();
    }

    public byte[] toBytes(){
        byte[] total = new byte[(height*width) + 2];
        total[0] = (byte)(height & 0xFF);
        total[1] = (byte)(width & 0xFF);
        int c = 2;
        for(int i = 0; i < height; i++)
            for(int j = 0; j < width; j++)
            total[c++] = matrix[i][j].toByte();
        return total;
    }
}
