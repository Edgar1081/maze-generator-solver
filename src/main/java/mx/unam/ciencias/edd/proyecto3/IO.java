package mx.unam.ciencias.edd.proyecto3;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.File;

public class IO {

    public static void printBytes(Maze mz) {

        byte[] ini = { (byte) 0x4d, (byte) 0x41, (byte) 0x5a, (byte) 0x45 };
        byte[] maze = mz.toBytes();
        try {
            BufferedOutputStream o = new BufferedOutputStream(System.out);
            for (byte m : ini)
                o.write(m & 0xFF);
            for (byte box : maze)
                o.write(box & 0xFF);

            o.flush();
            o.close();
        } catch (IOException ioe) {
            System.out.println("Error al escribir bytes");
        }
    }

    public static byte[] readBytes() {
        int c = 0;
        int i;
        int x = 2;
        int w = 0;
        int h = 0;
        byte [] code = new byte[1];
        byte[] ini = { (byte) 0x4d, (byte) 0x41, (byte) 0x5a, (byte) 0x45 };
        try {
            BufferedInputStream in = new BufferedInputStream(System.in);
            while ((i = in.read()) != -1) {
                if (c < 4) {
                    if(i != ini[c])
                        throw new IllegalArgumentException("Tipo de archivo erroneo");
                    c++;
                } else if (c == 4) {
                    h = i & 0xFF;
                    c++;
                } else if (c == 5) {
                    w = i & 0xFF;
                    code = new byte[h*w + 2];
                    code[0] = (byte)h;
                    code[1] = (byte)w;
                    c++;
                } else {
                    code[x++] = (byte) i;
                }
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al leer bytes");
        }
        return code;
    }
}
