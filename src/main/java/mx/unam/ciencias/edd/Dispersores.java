package mx.unam.ciencias.edd;
/**
 * Clase para métodos estáticos con dispersores de bytes.
 */
public class Dispersores {

    /* Constructor privado para evitar instanciación. */
    private Dispersores() {
    }

    private static int be(byte a, byte b, byte c, byte d) {
        return ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((c & 0xFF) << 8) | ((d & 0xFF));
    }

    /**
     * Función de dispersión XOR.
     *
     * @param llave la llave a dispersar.
     * @return la dispersión de XOR de la llave.
     */
    public static int dispersaXOR(byte[] llave) {
        int r = 0;
        int i = 0;
        int l = llave.length;
        while (l >= 4) {
            r ^= be(llave[i], llave[i + 1], llave[i + 2], llave[i + 3]);
            i += 4;
            l -= 4;
        }
        int t = 0;
        switch (l) {
            case 3:
                t |= pos(llave, 3);
                break;
            case 2:
                t |= pos(llave, 2);
                break;
            case 1:
                t |= pos(llave, 1);
                break;
        }

        return r ^ t;
    }
    private static int pos(byte[] b, int n) {
        int l = b.length;
        if (n == 3)
            return ((b[l - 3] & 0xFF) << 24) | ((b[l - 2] & 0xFF) << 16) | ((b[l - 1] & 0xFF) << 8);
        if (n == 2)
            return ((b[l - 2] & 0xFF) << 24) | ((b[l - 1] & 0xFF) << 16);

        return ((b[l - 1] & 0xFF) << 24);
    }

    /**
     * Función de dispersión de Bob Jenkins.
     *
     * @param llave la llave a dispersar.
     * @return la dispersión de Bob Jenkins de la llave.
     */
    public static int dispersaBJ(byte[] llave) {
        int a = 0x9E3779B9;
        int b = 0x9E3779B9;
        int c = 0xFFFFFFFF;

        int l = llave.length;
        int i = 0;
        boolean e = true;
        while (e) {
            a += be(getInt(llave, i+3), getInt(llave, i+2),
                             getInt(llave, i+1), getInt(llave, i));
            i += 4;

            b += be(getInt(llave, i+3), getInt(llave, i+2),
                             getInt(llave, i+1), getInt(llave, i));
            i += 4;

            if (l - i >= 4)
                c += be(getInt(llave, i+3), getInt(llave, i+2),
                             getInt(llave, i+1), getInt(llave, i));
            else {
                e = false;
                c += llave.length;
                c += be(getInt(llave, i+2), getInt(llave, i+1),
                             getInt(llave, i), (byte)0);
            }

            i+=4;

            a -= b + c;
            a ^= (c >>> 13);
            b -= c + a;
            b ^= (a << 8);
            c -= a + b;
            c ^= (b >>> 13);

            a -= b + c;
            a ^= (c >>> 12);
            b -= c + a;
            b ^= (a << 16);
            c -= a + b;
            c ^= (b >>> 5);

            a -= b + c;
            a ^= (c >>> 3);
            b -= c + a;
            b ^= (a << 10);
            c -= a + b;
            c ^= (b >>> 15);
        }
        return c;
    }
    /**
     * Función de dispersión Daniel J. Bernstein.
     *
     * @param llave la llave a dispersar.
     * @return la dispersión de Daniel Bernstein de la llave.
     */
    public static int dispersaDJB(byte[] llave) {
        int h = 5381;

        for (int i = 0; i < llave.length; i++)
            h += (h << 5) + (0xFF & llave[i]);

        return h;
    }
    private static byte getInt(byte[] llave, int i) {
        if (i < llave.length)
            return (byte)(0xFF & llave[i]);

        return (byte)0;
    }
}
