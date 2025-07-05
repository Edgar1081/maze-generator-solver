package mx.unam.ciencias.edd.proyecto3;

public class Proyecto3 {
    private static boolean flagG = false;
    private static boolean flagS = false;
    private static int sValue = 0;
    private static boolean flagW = false;
    private static int wValue = 0;
    private static boolean flagH = false;
    private static int hValue = 0;

    public static void main(String[] args) {
        Maze maze;
        manageFlags(args);

        if(args.length == 0){
            byte[] code = IO.readBytes();
            maze = new Maze(code);
            System.out.println(maze.toSvg());
            System.exit(0);
        }


        if((hValue < 2 || wValue < 2) || wValue > 255 || hValue > 255){
            uso();
            System.exit(0);
        }

        if (flagG) {
            if (flagS) {
                maze = new Maze(wValue, hValue, sValue);
            } else {
                maze = new Maze(wValue, hValue);
            }
            IO.printBytes(maze);
        }
    }

    private static void manageFlags(String[] args) {

        if (args.length == 0)
            return;

        for (int i = 0; i < args.length; i++) {
            String flag = args[i];
            switch (flag) {
                case "-g":
                    flagG = true;
                    break;
                case "-s":
                    flagS = true;
                    try {
                        sValue = Integer.valueOf(args[++i]);
                    } catch (Exception e) {
                        System.out.println("Semilla invalida");
                    }
                    break;
                case "-w":
                    flagW = true;
                    try {
                        wValue = Integer.valueOf(args[++i]);
                    } catch (Exception e) {
                        System.out.println("Ancho invalido");
                    }
                    break;
                case "-h":
                    flagH = true;
                    try {
                        hValue = Integer.valueOf(args[++i]);
                    } catch (Exception e) {
                        System.out.println("Alto invalido");
                    }
                    break;
            }

        }

        if (flagG && (!flagW && !flagH))
            uso();
        if (!flagG)
            uso();
    }

    private static void uso() {
        System.out.println("[-g, -s N, -w N>1, -h N>1 |  -g, -w N>1, -h N>1]");
    }

}
